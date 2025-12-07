# Sample Test Invoice Images

This directory should contain sample invoice images for testing the OCR functionality.

## Creating Test Invoices

You can create test invoice images similar to the Toy Mart receipt format with the following structure:

### Required Fields:
- **Header:**
  - Invoice Number
  - Date
  - Customer Name
  - Salesman Name

- **Items Table:**
  - Sr# (Serial Number)
  - Code/SKU
  - Description
  - Quantity
  - Unit Price
  - Amount

### Sample Invoice Format:

```
COMPANY NAME
[Date]

Invoice #: 12345
Customer: John Doe
Salesman: Jane Smith

Items:
--------------------------------------------
Sr# | Code    | Description       | Qty | Price | Amount
1   | SKU-001 | Product A        |  2  | 25.00 | 50.00
2   | SKU-002 | Product B        |  1  | 30.00 | 30.00
--------------------------------------------
                        Total Qty: 3
                     Total Amount: 80.00
```

## How to Use

1. Take photos of real invoices/receipts
2. Or create mock invoices using tools like:
   - Microsoft Word
   - Google Docs
   - Canva
   - Excel/Sheets formatted as invoice

3. Save images in this directory
4. Use in tests by uploading through the web interface

## Privacy Note

**Important:** Do not commit real invoice images with sensitive customer data to version control. Use anonymized test data only.

## Supported Formats

- JPG/JPEG
- PNG
- WebP

## Recommended Image Quality

- Minimum resolution: 800x600
- Clear, well-lit images
- Text should be readable
- Avoid blurry or skewed images for best OCR results
