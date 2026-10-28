package com.hostel.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User Journey 2 — Raise a Complaint
 *
 * TC-004  Student raises a complaint and sees the generated CMP- ID
 * TC-005  Empty form submission is blocked by HTML5 validation
 */
public class RaiseComplaintTest extends BaseTest {

    private static final String STUDENT_EMAIL    = "student@hostel.com";
    private static final String STUDENT_PASSWORD = "student123";

    // Test data
    private static final String COMPLAINT_TITLE =
            "WiFi not working in Block A";
    private static final String COMPLAINT_DESCRIPTION =
            "The WiFi in Block A Room 101 has been down since morning. Need urgent fix.";

    private void loginAsStudent() {
        driver.get(BASE_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        driver.findElement(By.id("login-email")).sendKeys(STUDENT_EMAIL);
        driver.findElement(By.id("login-password")).sendKeys(STUDENT_PASSWORD);
        driver.findElement(By.id("login-btn")).click();
        wait.until(ExpectedConditions.urlContains("/student"));
    }

    /**
     * Type into a React controlled input.
     * React fires onChange on every keystroke when the element has focus.
     * Steps: wait until clickable → click to focus → sendKeys character by character.
     * No Ctrl+A / DELETE — the fields start empty so there is nothing to clear.
     */
    private void fillField(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.click();
        el.sendKeys(text);
    }

    @Test(description = "TC-004: Student can raise a complaint successfully")
    public void testRaiseComplaint() {
        loginAsStudent();

        driver.get(BASE_URL + "/raise-complaint");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("complaint-form")));

        // Fill the form — plain sendKeys on focused empty fields fires React onChange
        fillField(By.id("complaint-title"),       COMPLAINT_TITLE);
        fillField(By.id("complaint-description"), COMPLAINT_DESCRIPTION);

        new Select(wait.until(ExpectedConditions.elementToBeClickable(
                By.id("complaint-category")))).selectByValue("WIFI");
        new Select(wait.until(ExpectedConditions.elementToBeClickable(
                By.id("complaint-priority")))).selectByValue("HIGH");

        takeScreenshot("complaint_form_filled");

        driver.findElement(By.id("submit-complaint-btn")).click();

        // Assertion: success alert with a CMP- complaint ID must appear
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("alert-success")));
        Assert.assertTrue(success.getText().contains("CMP-"),
                "Success message should contain complaint ID (CMP-). Actual: "
                + success.getText());

        takeScreenshot("complaint_submitted");
    }

    @Test(description = "TC-005: Empty complaint form submission is blocked by validation")
    public void testComplaintFormValidation() {
        loginAsStudent();
        driver.get(BASE_URL + "/raise-complaint");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("complaint-form")));

        // Click submit without filling any fields
        driver.findElement(By.id("submit-complaint-btn")).click();

        // Assertion: still on raise-complaint page (HTML5 required blocks submit)
        try { Thread.sleep(600); } catch (InterruptedException ignored) {}
        Assert.assertTrue(driver.getCurrentUrl().contains("/raise-complaint"),
                "Form must not submit with empty required fields");

        takeScreenshot("complaint_form_validation");
    }
}
