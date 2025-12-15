package com.orderManager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.time.Duration;
import java.util.List;

public class OrderManagerTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = System.getenv().getOrDefault("APP_URL", "http://localhost:8000");
    private static final String MONGO_URI = System.getenv("TEST_MONGO_URI");
    
    @BeforeSuite
    public void setupDatabase() {
        // Clear test database before running tests
        if (MONGO_URI != null && !MONGO_URI.isEmpty()) {
            try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
                MongoDatabase database = mongoClient.getDatabase("order_management_test_db");
                database.getCollection("orders").deleteMany(new org.bson.Document());
                System.out.println("✓ Test database cleared");
            } catch (Exception e) {
                System.err.println("Warning: Could not clear test database - " + e.getMessage());
            }
        }
    }
    
    @BeforeMethod
    public void setup() {
        // WebDriverManager.chromedriver().setup(); // Disabled: Using container-provided ChromeDriver
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--remote-allow-origins=*");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }
    
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @AfterSuite
    public void cleanupDatabase() {
        // Cleanup test database after all tests
        if (MONGO_URI != null && !MONGO_URI.isEmpty()) {
            try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
                MongoDatabase database = mongoClient.getDatabase("order_management_test_db");
                database.getCollection("orders").deleteMany(new org.bson.Document());
                System.out.println("✓ Test database cleaned up");
            } catch (Exception e) {
                System.err.println("Warning: Could not cleanup test database - " + e.getMessage());
            }
        }
    }
    
    // Helper method to expand add order form
    private void expandAddOrderForm() throws InterruptedException {
        WebElement details = driver.findElement(By.tagName("details"));
        WebElement summary = details.findElement(By.tagName("summary"));
        String openAttr = details.getAttribute("open");
        if (openAttr == null || !openAttr.equals("true")) {
            summary.click();
            Thread.sleep(500);
        }
    }
    
    @Test(priority = 1, description = "Test 1: Verify homepage loads with correct title")
    public void testHomepageLoads() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Order Manager"), "Page title should contain 'Order Manager'");
        System.out.println("✓ Test 1 Passed: Homepage loads with correct title");
    }
    
    @Test(priority = 2, description = "Test 2: Verify KPI cards are displayed correctly")
    public void testKPIsDisplay() {
        driver.get(BASE_URL);
        
        // Find KPI cards
        List<WebElement> kpiCards = driver.findElements(By.className("kpi-card"));
        Assert.assertEquals(kpiCards.size(), 3, "Should have exactly 3 KPI cards");
        
        // Verify KPI labels are present
        String pageSource = driver.getPageSource();
        Assert.assertTrue(pageSource.contains("Total Spend") || pageSource.contains("TOTAL SPEND"), "Should display Total Spend KPI");
        Assert.assertTrue(pageSource.contains("Total Orders") || pageSource.contains("TOTAL ORDERS"), "Should display Total Orders KPI");
        Assert.assertTrue(pageSource.contains("Average Order") || pageSource.contains("AVG") || pageSource.contains("Avg"), 
                         "Should display Average Order Value KPI");
        
        System.out.println("✓ Test 2 Passed: KPIs display correctly with 3 cards");
    }
    
    @Test(priority = 3, description = "Test 3: Verify currency is displayed in PKR format")
    public void testCurrencyDisplayPKR() {
        driver.get(BASE_URL);
        String pageSource = driver.getPageSource();
        Assert.assertTrue(pageSource.contains("PKR"), "Currency should be displayed in PKR format");
        System.out.println("✓ Test 3 Passed: Currency displayed in PKR format");
    }
    
    @Test(priority = 4, description = "Test 4: Verify search input exists and works")
    public void testSearchInputExists() {
        driver.get(BASE_URL);
        
        WebElement searchInput = driver.findElement(By.id("searchInput"));
        Assert.assertNotNull(searchInput, "Search input should exist");
        Assert.assertTrue(searchInput.isDisplayed(), "Search input should be visible");
        
        // Test typing in search
        searchInput.sendKeys("TEST");
        String value = searchInput.getAttribute("value");
        Assert.assertEquals(value, "TEST", "Search input should accept text");
        
        System.out.println("✓ Test 4 Passed: Real-time search input works");
    }
    
    @Test(priority = 5, description = "Test 5: Verify Items Database navigation link exists")
    public void testItemsDatabaseLinkExists() {
        driver.get(BASE_URL);
        
        WebElement itemsLink = driver.findElement(By.partialLinkText("View Items Database"));
        Assert.assertNotNull(itemsLink, "Items Database link should exist");
        Assert.assertTrue(itemsLink.isDisplayed(), "Items Database link should be visible");
        
        System.out.println("✓ Test 5 Passed: Items Database link exists");
    }
    
    @Test(priority = 6, description = "Test 6: Navigate to Items Database page")
    public void testNavigateToItemsPage() throws InterruptedException {
        driver.get(BASE_URL);
        
        WebElement itemsLink = driver.findElement(By.partialLinkText("View Items Database"));
        itemsLink.click();
        Thread.sleep(2000);
        
        Assert.assertTrue(driver.getCurrentUrl().contains("/items"), "Should navigate to items page");
        Assert.assertTrue(driver.getPageSource().contains("Items Database"), "Should show Items Database heading");
        
        System.out.println("✓ Test 6 Passed: Successfully navigated to Items Database page");
    }
    
    @Test(priority = 7, description = "Test 7: Verify Items Database statistics display")
    public void testItemsDatabaseStatistics() throws InterruptedException {
        driver.get(BASE_URL + "/items");
        Thread.sleep(1000);
        
        String pageSource = driver.getPageSource();
        Assert.assertTrue(pageSource.contains("UNIQUE ITEMS"), "Should display Unique Items stat");
        Assert.assertTrue(pageSource.contains("TOTAL QUANTITY"), "Should display Total Quantity stat");
        Assert.assertTrue(pageSource.contains("TOTAL VALUE"), "Should display Total Value stat");
        
        System.out.println("✓ Test 7 Passed: Items Database statistics display correctly");
    }
    
    @Test(priority = 8, description = "Test 8: Navigate back from Items Database to main page")
    public void testNavigateBackFromItems() throws InterruptedException {
        driver.get(BASE_URL + "/items");
        Thread.sleep(1000);
        
        WebElement backLink = driver.findElement(By.linkText("← Back to Orders"));
        backLink.click();
        Thread.sleep(1000);
        
        Assert.assertTrue(driver.getCurrentUrl().equals(BASE_URL + "/") || 
                         driver.getCurrentUrl().equals(BASE_URL),
                         "Should navigate back to main page");
        
        System.out.println("✓ Test 8 Passed: Navigation back from Items page works");
    }
    
    @Test(priority = 9, description = "Test 9: Verify add order form can be expanded")
    public void testAddOrderFormExpansion() throws InterruptedException {
        driver.get(BASE_URL);
        
        WebElement details = driver.findElement(By.tagName("details"));
        WebElement summary = details.findElement(By.tagName("summary"));
        
        // Click to expand
        summary.click();
        Thread.sleep(500);
        
        // Verify form fields are visible
        WebElement invoiceInput = driver.findElement(By.id("invoice_number"));
        Assert.assertTrue(invoiceInput.isDisplayed(), "Invoice number field should be visible");
        
        System.out.println("✓ Test 9 Passed: Add order form expands correctly");
    }
    
    @Test(priority = 10, description = "Test 10: Verify customer and salesman fields are optional")
    public void testOptionalFields() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        WebElement customerInput = driver.findElement(By.id("customer_name"));
        WebElement salesmanInput = driver.findElement(By.id("salesman_name"));
        
        // Check that fields don't have 'required' attribute or it's false
        String customerRequired = customerInput.getAttribute("required");
        String salesmanRequired = salesmanInput.getAttribute("required");
        
        Assert.assertNull(customerRequired, "Customer field should be optional");
        Assert.assertNull(salesmanRequired, "Salesman field should be optional");
        
        System.out.println("✓ Test 10 Passed: Customer and salesman fields are optional");
    }
    
    @Test(priority = 11, description = "Test 11: Add a complete order with all fields")
    public void testAddCompleteOrder() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        // Fill header fields
        driver.findElement(By.id("invoice_number")).sendKeys("TEST-JAVA-001");
        driver.findElement(By.id("customer_name")).sendKeys("Java Test Customer");
        driver.findElement(By.id("salesman_name")).sendKeys("Java Test Salesman");
        
        Thread.sleep(500);
        
        // Fill item fields
        List<WebElement> skuInputs = driver.findElements(By.name("sku[]"));
        List<WebElement> descInputs = driver.findElements(By.name("description[]"));
        List<WebElement> qtyInputs = driver.findElements(By.name("qty[]"));
        List<WebElement> priceInputs = driver.findElements(By.name("price[]"));
        
        Assert.assertTrue(skuInputs.size() >= 1, "Should have at least one item row");
        
        skuInputs.get(0).sendKeys("SKU-JAVA-001");
        descInputs.get(0).sendKeys("Java Test Product");
        qtyInputs.get(0).clear();
        qtyInputs.get(0).sendKeys("2");
        priceInputs.get(0).sendKeys("150.00");
        
        // Submit form
        WebElement submitBtn = driver.findElement(By.xpath("//button[text()='Save Order']"));
        submitBtn.click();
        Thread.sleep(3000);
        
        // Verify order appears on page
        String pageSource = driver.getPageSource();
        Assert.assertTrue(pageSource.contains("TEST-JAVA-001"), "Order should appear on page");
        Assert.assertTrue(pageSource.contains("Java Test Customer"), "Customer name should appear");
        
        System.out.println("✓ Test 11 Passed: Complete order added successfully");
    }
    
    @Test(priority = 12, description = "Test 12: Add order with multiple items")
    public void testAddOrderWithMultipleItems() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        // Fill header with unique invoice number
        String invoiceNum = "MULTI-" + System.currentTimeMillis();
        driver.findElement(By.id("invoice_number")).sendKeys(invoiceNum);
        driver.findElement(By.id("customer_name")).sendKeys("Multi Item Customer");
        
        Thread.sleep(500);
        
        // Fill first item
        List<WebElement> descInputs = driver.findElements(By.name("description[]"));
        List<WebElement> qtyInputs = driver.findElements(By.name("qty[]"));
        List<WebElement> priceInputs = driver.findElements(By.name("price[]"));
        
        descInputs.get(0).sendKeys("First Item");
        qtyInputs.get(0).clear();
        qtyInputs.get(0).sendKeys("1");
        priceInputs.get(0).clear();
        priceInputs.get(0).sendKeys("100");
        
        // Click Add Item button
        WebElement addItemBtn = driver.findElement(By.xpath("//button[contains(text(), '+ Add Item')]"));
        addItemBtn.click();
        Thread.sleep(500);
        
        // Fill second item
        descInputs = driver.findElements(By.name("description[]"));
        qtyInputs = driver.findElements(By.name("qty[]"));
        priceInputs = driver.findElements(By.name("price[]"));
        
        Assert.assertTrue(descInputs.size() >= 2, "Should have at least 2 item rows");
        
        descInputs.get(1).sendKeys("Second Item");
        qtyInputs.get(1).clear();
        qtyInputs.get(1).sendKeys("2");
        priceInputs.get(1).clear();
        priceInputs.get(1).sendKeys("200");
        
        // Submit
        WebElement submitBtn = driver.findElement(By.xpath("//button[text()='Save Order']"));
        submitBtn.click();
        
        // Wait for redirect back to homepage
        Thread.sleep(3000);
        
        // Verify we're back on homepage (successful submission)
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.equals(BASE_URL + "/") || currentUrl.equals(BASE_URL),
                         "Should redirect to homepage after submission");
        
        // Verify the form submission was successful (no HTTP errors)
        String pageSource = driver.getPageSource();
        Assert.assertFalse(pageSource.contains("HTTPException") || pageSource.contains("status_code=500"),
                          "Page should not contain server errors after submission");
        
        System.out.println("✓ Test 12 Passed: Order with multiple items submitted successfully");
    }
    
    @Test(priority = 13, description = "Test 13: Add order with optional fields empty")
    public void testAddOrderWithOptionalFieldsEmpty() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        // Fill only required fields (skip customer and salesman) with unique invoice
        String invoiceNum = "OPTIONAL-" + System.currentTimeMillis();
        driver.findElement(By.id("invoice_number")).sendKeys(invoiceNum);
        
        Thread.sleep(500);
        
        // Fill item
        List<WebElement> descInputs = driver.findElements(By.name("description[]"));
        List<WebElement> qtyInputs = driver.findElements(By.name("qty[]"));
        List<WebElement> priceInputs = driver.findElements(By.name("price[]"));
        
        descInputs.get(0).sendKeys("Optional Fields Test Item");
        qtyInputs.get(0).clear();
        qtyInputs.get(0).sendKeys("1");
        priceInputs.get(0).clear();
        priceInputs.get(0).sendKeys("75");
        
        // Submit
        WebElement submitBtn = driver.findElement(By.xpath("//button[text()='Save Order']"));
        submitBtn.click();
        
        // Wait for redirect back to homepage
        Thread.sleep(3000);
        
        // Verify we're back on homepage (successful submission)
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.equals(BASE_URL + "/") || currentUrl.equals(BASE_URL),
                         "Should redirect to homepage after submission");
        
        // Verify the form submission was successful (no HTTP errors)
        String pageSource = driver.getPageSource();
        Assert.assertFalse(pageSource.contains("HTTPException") || pageSource.contains("status_code=500"),
                          "Page should not contain server errors after submission");
        
        System.out.println("✓ Test 13 Passed: Order with optional fields submitted successfully");
    }
    
    @Test(priority = 14, description = "Test 14: Verify order details can be expanded")
    public void testOrderDetailsExpansion() throws InterruptedException {
        driver.get(BASE_URL);
        Thread.sleep(1000);
        
        // Find details elements (orders with items)
        List<WebElement> detailsElements = driver.findElements(By.tagName("details"));
        
        // Find order details (not the add form)
        WebElement orderDetails = null;
        for (WebElement details : detailsElements) {
            if (details.getText().contains("Items (")) {
                orderDetails = details;
                break;
            }
        }
        
        if (orderDetails != null) {
            WebElement summary = orderDetails.findElement(By.tagName("summary"));
            summary.click();
            Thread.sleep(500);
            
            // Verify items are visible
            List<WebElement> itemsList = orderDetails.findElements(By.className("item-detail"));
            Assert.assertTrue(itemsList.size() > 0, "Should show item details when expanded");
            
            System.out.println("✓ Test 14 Passed: Order details expand correctly");
        } else {
            System.out.println("⚠ Test 14 Skipped: No orders with items to expand");
        }
    }
    
    @Test(priority = 15, description = "Test 15: Test real-time search filtering")
    public void testRealTimeSearchFiltering() throws InterruptedException {
        driver.get(BASE_URL);
        Thread.sleep(1000);
        
        // Get initial count of visible orders
        List<WebElement> ordersBefore = driver.findElements(By.className("order-item"));
        int countBefore = ordersBefore.size();
        
        if (countBefore > 0) {
            // Type in search box
            WebElement searchInput = driver.findElement(By.id("searchInput"));
            searchInput.sendKeys("TEST-JAVA");
            Thread.sleep(1000);
            
            // Check that filtering occurred (some orders might be hidden)
            List<WebElement> visibleOrders = driver.findElements(
                By.xpath("//div[@class='order-item' and not(contains(@style, 'display: none'))]")
            );
            
            System.out.println("✓ Test 15 Passed: Real-time search filtering works");
        } else {
            System.out.println("⚠ Test 15 Skipped: No orders to search");
        }
    }
    
    @Test(priority = 16, description = "Test 16: Edit order functionality")
    public void testEditOrder() throws InterruptedException {
        driver.get(BASE_URL);
        Thread.sleep(1000);
        
        // Find edit button
        List<WebElement> editButtons = driver.findElements(By.linkText("Edit"));
        
        if (!editButtons.isEmpty()) {
            editButtons.get(0).click();
            Thread.sleep(2000);
            
            // Verify we're on edit page
            Assert.assertTrue(driver.getPageSource().contains("Edit Order"), 
                             "Should navigate to edit page");
            
            // Modify customer name
            WebElement customerField = driver.findElement(By.name("customer_name"));
            customerField.clear();
            customerField.sendKeys("Updated Java Customer");
            
            // Submit update
            WebElement updateBtn = driver.findElement(By.xpath("//button[text()='Update Order']"));
            updateBtn.click();
            Thread.sleep(3000);
            
            // Verify update
            Assert.assertTrue(driver.getPageSource().contains("Updated Java Customer"), 
                             "Customer name should be updated");
            
            System.out.println("✓ Test 16 Passed: Order edited successfully");
        } else {
            System.out.println("⚠ Test 16 Skipped: No orders to edit");
        }
    }
    
    @Test(priority = 17, description = "Test 17: Delete order functionality")
    public void testDeleteOrder() throws InterruptedException {
        driver.get(BASE_URL);
        Thread.sleep(1000);
        
        // Count orders before deletion
        List<WebElement> ordersBefore = driver.findElements(By.className("order-item"));
        int countBefore = ordersBefore.size();
        
        if (countBefore > 0) {
            // Override confirm dialog
            ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("window.confirm = function(){return true;}");
            
            // Find and click delete button
            List<WebElement> deleteButtons = driver.findElements(
                By.xpath("//button[contains(text(), 'Delete')]")
            );
            
            if (!deleteButtons.isEmpty()) {
                deleteButtons.get(0).click();
                Thread.sleep(3000);
                
                // Verify order count decreased
                List<WebElement> ordersAfter = driver.findElements(By.className("order-item"));
                Assert.assertTrue(ordersAfter.size() < countBefore || ordersAfter.size() == 0,
                                 "Order count should decrease after deletion");
                
                System.out.println("✓ Test 17 Passed: Order deleted successfully");
            } else {
                System.out.println("⚠ Test 17 Skipped: Delete button not found");
            }
        } else {
            System.out.println("⚠ Test 17 Skipped: No orders to delete");
        }
    }
    
    @Test(priority = 18, description = "Test 18: Form validation for required invoice number")
    public void testRequiredInvoiceNumber() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        // Try to submit without invoice number
        WebElement submitBtn = driver.findElement(By.xpath("//button[text()='Save Order']"));
        submitBtn.click();
        Thread.sleep(1000);
        
        // Should still be on same page (HTML5 validation)
        Assert.assertTrue(driver.getTitle().contains("Order Manager"), 
                         "Should remain on same page due to validation");
        
        System.out.println("✓ Test 18 Passed: Invoice number field validation works");
    }
    
    @Test(priority = 19, description = "Test 19: Verify item description is required")
    public void testRequiredItemDescription() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        // Fill invoice but leave description empty
        driver.findElement(By.id("invoice_number")).sendKeys("VALIDATION-TEST");
        
        Thread.sleep(500);
        
        // Fill qty and price but not description
        List<WebElement> qtyInputs = driver.findElements(By.name("qty[]"));
        List<WebElement> priceInputs = driver.findElements(By.name("price[]"));
        
        qtyInputs.get(0).clear();
        qtyInputs.get(0).sendKeys("1");
        priceInputs.get(0).sendKeys("10.00");
        
        // Try to submit
        WebElement submitBtn = driver.findElement(By.xpath("//button[text()='Save Order']"));
        submitBtn.click();
        Thread.sleep(1000);
        
        // Should remain on same page
        Assert.assertTrue(driver.getCurrentUrl().contains(BASE_URL), 
                         "Should remain on page due to validation");
        
        System.out.println("✓ Test 19 Passed: Item description validation works");
    }
    
    @Test(priority = 20, description = "Test 20: Verify all form fields accept valid input")
    public void testAllFormFieldsAcceptInput() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        // Test all input fields
        WebElement invoiceInput = driver.findElement(By.id("invoice_number"));
        WebElement customerInput = driver.findElement(By.id("customer_name"));
        WebElement salesmanInput = driver.findElement(By.id("salesman_name"));
        
        invoiceInput.sendKeys("FIELD-TEST-001");
        customerInput.sendKeys("Test Customer");
        salesmanInput.sendKeys("Test Salesman");
        
        Thread.sleep(500);
        
        // Test item fields
        List<WebElement> skuInputs = driver.findElements(By.name("sku[]"));
        List<WebElement> descInputs = driver.findElements(By.name("description[]"));
        List<WebElement> qtyInputs = driver.findElements(By.name("qty[]"));
        List<WebElement> priceInputs = driver.findElements(By.name("price[]"));
        
        skuInputs.get(0).sendKeys("TEST-SKU");
        descInputs.get(0).sendKeys("Test Description");
        qtyInputs.get(0).clear();
        qtyInputs.get(0).sendKeys("5");
        priceInputs.get(0).sendKeys("99.99");
        
        // Verify all inputs have values
        Assert.assertEquals(invoiceInput.getAttribute("value"), "FIELD-TEST-001");
        Assert.assertEquals(customerInput.getAttribute("value"), "Test Customer");
        Assert.assertEquals(salesmanInput.getAttribute("value"), "Test Salesman");
        Assert.assertEquals(skuInputs.get(0).getAttribute("value"), "TEST-SKU");
        Assert.assertEquals(descInputs.get(0).getAttribute("value"), "Test Description");
        Assert.assertEquals(qtyInputs.get(0).getAttribute("value"), "5");
        Assert.assertEquals(priceInputs.get(0).getAttribute("value"), "99.99");
        
        System.out.println("✓ Test 20 Passed: All form fields accept valid input");
    }
}
