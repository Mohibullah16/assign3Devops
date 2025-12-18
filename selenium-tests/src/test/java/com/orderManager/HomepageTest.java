package com.orderManager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests for homepage UI elements and basic functionality
 */
public class HomepageTest extends BaseTest {
    
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
        Assert.assertTrue(pageSource.contains("Total Spend") || pageSource.contains("TOTAL SPEND"), 
                         "Should display Total Spend KPI");
        Assert.assertTrue(pageSource.contains("Total Orders") || pageSource.contains("TOTAL ORDERS"), 
                         "Should display Total Orders KPI");
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
    
    @Test(priority = 5, description = "Test 5: Verify add order form can be expanded")
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
        
        System.out.println("✓ Test 5 Passed: Add order form expands correctly");
    }
    
    @Test(priority = 6, description = "Verify page header and branding")
    public void testPageHeaderExists() {
        driver.get(BASE_URL);
        
        String pageSource = driver.getPageSource();
        Assert.assertTrue(pageSource.contains("Order Manager") || pageSource.contains("ORDER MANAGER"), 
                         "Should display application title");
        
        System.out.println("✓ Test 6 Passed: Page header and branding present");
    }
}
