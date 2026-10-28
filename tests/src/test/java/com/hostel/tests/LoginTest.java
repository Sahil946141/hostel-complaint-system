package com.hostel.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User Journey 1 — Authentication / Login
 *
 * TC-001  Valid student login redirects to the student dashboard
 * TC-002  Invalid login credentials show an error message
 * TC-003  Admin login redirects to the admin dashboard
 *
 * TASK 10 — DELIBERATE DEFECT INTRODUCED:
 *   TC-001 uses id="login-btn-BROKEN" (wrong button ID) to simulate a
 *   developer accidentally renaming a UI element.  This causes the test
 *   to fail with NoSuchElementException, which blocks the Deploy stage.
 *   See commit "test(task-10): introduce deliberate defect in LoginTest".
 *   Fixed in commit "fix(task-10): correct broken selector in LoginTest".
 */
public class LoginTest extends BaseTest {

    private static final String STUDENT_EMAIL    = "student@hostel.com";
    private static final String STUDENT_PASSWORD = "student123";
    private static final String ADMIN_EMAIL      = "admin@hostel.com";
    private static final String ADMIN_PASSWORD   = "admin123";

    @Test(description = "TC-001: Valid student login should redirect to student dashboard")
    public void testValidStudentLogin() {
        driver.get(BASE_URL + "/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));

        driver.findElement(By.id("login-email")).sendKeys(STUDENT_EMAIL);
        driver.findElement(By.id("login-password")).sendKeys(STUDENT_PASSWORD);

        takeScreenshot("login_before_submit");

        // ✅  DEFECT FIXED: restored correct button ID (was "login-btn-BROKEN")
        driver.findElement(By.id("login-btn")).click();

        wait.until(ExpectedConditions.urlContains("/student"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/student"),
                "Should redirect to student dashboard after login");

        takeScreenshot("login_success");
    }

    @Test(description = "TC-002: Invalid login credentials should show error message")
    public void testInvalidLogin() {
        driver.get(BASE_URL + "/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));

        driver.findElement(By.id("login-email")).sendKeys("wrong@email.com");
        driver.findElement(By.id("login-password")).sendKeys("wrongpassword");
        driver.findElement(By.id("login-btn")).click();

        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("alert-error")));
        Assert.assertTrue(errorAlert.isDisplayed(),
                "Error message should be displayed for invalid login");

        takeScreenshot("login_invalid_error");
    }

    @Test(description = "TC-003: Admin login should redirect to admin dashboard")
    public void testAdminLogin() {
        driver.get(BASE_URL + "/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));

        driver.findElement(By.id("login-email")).sendKeys(ADMIN_EMAIL);
        driver.findElement(By.id("login-password")).sendKeys(ADMIN_PASSWORD);
        driver.findElement(By.id("login-btn")).click();

        wait.until(ExpectedConditions.urlContains("/admin"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin"),
                "Admin should be redirected to admin dashboard");

        takeScreenshot("admin_login_success");
    }
}
