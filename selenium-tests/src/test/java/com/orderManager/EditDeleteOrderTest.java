package com.orderManager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests for editing and deleting orders
 */
public class EditDeleteOrderTest extends BaseTest {
    
    @Test(priority = 1, description = "Test 1: Verify order details can be expanded")
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
            
            System.out.println("✓ Test 1 Passed: Order details expand correctly");
        } else {
            System.out.println("⚠ Test 1 Skipped: No orders with items to expand");
        }
    }
    
    @Test(priority = 2, description = "Test 2: Edit order functionality")
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
            customerField.sendKeys("Updated Customer");
            
            // Submit update
            WebElement updateBtn = driver.findElement(By.xpath("//button[text()='Update Order']"));
            updateBtn.click();
            Thread.sleep(3000);
            
            // Verify update
            Assert.assertTrue(driver.getPageSource().contains("Updated Customer"), 
                             "Customer name should be updated");
            
            System.out.println("✓ Test 2 Passed: Order edited successfully");
        } else {
            System.out.println("⚠ Test 2 Skipped: No orders to edit");
        }
    }
    
    @Test(priority = 3, description = "Test 3: Delete order functionality")
    public void testDeleteOrder() throws InterruptedException {
        driver.get(BASE_URL);
        Thread.sleep(1000);
        
        // Count orders before deletion
        List<WebElement> ordersBefore = driver.findElements(By.className("order-item"));
        int countBefore = ordersBefore.size();
        
        if (countBefore > 0) {
            // Override confirm dialog
            overrideConfirm();
            
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
                
                System.out.println("✓ Test 3 Passed: Order deleted successfully");
            } else {
                System.out.println("⚠ Test 3 Skipped: Delete button not found");
            }
        } else {
            System.out.println("⚠ Test 3 Skipped: No orders to delete");
        }
    }
    
    @Test(priority = 4, description = "Verify edit button visibility")
    public void testEditButtonExists() throws InterruptedException {
        driver.get(BASE_URL);
        Thread.sleep(1000);
        
        List<WebElement> orders = driver.findElements(By.className("order-item"));
        if (orders.size() > 0) {
            List<WebElement> editButtons = driver.findElements(By.linkText("Edit"));
            Assert.assertTrue(editButtons.size() > 0, "Edit buttons should be present");
            System.out.println("✓ Test 4 Passed: Edit buttons are visible");
        } else {
            System.out.println("⚠ Test 4 Skipped: No orders to check");
        }
    }
    
    @Test(priority = 5, description = "Verify delete button visibility")
    public void testDeleteButtonExists() throws InterruptedException {
        driver.get(BASE_URL);
        Thread.sleep(1000);
        
        List<WebElement> orders = driver.findElements(By.className("order-item"));
        if (orders.size() > 0) {
            List<WebElement> deleteButtons = driver.findElements(
                By.xpath("//button[contains(text(), 'Delete')]")
            );
            Assert.assertTrue(deleteButtons.size() > 0, "Delete buttons should be present");
            System.out.println("✓ Test 5 Passed: Delete buttons are visible");
        } else {
            System.out.println("⚠ Test 5 Skipped: No orders to check");
        }
    }
}
