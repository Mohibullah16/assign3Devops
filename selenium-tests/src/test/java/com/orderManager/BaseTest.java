package com.orderManager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.time.Duration;

/**
 * Base test class containing setup, teardown and common utilities
 */
public abstract class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = System.getenv().getOrDefault("APP_URL", "http://localhost:8000");
    protected static final String MONGO_URI = System.getenv("TEST_MONGO_URI");
    
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
    
    /**
     * Helper method to expand add order form
     */
    protected void expandAddOrderForm() throws InterruptedException {
        WebElement details = driver.findElement(By.tagName("details"));
        WebElement summary = details.findElement(By.tagName("summary"));
        String openAttr = details.getAttribute("open");
        if (openAttr == null || !openAttr.equals("true")) {
            summary.click();
            Thread.sleep(500);
        }
    }
    
    /**
     * Helper method to generate unique invoice number
     */
    protected String generateUniqueInvoice(String prefix) {
        return prefix + "-" + System.currentTimeMillis();
    }
    
    /**
     * Helper method to override confirm dialogs
     */
    protected void overrideConfirm() {
        ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("window.confirm = function(){return true;}");
    }
}
