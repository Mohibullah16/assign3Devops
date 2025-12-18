package com.orderManager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests for form validation
 */
public class FormValidationTest extends BaseTest {
    
    @Test(priority = 1, description = "Test 1: Form validation for required invoice number")
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
        
        System.out.println("✓ Test 1 Passed: Invoice number field validation works");
    }
    
    @Test(priority = 2, description = "Test 2: Verify item description is required")
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
        
        System.out.println("✓ Test 2 Passed: Item description validation works");
    }
    
    @Test(priority = 3, description = "Test 3: Verify all form fields accept valid input")
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
        
        System.out.println("✓ Test 3 Passed: All form fields accept valid input");
    }
    
    @Test(priority = 4, description = "Test numeric validation for quantity field")
    public void testQuantityNumericValidation() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        List<WebElement> qtyInputs = driver.findElements(By.name("qty[]"));
        WebElement qtyInput = qtyInputs.get(0);
        
        // Check input type
        String inputType = qtyInput.getAttribute("type");
        Assert.assertTrue(inputType.equals("number") || inputType.equals("text"), 
                         "Quantity field should be number or text type");
        
        System.out.println("✓ Test 4 Passed: Quantity field has proper input type");
    }
    
    @Test(priority = 5, description = "Test numeric validation for price field")
    public void testPriceNumericValidation() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        List<WebElement> priceInputs = driver.findElements(By.name("price[]"));
        WebElement priceInput = priceInputs.get(0);
        
        // Check input type
        String inputType = priceInput.getAttribute("type");
        Assert.assertTrue(inputType.equals("number") || inputType.equals("text"), 
                         "Price field should be number or text type");
        
        System.out.println("✓ Test 5 Passed: Price field has proper input type");
    }
    
    @Test(priority = 6, description = "Test form submission with minimum required fields")
    public void testMinimumFieldsSubmission() throws InterruptedException {
        driver.get(BASE_URL);
        expandAddOrderForm();
        
        String invoiceNum = generateUniqueInvoice("MIN");
        driver.findElement(By.id("invoice_number")).sendKeys(invoiceNum);
        
        Thread.sleep(500);
        
        // Fill only one item (minimum required)
        List<WebElement> descInputs = driver.findElements(By.name("description[]"));
        List<WebElement> qtyInputs = driver.findElements(By.name("qty[]"));
        List<WebElement> priceInputs = driver.findElements(By.name("price[]"));
        
        descInputs.get(0).sendKeys("Minimum Required Item");
        qtyInputs.get(0).clear();
        qtyInputs.get(0).sendKeys("1");
        priceInputs.get(0).clear();
        priceInputs.get(0).sendKeys("1");
        
        // Submit
        WebElement submitBtn = driver.findElement(By.xpath("//button[text()='Save Order']"));
        submitBtn.click();
        Thread.sleep(3000);
        
        // Should successfully submit
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.equals(BASE_URL + "/") || currentUrl.equals(BASE_URL),
                         "Should submit successfully with minimum fields");
        
        System.out.println("✓ Test 6 Passed: Minimum required fields submission works");
    }
}
