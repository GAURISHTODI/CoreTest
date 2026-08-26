package com.coretest.mobile;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

/**
 * Mobile Smoke Test using Appium to test the mobile-responsive web frontend
 * running in an Android Emulator browser.
 *
 * Requires a local Appium server running on http://127.0.0.1:4723
 * and an active Android Emulator.
 */
public class MobileSmokeTest {
    private AndroidDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeMethod
    public void setup() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                // Use Chrome browser on the emulator
                .withBrowserName("Chrome");

        URL appiumServerUrl = new URL("http://127.0.0.1:4723");
        
        try {
            driver = new AndroidDriver(appiumServerUrl, options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            // Use 10.0.2.2 which is the Android emulator's alias to host localhost
            baseUrl = System.getProperty("frontend.url", "http://10.0.2.2:5173");
        } catch (Exception e) {
            System.err.println("Failed to connect to Appium Server. Ensure it is running.");
            throw e;
        }
    }

    @Test
    @Feature("Mobile Order Flow")
    @Description("Verify that a user can place an order using a mobile browser")
    public void testMobileOrderFlow() {
        // 1. Load Catalog
        driver.get(baseUrl + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        
        // 2. Click first "Order Now" button
        List<WebElement> orderButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.xpath("//button[contains(text(), 'Order Now')]")));
        Assert.assertFalse(orderButtons.isEmpty(), "No products available to order");
        orderButtons.get(0).click();

        // 3. Place Order
        wait.until(ExpectedConditions.urlContains("/order"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='number']"))).clear();
        driver.findElement(By.cssSelector("input[type='number']")).sendKeys("1");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // 4. Verify Status
        wait.until(ExpectedConditions.urlContains("/status"));
        WebElement statusBadge = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".badge")));
        
        String status = statusBadge.getText();
        Assert.assertTrue(status.equals("PENDING") || status.equals("CONFIRMED"), 
            "Unexpected order status: " + status);
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
