package com.hostel.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User Journey 4 — New User Registration
 *
 * TC-009  A new student can register and is redirected to the student dashboard
 */
public class RegistrationTest extends BaseTest {

    @Test(description = "TC-009: New student registration should redirect to student dashboard")
    public void testStudentRegistration() {
        driver.get(BASE_URL + "/register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reg-name")));

        // Test data — unique email so the test is idempotent across re-runs
        String uniqueEmail = "teststudent_" + generateTimestamp() + "@hostel.com";

        driver.findElement(By.id("reg-name")).sendKeys("Test Student");
        driver.findElement(By.id("reg-email")).sendKeys(uniqueEmail);
        driver.findElement(By.id("reg-password")).sendKeys("password123");

        // Role defaults to STUDENT; enter room number
        driver.findElement(By.id("reg-room")).sendKeys("B-202");

        takeScreenshot("registration_form_filled");

        driver.findElement(By.id("reg-btn")).click();

        // Assertion: should redirect to student dashboard
        wait.until(ExpectedConditions.urlContains("/student"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/student"),
                "New student should be redirected to student dashboard after registration");

        takeScreenshot("registration_success");
    }
}
