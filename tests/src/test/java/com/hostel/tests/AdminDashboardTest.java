package com.hostel.tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * TC-006: Admin Dashboard & Status Update Test
 */
public class AdminDashboardTest extends BaseTest {

    private void loginAsAdmin() {
        driver.get(BASE_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        driver.findElement(By.id("login-email")).sendKeys("admin@hostel.com");
        driver.findElement(By.id("login-password")).sendKeys("admin123");
        driver.findElement(By.id("login-btn")).click();
        wait.until(ExpectedConditions.urlContains("/admin"));
    }

    @Test(description = "TC-006: Admin dashboard should display complaint statistics")
    public void testAdminDashboardStats() throws Exception {
        loginAsAdmin();

        // Assert stats grid is present
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("stats-grid")));
        List<WebElement> statCards = driver.findElements(By.className("stat-card"));
        Assert.assertTrue(statCards.size() >= 4,
            "Admin dashboard should display at least 4 stat cards");

        // Assert complaints table is present
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("admin-complaints-table")));
        Assert.assertNotNull(table, "Admin complaints table should be visible");

        takeScreenshot("admin_dashboard");
    }

    @Test(description = "TC-007: Admin can filter complaints by status")
    public void testAdminFilterComplaintsByStatus() throws Exception {
        loginAsAdmin();

        wait.until(ExpectedConditions.urlContains("/admin"));

        // Click PENDING filter
        WebElement pendingFilter = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("admin-filter-pending")));
        pendingFilter.click();

        Thread.sleep(500);
        takeScreenshot("admin_filter_pending");

        // Click IN_PROGRESS filter
        WebElement inProgressFilter = driver.findElement(By.id("admin-filter-in_progress"));
        inProgressFilter.click();

        Thread.sleep(500);
        takeScreenshot("admin_filter_inprogress");

        // Assert we're still on admin page
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin"),
            "Should remain on admin page after filtering");
    }

    @Test(description = "TC-008: Complaint status can be updated by admin (deliberately break/fix test)")
    public void testUpdateComplaintStatus() throws Exception {
        loginAsAdmin();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("admin-complaints-table")));

        // Find first Update button
        List<WebElement> updateButtons = driver.findElements(
            By.cssSelector("[id^='update-btn-']"));

        if (!updateButtons.isEmpty()) {
            updateButtons.get(0).click();

            // Wait for inline update panel to appear
            String btnId = updateButtons.get(0).getAttribute("id");
            String complaintId = btnId.replace("update-btn-", "");

            WebElement statusSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("status-select-" + complaintId)));
            new Select(statusSelect).selectByValue("IN_PROGRESS");

            WebElement remarksInput = driver.findElement(By.id("remarks-input-" + complaintId));
            remarksInput.sendKeys("Assigned to maintenance team");

            takeScreenshot("admin_update_status");

            // Save update
            driver.findElement(By.id("save-update-" + complaintId)).click();

            // Assert success message
            WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("alert-success")));
            Assert.assertTrue(successMsg.isDisplayed(),
                "Success message should appear after status update");

            takeScreenshot("admin_update_success");
        } else {
            System.out.println("No complaints to update. Skipping status update assertion.");
        }
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
