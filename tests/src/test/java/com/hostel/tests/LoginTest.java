package com.hostel.tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * TC-001: Login Test
 * Tests student login with valid and invalid credentials
 */
public class LoginTest extends BaseTest {

    private static final String TEST_EMAIL = "student@hostel.com";
    private static final String TEST_PASSWORD = "student123";

    @Test(description = "TC-001: Valid student login should redirect to student dashboard")
    public void testValidStudentLogin() throws Exception {
        driver.get(BASE_URL + "/login");

        // Wait for login page to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));

        // Enter credentials
        driver.findElement(By.id("login-email")).sendKeys(TEST_EMAIL);
        driver.findElement(By.id("login-password")).sendKeys(TEST_PASSWORD);

        // Take screenshot before submit
        takeScreenshot("login_before_submit");

        // Click login
        driver.findElement(By.id("login-btn")).click();

        // Assert: redirect to student dashboard (URL should change)
        wait.until(ExpectedConditions.urlContains("/student"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/student"),
            "Should redirect to student dashboard after login");

        takeScreenshot("login_success");
    }

    @Test(description = "TC-002: Invalid login credentials should show error message")
    public void testInvalidLogin() throws Exception {
        driver.get(BASE_URL + "/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));

        driver.findElement(By.id("login-email")).sendKeys("wrong@email.com");
        driver.findElement(By.id("login-password")).sendKeys("wrongpassword");
        driver.findElement(By.id("login-btn")).click();

        // Assert: error message should appear
        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.className("alert-error")));
        Assert.assertNotNull(errorAlert, "Error message should be displayed for invalid login");
        Assert.assertTrue(errorAlert.isDisplayed(), "Error alert must be visible");

        takeScreenshot("login_invalid_error");
    }

    @Test(description = "TC-003: Admin login should redirect to admin dashboard")
    public void testAdminLogin() throws Exception {
        driver.get(BASE_URL + "/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));

        driver.findElement(By.id("login-email")).sendKeys("admin@hostel.com");
        driver.findElement(By.id("login-password")).sendKeys("admin123");
        driver.findElement(By.id("login-btn")).click();

        wait.until(ExpectedConditions.urlContains("/admin"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin"),
            "Admin should be redirected to admin dashboard");

        takeScreenshot("admin_login_success");
    }

    private void takeScreenshot(String name) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String path = SCREENSHOTS_DIR + "/" + name + "_" + generateTimestamp() + ".png";
            Files.copy(src.toPath(), Paths.get(path));
            System.out.println("Screenshot saved: " + path);
        } catch (Exception e) {
            System.err.println("Screenshot failed: " + e.getMessage());
        }
    }
}
