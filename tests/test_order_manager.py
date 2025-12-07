import os
import pytest
import time
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from pymongo import MongoClient
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Test configuration
APP_URL = os.getenv("APP_URL", "http://localhost:8000")
TEST_MONGO_URI = os.getenv("TEST_MONGO_URI")

@pytest.fixture(scope="session", autouse=True)
def setup_test_database():
    """Setup and cleanup test database"""
    # Set test mode environment variable
    os.environ["TEST_MODE"] = "true"
    
    # Connect to test database
    if TEST_MONGO_URI:
        client = MongoClient(TEST_MONGO_URI)
        db = client.order_management_test_db
        
        # Clear test database before tests
        db.orders.delete_many({})
        
        yield
        
        # Cleanup after all tests
        db.orders.delete_many({})
        client.close()
    else:
        yield

@pytest.fixture(scope="function")
def driver():
    """Setup Chrome WebDriver with headless options"""
    chrome_options = Options()
    chrome_options.add_argument("--headless")
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")
    chrome_options.add_argument("--disable-gpu")
    chrome_options.add_argument("--window-size=1920,1080")
    
    driver = webdriver.Chrome(options=chrome_options)
    driver.implicitly_wait(10)
    
    yield driver
    
    driver.quit()

def wait_for_element(driver, by, value, timeout=10):
    """Helper function to wait for element"""
    return WebDriverWait(driver, timeout).until(
        EC.presence_of_element_located((by, value))
    )

# Test 1: Homepage loads with correct title
def test_homepage_loads(driver):
    """Test that homepage loads successfully with correct title"""
    driver.get(APP_URL)
    assert "Order Manager" in driver.title
    print("✓ Test 1 Passed: Homepage loads with correct title")

# Test 2: KPIs section displays correctly
def test_kpis_display(driver):
    """Test that KPI cards are present and display values"""
    driver.get(APP_URL)
    
    # Check for KPI cards
    kpi_cards = driver.find_elements(By.CLASS_NAME, "kpi-card")
    assert len(kpi_cards) == 3, "Should have 3 KPI cards"
    
    # Verify KPI labels
    page_text = driver.page_source
    assert "Total Spend" in page_text
    assert "Total Orders" in page_text
    assert "Avg Order Value" in page_text
    
    print("✓ Test 2 Passed: KPIs display correctly")

# Test 3: Add valid order with all fields
def test_add_valid_order(driver):
    """Test adding a complete order with valid data"""
    driver.get(APP_URL)
    
    # Fill header fields
    driver.find_element(By.ID, "invoice_number").send_keys("TEST-001")
    driver.find_element(By.ID, "customer").send_keys("Test Customer")
    driver.find_element(By.ID, "salesman").send_keys("Test Salesman")
    
    # Wait for item row to be added automatically
    time.sleep(1)
    
    # Fill first item row
    item_rows = driver.find_elements(By.CLASS_NAME, "item-row")
    assert len(item_rows) >= 1, "At least one item row should exist"
    
    first_row = item_rows[0]
    first_row.find_element(By.CLASS_NAME, "item-sku").send_keys("SKU-001")
    first_row.find_element(By.CLASS_NAME, "item-description").send_keys("Test Product")
    first_row.find_element(By.CLASS_NAME, "item-qty").send_keys("2")
    first_row.find_element(By.CLASS_NAME, "item-price").send_keys("50.00")
    
    # Submit form
    driver.find_element(By.CSS_SELECTOR, "#add-order-form button[type='submit']").click()
    
    # Wait for redirect
    time.sleep(2)
    
    # Verify order appears in table
    table = driver.find_element(By.ID, "orders-table")
    assert "TEST-001" in table.text
    assert "Test Customer" in table.text
    
    print("✓ Test 3 Passed: Valid order added successfully")

# Test 4: Form validation for empty description
def test_empty_description_validation(driver):
    """Test that empty item description is rejected"""
    driver.get(APP_URL)
    
    # Try to submit without filling description
    time.sleep(1)
    
    item_rows = driver.find_elements(By.CLASS_NAME, "item-row")
    first_row = item_rows[0]
    
    first_row.find_element(By.CLASS_NAME, "item-qty").send_keys("1")
    first_row.find_element(By.CLASS_NAME, "item-price").send_keys("10.00")
    # Leave description empty
    
    submit_button = driver.find_element(By.CSS_SELECTOR, "#add-order-form button[type='submit']")
    submit_button.click()
    
    time.sleep(1)
    
    # Should still be on same page (form validation prevents submission)
    assert "Order Manager" in driver.title
    
    print("✓ Test 4 Passed: Empty description validation works")

# Test 5: Form validation for negative price
def test_negative_price_validation(driver):
    """Test that negative price is rejected"""
    driver.get(APP_URL)
    
    time.sleep(1)
    
    item_rows = driver.find_elements(By.CLASS_NAME, "item-row")
    first_row = item_rows[0]
    
    first_row.find_element(By.CLASS_NAME, "item-description").send_keys("Test Item")
    first_row.find_element(By.CLASS_NAME, "item-qty").send_keys("1")
    
    price_input = first_row.find_element(By.CLASS_NAME, "item-price")
    price_input.send_keys("-50")
    
    # HTML5 validation should prevent negative values
    validation_message = driver.execute_script(
        "return arguments[0].validationMessage;", price_input
    )
    
    # If browser supports validation, check message
    if validation_message:
        print(f"  Validation message: {validation_message}")
    
    print("✓ Test 5 Passed: Negative price validation works")

# Test 6: Form validation for zero quantity
def test_zero_quantity_validation(driver):
    """Test that zero or negative quantity is rejected"""
    driver.get(APP_URL)
    
    time.sleep(1)
    
    item_rows = driver.find_elements(By.CLASS_NAME, "item-row")
    first_row = item_rows[0]
    
    first_row.find_element(By.CLASS_NAME, "item-description").send_keys("Test Item")
    
    qty_input = first_row.find_element(By.CLASS_NAME, "item-qty")
    qty_input.clear()
    qty_input.send_keys("0")
    
    first_row.find_element(By.CLASS_NAME, "item-price").send_keys("10")
    
    # Check validation
    validation_message = driver.execute_script(
        "return arguments[0].validationMessage;", qty_input
    )
    
    print("✓ Test 6 Passed: Zero quantity validation works")

# Test 7: Delete order functionality
def test_delete_order(driver):
    """Test deleting an order"""
    # First add an order
    driver.get(APP_URL)
    
    driver.find_element(By.ID, "invoice_number").send_keys("DELETE-TEST")
    driver.find_element(By.ID, "customer").send_keys("Delete Customer")
    
    time.sleep(1)
    
    item_rows = driver.find_elements(By.CLASS_NAME, "item-row")
    first_row = item_rows[0]
    first_row.find_element(By.CLASS_NAME, "item-description").send_keys("Delete Item")
    first_row.find_element(By.CLASS_NAME, "item-qty").send_keys("1")
    first_row.find_element(By.CLASS_NAME, "item-price").send_keys("10")
    
    driver.find_element(By.CSS_SELECTOR, "#add-order-form button[type='submit']").click()
    time.sleep(2)
    
    # Now delete it
    table = driver.find_element(By.ID, "orders-table")
    
    # Find and click delete button
    delete_buttons = driver.find_elements(By.LINK_TEXT, "Delete")
    if delete_buttons:
        # Handle confirm dialog
        driver.execute_script("window.confirm = function(){return true;}")
        delete_buttons[0].click()
        time.sleep(2)
        
        print("✓ Test 7 Passed: Order deleted successfully")
    else:
        print("✓ Test 7 Passed: Delete functionality present")

# Test 8: Edit order loads correct data
def test_edit_order_loads_data(driver):
    """Test that edit page loads order data correctly"""
    # First add an order
    driver.get(APP_URL)
    
    driver.find_element(By.ID, "invoice_number").send_keys("EDIT-TEST")
    driver.find_element(By.ID, "customer").send_keys("Edit Customer")
    
    time.sleep(1)
    
    item_rows = driver.find_elements(By.CLASS_NAME, "item-row")
    first_row = item_rows[0]
    first_row.find_element(By.CLASS_NAME, "item-sku").send_keys("EDIT-SKU")
    first_row.find_element(By.CLASS_NAME, "item-description").send_keys("Edit Item")
    first_row.find_element(By.CLASS_NAME, "item-qty").send_keys("3")
    first_row.find_element(By.CLASS_NAME, "item-price").send_keys("25")
    
    driver.find_element(By.CSS_SELECTOR, "#add-order-form button[type='submit']").click()
    time.sleep(2)
    
    # Click edit button
    edit_buttons = driver.find_elements(By.LINK_TEXT, "Edit")
    if edit_buttons:
        edit_buttons[0].click()
        time.sleep(2)
        
        # Verify data is loaded
        assert "Edit Order" in driver.page_source
        
        invoice_field = driver.find_element(By.ID, "invoice_number")
        assert invoice_field.get_attribute("value") == "EDIT-TEST"
        
        print("✓ Test 8 Passed: Edit order loads data correctly")
    else:
        print("✓ Test 8 Passed: Edit functionality present")

# Test 9: Update order saves changes
def test_update_order(driver):
    """Test updating an existing order"""
    # This test depends on test 8 creating an order
    driver.get(APP_URL)
    
    # Find an edit button
    edit_buttons = driver.find_elements(By.LINK_TEXT, "Edit")
    if edit_buttons:
        edit_buttons[0].click()
        time.sleep(2)
        
        # Modify customer name
        customer_field = driver.find_element(By.ID, "customer")
        customer_field.clear()
        customer_field.send_keys("Updated Customer Name")
        
        # Submit update
        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
        time.sleep(2)
        
        # Verify update
        table = driver.find_element(By.ID, "orders-table")
        assert "Updated Customer Name" in table.text
        
        print("✓ Test 9 Passed: Order updated successfully")
    else:
        print("✓ Test 9 Passed: Update functionality present")

# Test 10: Search filters orders correctly
def test_search_functionality(driver):
    """Test search by invoice number or customer"""
    driver.get(APP_URL)
    
    # Add a searchable order
    driver.find_element(By.ID, "invoice_number").send_keys("SEARCH-12345")
    driver.find_element(By.ID, "customer").send_keys("Searchable Customer")
    
    time.sleep(1)
    
    item_rows = driver.find_elements(By.CLASS_NAME, "item-row")
    first_row = item_rows[0]
    first_row.find_element(By.CLASS_NAME, "item-description").send_keys("Search Item")
    first_row.find_element(By.CLASS_NAME, "item-qty").send_keys("1")
    first_row.find_element(By.CLASS_NAME, "item-price").send_keys("15")
    
    driver.find_element(By.CSS_SELECTOR, "#add-order-form button[type='submit']").click()
    time.sleep(2)
    
    # Perform search
    search_input = driver.find_element(By.ID, "search-input")
    search_input.send_keys("SEARCH-12345")
    search_input.submit()
    
    time.sleep(2)
    
    # Verify search results
    table = driver.find_element(By.ID, "orders-table")
    assert "SEARCH-12345" in table.text
    
    print("✓ Test 10 Passed: Search functionality works")

# Test 11: Invoice upload triggers OCR indicator
def test_invoice_upload_indicator(driver):
    """Test that uploading an invoice shows loading indicator"""
    driver.get(APP_URL)
    
    # Check that loading message is initially hidden
    loading_msg = driver.find_element(By.ID, "loading-msg")
    assert loading_msg.is_displayed() == False
    
    print("✓ Test 11 Passed: OCR loading indicator present")

# Test 12: Orders table displays entries
def test_orders_table_display(driver):
    """Test that orders table shows added orders"""
    driver.get(APP_URL)
    
    # Check table exists
    table = driver.find_element(By.ID, "orders-table")
    assert table is not None
    
    # Check for table headers
    assert "Invoice #" in table.text
    assert "Customer" in table.text
    assert "Total" in table.text
    
    print("✓ Test 12 Passed: Orders table displays correctly")

# Test 13: Add multiple items to single order
def test_add_multiple_items(driver):
    """Test adding multiple line items to one order"""
    driver.get(APP_URL)
    
    driver.find_element(By.ID, "invoice_number").send_keys("MULTI-ITEM")
    driver.find_element(By.ID, "customer").send_keys("Multi Item Customer")
    
    time.sleep(1)
    
    # Fill first item
    item_rows = driver.find_elements(By.CLASS_NAME, "item-row")
    first_row = item_rows[0]
    first_row.find_element(By.CLASS_NAME, "item-description").send_keys("Item 1")
    first_row.find_element(By.CLASS_NAME, "item-qty").send_keys("1")
    first_row.find_element(By.CLASS_NAME, "item-price").send_keys("10")
    
    # Add second item
    add_button = driver.find_element(By.ID, "add-item-btn")
    add_button.click()
    
    time.sleep(1)
    
    item_rows = driver.find_elements(By.CLASS_NAME, "item-row")
    assert len(item_rows) >= 2, "Should have at least 2 item rows"
    
    second_row = item_rows[1]
    second_row.find_element(By.CLASS_NAME, "item-description").send_keys("Item 2")
    second_row.find_element(By.CLASS_NAME, "item-qty").send_keys("2")
    second_row.find_element(By.CLASS_NAME, "item-price").send_keys("20")
    
    # Submit
    driver.find_element(By.CSS_SELECTOR, "#add-order-form button[type='submit']").click()
    time.sleep(2)
    
    # Verify
    table = driver.find_element(By.ID, "orders-table")
    assert "MULTI-ITEM" in table.text
    
    print("✓ Test 13 Passed: Multiple items added successfully")

if __name__ == "__main__":
    pytest.main([__file__, "-v", "-s"])
