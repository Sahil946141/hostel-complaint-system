package com.hostel.tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * TC-004: Raise Complaint Test
 * Tests the complete complaint submission flow
 */
public class RaiseComplaintTest extends BaseTest {

    private static final String STUDENT_EMAIL = "student@hostel.com";
    private static final String STUDENT_PASSWORD = "student123";

    private void loginAsStudent() {
        driver.get(BASE_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        driver.findElement(By.id("login-email")).sendKeys(STUDENT_EMAIL);
        driver.findElement(By.id("login-password")).sendKeys(STUDENT_PASSWORD);
        driver.findElement(By.id("login-btn")).click();
        wait.until(ExpectedConditions.urlContains("/student"));
    }

    @Test(description = "TC-004: Student can raise a complaint successfully")
    public void testRaiseComplaint() throws Exception {
        loginAsStudent();

        // Navigate to raise complaint
        driver.get(BASE_URL + "/raise-complaint");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("complaint-form")));

        // Fill form
        driver.findElement(By.id("complaint-title")).sendKeys("WiFi not working in Block A");
        driver.findElement(By.id("complaint-description")).sendKeys(
            "The WiFi in Block A Room 101 has been down since morning. Need urgent fix.");

        Select category = new Select(driver.findElement(By.id("complaint-category")));
        category.selectByValue("WIFI");

        Select priority = new Select(driver.findElement(By.id("complaint-priority")));
        priority.selectByValue("HIGH");

        takeScreenshot("complaint_form_filled");

        // Submit
        driver.findElement(By.id("submit-complaint-btn")).click();

        // Assert: success message appears
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.className("alert-success")));
        Assert.assertTrue(success.getText().contains("CMP-"),
            "Success message should contain complaint ID");

        takeScreenshot("complaint_submitted");
    }

    @Test(description = "TC-005: Complaint form validation - empty submission should fail")
    public void testComplaintFormValidation() throws Exception {
        loginAsStudent();
        driver.get(BASE_URL + "/raise-complaint");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("complaint-form")));

        // Try to submit empty form
        driver.findElement(By.id("submit-complaint-btn")).click();

        // Assert: HTML5 validation prevents submission (URL should NOT change)
        Thread.sleep(500);
        Assert.assertTrue(driver.getCurrentUrl().contains("/raise-complaint"),
            "Form should not submit with empty required fields");

        takeScreenshot("complaint_form_validation");
    }

    private void takeScreenshot(String name) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String path = SCREENSHOTS_DIR + "/" + name + "_" + generateTimestamp() + ".png";
            Files.copy(src.toPath(), Paths.get(path));
        } catch (Exception e) {
            System.err.println("Screenshot failed: " + e.getMessage());
        }
    }
}
