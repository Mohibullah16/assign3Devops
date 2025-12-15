# Part 1: Selenium Automation Report

## 1. Objective
The objective of this part was to implement **automated test cases** for the web application using **Selenium**. The requirements were:
- **Tool**: Selenium (for browser automation).
- **Browser**: Chrome (Headless mode for EC2 compatibility).
- **Test Count**: At least 10 automated test cases.
- **Application**: A web app using a Database Server.
- **Language**: Any language supporting Selenium (Java was chosen).
- **Integration**: Must run in a Jenkins pipeline (Headless).

## 2. Implementation Overview

We implemented **20 automated test cases** using **Java**, **TestNG**, and **Selenium WebDriver**. The project is managed with **Maven**.

### 2.1 Technology Stack
- **Language**: Java 17
- **Test Framework**: TestNG 7.8.0
- **Automation Tool**: Selenium WebDriver 4.16.1
- **Build Tool**: Maven
- **Database**: MongoDB (via `mongodb-driver-sync`)

### 2.2 Project Structure
The project follows a standard Maven structure:
```
selenium-tests/
├── pom.xml                 # Dependencies and build configuration
└── src/test/java/com/orderManager/
    └── OrderManagerTest.java   # Test suite containing all 20 tests
```

## 3. Meeting Requirements (Code Snippets)

### 3.1 Selenium & Chrome Headless Configuration
To ensure compatibility with Jenkins on AWS EC2 (which has no GUI), we configured Chrome to run in **headless mode**.

**File:** `src/test/java/com/orderManager/OrderManagerTest.java`
```java
@BeforeMethod
public void setup() {
    // Setup WebDriverManager to handle ChromeDriver binary
    WebDriverManager.chromedriver().setup();
    
    ChromeOptions options = new ChromeOptions();
    // CRITICAL: Headless mode for server environments
    options.addArguments("--headless"); 
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-gpu");
    options.addArguments("--window-size=1920,1080");
    
    driver = new ChromeDriver(options);
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
}
```

### 3.2 Database Integration
The application uses a Database Server. Our tests integrate with **MongoDB** to handle test data cleanup, ensuring a clean state for each run.

**File:** `src/test/java/com/orderManager/OrderManagerTest.java`
```java
@BeforeSuite
public void setupDatabase() {
    // Clear test database before running tests
    if (MONGO_URI != null && !MONGO_URI.isEmpty()) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase("order_management_test_db");
            database.getCollection("orders").deleteMany(new org.bson.Document());
            System.out.println("✓ Test database cleared");
        }
    }
}
```

### 3.3 Test Cases (>10 Required)
We implemented **20 robust test cases** covering various functionalities:
1.  Homepage Loading
2.  KPI Display
3.  Currency Formatting
4.  Real-time Search
5.  Items Database Navigation
6.  Items Database Stats
7.  Navigation Back
8.  Add Order Form Expansion
9.  Optional Fields Check
10. Add Complete Order
11. Add Order (Multiple Items)
12. Add Order (Empty Optional Fields)
13. Order Details Expansion
14. Real-time Filtering Logic
15. Edit Order
16. Delete Order
17. Validation (Required Invoice #)
18. Validation (Required Description)
19. Input Field Acceptance
20. Form Submission Stability

**Sample Test Case:**
```java
@Test(priority = 11, description = "Test 11: Add a complete order with all fields")
public void testAddCompleteOrder() throws InterruptedException {
    driver.get(BASE_URL);
    expandAddOrderForm();
    
    // Fill header fields
    driver.findElement(By.id("invoice_number")).sendKeys("TEST-JAVA-001");
    driver.findElement(By.id("customer_name")).sendKeys("Java Test Customer");
    
    // Fill item fields (Dynamic)
    List<WebElement> skuInputs = driver.findElements(By.name("sku[]"));
    skuInputs.get(0).sendKeys("SKU-JAVA-001");
    
    // Submit form
    WebElement submitBtn = driver.findElement(By.xpath("//button[text()='Save Order']"));
    submitBtn.click();
    
    // Verify
    String pageSource = driver.getPageSource();
    Assert.assertTrue(pageSource.contains("TEST-JAVA-001"), "Order should appear on page");
}
```

## 4. Execution Results
The tests were executed successfully using `mvn test`.

**Summary:**
```text
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

All 20 tests passed, confirming the application meets the functional requirements and the testing infrastructure is correctly set up for the CI/CD pipeline.
