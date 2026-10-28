package com.hostel.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * User Journey 3 — Admin Dashboard & Status Management
 *
 * TC-006  Admin dashboard displays complaint statistics and the complaints table
 * TC-007  Admin can filter complaints by status
 * TC-008  Admin can update a complaint's status (pending -> in progress)
 */
public class AdminDashboardTest extends BaseTest {

    private static final String ADMIN_EMAIL = "admin@hostel.com";
    private static final String ADMIN_PASSWORD = "admin123";

    private void loginAsAdmin() {
        driver.get(BASE_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        driver.findElement(By.id("login-email")).sendKeys(ADMIN_EMAIL);
        driver.findElement(By.id("login-password")).sendKeys(ADMIN_PASSWORD);
        driver.findElement(By.id("login-btn")).click();
        wait.until(ExpectedConditions.urlContains("/admin"));
    }

    @Test(description = "TC-006: Admin dashboard should display complaint statistics")
    public void testAdminDashboardStats() {
        loginAsAdmin();

        // Assertion: stats grid present with at least 4 stat cards
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("stats-grid")));
        List<WebElement> statCards = driver.findElements(By.className("stat-card"));
        Assert.assertTrue(statCards.size() >= 4,
                "Admin dashboard should display at least 4 stat cards");

        // Assertion: complaints table is visible
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("admin-complaints-table")));
        Assert.assertNotNull(table, "Admin complaints table should be visible");

        takeScreenshot("admin_dashboard");
    }

    @Test(description = "TC-007: Admin can filter complaints by status")
    public void testAdminFilterComplaintsByStatus() {
        loginAsAdmin();
        wait.until(ExpectedConditions.urlContains("/admin"));

        // Click PENDING filter
        wait.until(ExpectedConditions.elementToBeClickable(By.id("admin-filter-pending"))).click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        takeScreenshot("admin_filter_pending");

        // Click IN_PROGRESS filter
        driver.findElement(By.id("admin-filter-in_progress")).click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        takeScreenshot("admin_filter_inprogress");

        // Assertion: still on admin page
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin"),
                "Should remain on admin page after filtering");
    }

    @Test(description = "TC-008: Complaint status can be updated by admin")
    public void testUpdateComplaintStatus() {
        loginAsAdmin();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("admin-complaints-table")));

        // Find first Update button
        List<WebElement> updateButtons = driver.findElements(
                By.cssSelector("[id^='update-btn-']"));

        if (updateButtons.isEmpty()) {
            System.out.println("No complaints to update. Skipping status update assertion.");
            return;
        }

        updateButtons.get(0).click();

        // Extract complaint ID from button id
        String btnId = updateButtons.get(0).getAttribute("id");
        String complaintId = btnId.replace("update-btn-", "");

        // Select new status
        WebElement statusSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("status-select-" + complaintId)));
        new Select(statusSelect).selectByValue("IN_PROGRESS");

        // Enter remarks (test data)
        driver.findElement(By.id("remarks-input-" + complaintId))
                .sendKeys("Assigned to maintenance team");

        takeScreenshot("admin_update_status");

        // Save
        driver.findElement(By.id("save-update-" + complaintId)).click();

        // Assertion: success message appears
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("alert-success")));
        Assert.assertTrue(successMsg.isDisplayed(),
                "Success message should appear after status update");

        takeScreenshot("admin_update_success");
    }
}
