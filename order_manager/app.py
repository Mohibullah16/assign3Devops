import os
import base64
from datetime import datetime
from typing import Optional, List
from fastapi import FastAPI, Request, Form, File, UploadFile, HTTPException
from fastapi.responses import HTMLResponse, RedirectResponse, JSONResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from motor.motor_asyncio import AsyncIOMotorClient
from pymongo import DESCENDING
from bson.objectid import ObjectId
from groq import Groq
from dotenv import load_dotenv
from pydantic import BaseModel, Field

# Load environment variables
load_dotenv()

app = FastAPI(title="Order Manager")

# Mount static files
app.mount("/static", StaticFiles(directory="order_manager/static"), name="static")

# Templates
templates = Jinja2Templates(directory="order_manager/templates")

# --- Database Setup (Motor - Async MongoDB) ---
MONGO_URI = os.getenv("MONGO_URI")
TEST_MODE = os.getenv("TEST_MODE", "false").lower() == "true"

if TEST_MODE:
    MONGO_URI = os.getenv("TEST_MONGO_URI", MONGO_URI)

client = AsyncIOMotorClient(MONGO_URI)
db = client.order_management_db if not TEST_MODE else client.order_management_test_db
orders_collection = db.orders

# --- Groq Setup ---
groq_client = Groq(api_key=os.getenv("GROQ_API_KEY"))

# --- Pydantic Models ---
class InvoiceItem(BaseModel):
    sr_no: Optional[int] = None
    sku: str
    description: str
    qty: int
    price: float
    amount: float

class InvoiceHeader(BaseModel):
    invoice_number: Optional[str] = None
    date: Optional[str] = None
    customer: Optional[str] = None
    salesman: Optional[str] = None

class OCRResponse(BaseModel):
    header: InvoiceHeader
    items: List[InvoiceItem]

class OrderCreate(BaseModel):
    invoice_number: Optional[str] = None
    customer: Optional[str] = None
    salesman: Optional[str] = None
    items: List[InvoiceItem]

# --- Helper: Calculate KPIs ---
async def get_kpis():
    pipeline = [
        {
            "$group": {
                "_id": None,
                "total_orders": {"$sum": 1},
                "total_spend": {"$sum": "$total_amount"},
                "avg_order": {"$avg": "$total_amount"}
            }
        }
    ]
    result = []
    async for doc in orders_collection.aggregate(pipeline):
        result.append(doc)
    
    if result:
        data = result[0]
        return {
            "count": data['total_orders'],
            "spend": round(data['total_spend'], 2),
            "avg": round(data['avg_order'], 2)
        }
    return {"count": 0, "spend": 0, "avg": 0}

# --- Routes ---

@app.get("/", response_class=HTMLResponse)
async def index(request: Request):
    """Homepage with orders list and KPIs"""
    orders = []
    async for order in orders_collection.find().sort("created_at", DESCENDING).limit(50):
        order['_id'] = str(order['_id'])
        orders.append(order)
    
    kpis = await get_kpis()
    return templates.TemplateResponse("index.html", {
        "request": request,
        "orders": orders,
        "kpis": kpis
    })

@app.post("/add_order")
async def add_order(
    request: Request,
    invoice_number: Optional[str] = Form(None),
    customer: Optional[str] = Form(None),
    salesman: Optional[str] = Form(None),
    items_json: str = Form(...),
    invoice_image: Optional[UploadFile] = File(None)
):
    """Add new order with multiple items"""
    try:
        import json
        items = json.loads(items_json)
        
        # Validate items
        if not items or len(items) == 0:
            raise HTTPException(status_code=400, detail="At least one item is required")
        
        # Calculate totals
        total_qty = sum(item['qty'] for item in items)
        total_amount = sum(item['amount'] for item in items)
        
        # Handle invoice image upload
        filename = None
        if invoice_image and invoice_image.filename:
            timestamp = int(datetime.now().timestamp())
            filename = f"inv_{timestamp}_{invoice_image.filename}"
            filepath = os.path.join("order_manager", "static", "uploads", filename)
            
            with open(filepath, "wb") as f:
                content = await invoice_image.read()
                f.write(content)
        
        # Create order document
        order_data = {
            "invoice_number": invoice_number or f"INV-{int(datetime.now().timestamp())}",
            "customer": customer or "Walk-in Customer",
            "salesman": salesman or "N/A",
            "items": items,
            "total_qty": total_qty,
            "total_amount": total_amount,
            "invoice_image": filename,
            "created_at": datetime.now()
        }
        
        await orders_collection.insert_one(order_data)
        return RedirectResponse(url="/", status_code=303)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error adding order: {str(e)}")

@app.get("/delete/{order_id}")
async def delete_order(order_id: str):
    """Delete an order"""
    try:
        result = await orders_collection.delete_one({"_id": ObjectId(order_id)})
        if result.deleted_count == 0:
            raise HTTPException(status_code=404, detail="Order not found")
        return RedirectResponse(url="/", status_code=303)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error deleting order: {str(e)}")

@app.get("/edit/{order_id}", response_class=HTMLResponse)
async def edit_order(request: Request, order_id: str):
    """Show edit form for an order"""
    try:
        order = await orders_collection.find_one({"_id": ObjectId(order_id)})
        if not order:
            raise HTTPException(status_code=404, detail="Order not found")
        
        order['_id'] = str(order['_id'])
        return templates.TemplateResponse("edit.html", {
            "request": request,
            "order": order
        })
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error loading order: {str(e)}")

@app.post("/update/{order_id}")
async def update_order(
    order_id: str,
    invoice_number: Optional[str] = Form(None),
    customer: Optional[str] = Form(None),
    salesman: Optional[str] = Form(None),
    items_json: str = Form(...)
):
    """Update an existing order"""
    try:
        import json
        items = json.loads(items_json)
        
        # Calculate totals
        total_qty = sum(item['qty'] for item in items)
        total_amount = sum(item['amount'] for item in items)
        
        update_data = {
            "invoice_number": invoice_number,
            "customer": customer,
            "salesman": salesman,
            "items": items,
            "total_qty": total_qty,
            "total_amount": total_amount
        }
        
        result = await orders_collection.update_one(
            {"_id": ObjectId(order_id)},
            {"$set": update_data}
        )
        
        if result.matched_count == 0:
            raise HTTPException(status_code=404, detail="Order not found")
        
        return RedirectResponse(url="/", status_code=303)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error updating order: {str(e)}")

@app.get("/search")
async def search_orders(request: Request, q: str = ""):
    """Search orders by invoice number or customer name"""
    try:
        if not q:
            return RedirectResponse(url="/", status_code=303)
        
        query = {
            "$or": [
                {"invoice_number": {"$regex": q, "$options": "i"}},
                {"customer": {"$regex": q, "$options": "i"}}
            ]
        }
        
        orders = []
        async for order in orders_collection.find(query).sort("created_at", DESCENDING):
            order['_id'] = str(order['_id'])
            orders.append(order)
        
        kpis = await get_kpis()
        return templates.TemplateResponse("index.html", {
            "request": request,
            "orders": orders,
            "kpis": kpis,
            "search_query": q
        })
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Search error: {str(e)}")

@app.post("/process_ocr")
async def process_ocr(file: UploadFile = File(...)):
    """Process invoice image with OCR using Groq Vision API"""
    try:
        # Read and encode image
        image_content = await file.read()
        encoded_string = base64.b64encode(image_content).decode('utf-8')
        
        # Groq API call with structured prompt
        chat_completion = groq_client.chat.completions.create(
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "text",
                            "text": """Analyze this invoice/receipt image. Extract the following:

1. Header information: invoice_number, date, customer, salesman
2. Items table with columns: sr_no, sku/code, description, qty, price, amount

Return ONLY valid JSON in this exact format:
{
  "header": {
    "invoice_number": "string or null",
    "date": "string or null",
    "customer": "string or null",
    "salesman": "string or null"
  },
  "items": [
    {
      "sr_no": 1,
      "sku": "CODE-123",
      "description": "Item description",
      "qty": 1,
      "price": 100.0,
      "amount": 100.0
    }
  ]
}

Preserve exact SKU codes and descriptions. If a field is not visible, use null."""
                        },
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": f"data:image/jpeg;base64,{encoded_string}"
                            }
                        }
                    ]
                }
            ],
            model="llama-3.2-11b-vision-preview",
            response_format={"type": "json_object"},
            temperature=0.1
        )
        
        # Parse response
        result = chat_completion.choices[0].message.content
        return JSONResponse(content={"success": True, "data": result})
        
    except Exception as e:
        print(f"OCR Error: {str(e)}")
        return JSONResponse(
            content={
                "success": False,
                "error": "OCR processing failed",
                "data": {
                    "header": {
                        "invoice_number": None,
                        "date": None,
                        "customer": None,
                        "salesman": None
                    },
                    "items": []
                }
            },
            status_code=200
        )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
