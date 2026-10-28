package com.hostel.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base class for all Selenium tests.
 * - Sets up a headless Chrome WebDriver via WebDriverManager.
 * - Centralised screenshot helper used by every test class.
 * - Automatically captures a screenshot when a test FAILS so that the
 *   failure screenshot mechanism required by Task 9 is always in place.
 */
public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = System.getProperty("app.url", "http://localhost:3000");
    protected static final String SCREENSHOTS_DIR = "test-screenshots";

    @BeforeMethod
    public void setUp() {
        // Auto-download matching ChromeDriver
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");       // Run headless in CI / local
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Create screenshots directory
        new File(SCREENSHOTS_DIR).mkdirs();
    }

    /**
     * Automatically captures a screenshot on test failure, then quits the driver.
     * This is the Task 9 "failure screenshot mechanism".
     */
    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            takeScreenshot("FAILURE_" + result.getName());
            System.err.println(">>> TEST FAILED: " + result.getName()
                    + " — screenshot saved to " + SCREENSHOTS_DIR + "/");
        }
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Capture a full-page screenshot and save it as a timestamped PNG.
     * Called by tests for success evidence AND automatically on failure.
     */
    protected void takeScreenshot(String name) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String path = SCREENSHOTS_DIR + "/" + name + "_" + generateTimestamp() + ".png";
            Files.copy(src.toPath(), Paths.get(path));
            System.out.println("Screenshot saved: " + path);
        } catch (Exception e) {
            System.err.println("Screenshot failed: " + e.getMessage());
        }
    }

    protected String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
