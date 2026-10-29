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
        WebElement titleInput = driver.findElement(By.id("complaint-title"));
        setReactFormValue(titleInput, "WiFi not working in Block A");
        wait.until(d -> "WiFi not working in Block A".equals(
            d.findElement(By.id("complaint-title")).getDomProperty("value")));

        WebElement descriptionInput = driver.findElement(By.id("complaint-description"));
        setReactFormValue(descriptionInput,
            "The WiFi in Block A Room 101 has been down since morning. Need urgent fix.");
        wait.until(ExpectedConditions.attributeToBe(By.id("complaint-description"), "value",
            "The WiFi in Block A Room 101 has been down since morning. Need urgent fix."));

        Select category = new Select(driver.findElement(By.id("complaint-category")));
        category.selectByValue("WIFI");

        Select priority = new Select(driver.findElement(By.id("complaint-priority")));
        priority.selectByValue("HIGH");

        wait.until(d -> "WiFi not working in Block A".equals(
            d.findElement(By.id("complaint-title")).getDomProperty("value")));
        wait.until(d -> "The WiFi in Block A Room 101 has been down since morning. Need urgent fix."
            .equals(d.findElement(By.id("complaint-description")).getDomProperty("value")));

        takeScreenshot("complaint_form_filled");

        // Submit
        ((JavascriptExecutor) driver).executeScript(
            "document.getElementById('complaint-form').requestSubmit(" +
            "document.getElementById('submit-complaint-btn'))");

        WebElement result = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success, .alert-error")));
        Assert.assertTrue(result.getAttribute("class").contains("alert-success"),
            "Complaint submission failed: " + result.getText());
        Assert.assertTrue(result.getText().contains("CMP-"),
            "Success message should contain the complaint ID");

        // The form redirects after confirmation; assert the saved complaint on the destination page.
        wait.until(ExpectedConditions.urlContains("/my-complaints"));
        WebElement complaintRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//tr[.//td[normalize-space()='WiFi not working in Block A']]")));
        Assert.assertTrue(complaintRow.isDisplayed(),
            "The newly submitted complaint should appear in the student's list");

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

    private void setReactFormValue(WebElement field, String value) {
        ((JavascriptExecutor) driver).executeScript(
            "const element = arguments[0];" +
            "const prototype = element instanceof HTMLTextAreaElement " +
                "? HTMLTextAreaElement.prototype : HTMLInputElement.prototype;" +
            "Object.getOwnPropertyDescriptor(prototype, 'value').set.call(element, arguments[1]);" +
            "element.dispatchEvent(new Event('input', { bubbles: true }));" +
            "element.dispatchEvent(new Event('change', { bubbles: true }));",
            field, value);
    }

}
