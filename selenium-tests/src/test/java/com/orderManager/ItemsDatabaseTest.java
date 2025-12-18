package com.orderManager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for Items Database page functionality
 */
public class ItemsDatabaseTest extends BaseTest {
    
    @Test(priority = 1, description = "Test 1: Verify Items Database navigation link exists")
    public void testItemsDatabaseLinkExists() {
        driver.get(BASE_URL);
        
        WebElement itemsLink = driver.findElement(By.partialLinkText("View Items Database"));
        Assert.assertNotNull(itemsLink, "Items Database link should exist");
        Assert.assertTrue(itemsLink.isDisplayed(), "Items Database link should be visible");
        
        System.out.println("✓ Test 1 Passed: Items Database link exists");
    }
    
    @Test(priority = 2, description = "Test 2: Navigate to Items Database page")
    public void testNavigateToItemsPage() throws InterruptedException {
        driver.get(BASE_URL);
        
        WebElement itemsLink = driver.findElement(By.partialLinkText("View Items Database"));
        itemsLink.click();
        Thread.sleep(2000);
        
        Assert.assertTrue(driver.getCurrentUrl().contains("/items"), "Should navigate to items page");
        Assert.assertTrue(driver.getPageSource().contains("Items Database"), "Should show Items Database heading");
        
        System.out.println("✓ Test 2 Passed: Successfully navigated to Items Database page");
    }
    
    @Test(priority = 3, description = "Test 3: Verify Items Database statistics display")
    public void testItemsDatabaseStatistics() throws InterruptedException {
        driver.get(BASE_URL + "/items");
        Thread.sleep(1000);
        
        String pageSource = driver.getPageSource();
        Assert.assertTrue(pageSource.contains("UNIQUE ITEMS"), "Should display Unique Items stat");
        Assert.assertTrue(pageSource.contains("TOTAL QUANTITY"), "Should display Total Quantity stat");
        Assert.assertTrue(pageSource.contains("TOTAL VALUE"), "Should display Total Value stat");
        
        System.out.println("✓ Test 3 Passed: Items Database statistics display correctly");
    }
    
    @Test(priority = 4, description = "Test 4: Navigate back from Items Database to main page")
    public void testNavigateBackFromItems() throws InterruptedException {
        driver.get(BASE_URL + "/items");
        Thread.sleep(1000);
        
        WebElement backLink = driver.findElement(By.linkText("← Back to Orders"));
        backLink.click();
        Thread.sleep(1000);
        
        Assert.assertTrue(driver.getCurrentUrl().equals(BASE_URL + "/") || 
                         driver.getCurrentUrl().equals(BASE_URL),
                         "Should navigate back to main page");
        
        System.out.println("✓ Test 4 Passed: Navigation back from Items page works");
    }
    
    @Test(priority = 5, description = "Verify items database page title")
    public void testItemsDatabasePageTitle() throws InterruptedException {
        driver.get(BASE_URL + "/items");
        Thread.sleep(1000);
        
        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Items") || title.contains("Database"), 
                         "Page title should reference Items or Database");
        
        System.out.println("✓ Test 5 Passed: Items Database page title correct");
    }
}
