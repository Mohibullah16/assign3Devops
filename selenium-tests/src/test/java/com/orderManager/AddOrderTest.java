package com.orderManager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests for adding orders through the form
 */
public class AddOrderTest extends BaseTest {
    
    @Test(priority = 1, description = "Test 1: Verify customer and salesman fields are optional")
    public void testOptionalFields() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        WebElement customerInput = driver.findElement(By.id("customer_name"));
        WebElement salesmanInput = driver.findElement(By.id("salesman_name"));
        
        // Check that fields don't have 'required' attribute
        String customerRequired = customerInput.getAttribute("required");
        String salesmanRequired = salesmanInput.getAttribute("required");
        
        Assert.assertNull(customerRequired, "Customer field should be optional");
        Assert.assertNull(salesmanRequired, "Salesman field should be optional");
        
        System.out.println("✓ Test 1 Passed: Customer and salesman fields are optional");
    }
    
    @Test(priority = 2, description = "Test 2: Add a complete order with all fields")
    public void testAddCompleteOrder() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        String invoiceNum = generateUniqueInvoice("COMPLETE");
        
        // Fill header fields
        driver.findElement(By.id("invoice_number")).sendKeys(invoiceNum);
        driver.findElement(By.id("customer_name")).sendKeys("Complete Test Customer");
        driver.findElement(By.id("salesman_name")).sendKeys("Complete Test Salesman");
        
        Thread.sleep(500);
        
        // Fill item fields
        List<WebElement> skuInputs = driver.findElements(By.name("sku[]"));
        List<WebElement> descInputs = driver.findElements(By.name("description[]"));
        List<WebElement> qtyInputs = driver.findElements(By.name("qty[]"));
        List<WebElement> priceInputs = driver.findElements(By.name("price[]"));
        
        Assert.assertTrue(skuInputs.size() >= 1, "Should have at least one item row");
        
        skuInputs.get(0).sendKeys("SKU-COMPLETE-001");
        descInputs.get(0).sendKeys("Complete Test Product");
        qtyInputs.get(0).clear();
        qtyInputs.get(0).sendKeys("2");
        priceInputs.get(0).sendKeys("150.00");
        
        // Submit form
        WebElement submitBtn = driver.findElement(By.xpath("//button[text()='Save Order']"));
        submitBtn.click();
        Thread.sleep(3000);
        
        // Verify order appears on page
        String pageSource = driver.getPageSource();
        Assert.assertTrue(pageSource.contains(invoiceNum), "Order should appear on page");
        Assert.assertTrue(pageSource.contains("Complete Test Customer"), "Customer name should appear");
        
        System.out.println("✓ Test 2 Passed: Complete order added successfully");
    }
    
    @Test(priority = 3, description = "Test 3: Add order with multiple items")
    public void testAddOrderWithMultipleItems() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        String invoiceNum = generateUniqueInvoice("MULTI");
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
        Thread.sleep(3000);
        
        // Verify successful submission
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.equals(BASE_URL + "/") || currentUrl.equals(BASE_URL),
                         "Should redirect to homepage after submission");
        
        System.out.println("✓ Test 3 Passed: Order with multiple items submitted successfully");
    }
    
    @Test(priority = 4, description = "Test 4: Add order with optional fields empty")
    public void testAddOrderWithOptionalFieldsEmpty() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        String invoiceNum = generateUniqueInvoice("OPTIONAL");
        driver.findElement(By.id("invoice_number")).sendKeys(invoiceNum);
        
        Thread.sleep(500);
        
        // Fill only item (no customer/salesman)
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
        Thread.sleep(3000);
        
        // Verify successful submission
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.equals(BASE_URL + "/") || currentUrl.equals(BASE_URL),
                         "Should redirect to homepage after submission");
        
        System.out.println("✓ Test 4 Passed: Order with optional fields empty submitted successfully");
    }
    
    @Test(priority = 5, description = "Test add item button functionality")
    public void testAddItemButton() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        // Count initial item rows
        List<WebElement> initialRows = driver.findElements(By.name("description[]"));
        int initialCount = initialRows.size();
        
        // Click Add Item button
        WebElement addItemBtn = driver.findElement(By.xpath("//button[contains(text(), '+ Add Item')]"));
        addItemBtn.click();
        Thread.sleep(500);
        
        // Count rows after adding
        List<WebElement> afterRows = driver.findElements(By.name("description[]"));
        Assert.assertTrue(afterRows.size() > initialCount, "Should add a new item row");
        
        System.out.println("✓ Test 5 Passed: Add item button works correctly");
    }
}
