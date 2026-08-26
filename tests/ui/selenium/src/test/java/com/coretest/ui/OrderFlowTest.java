package com.coretest.ui;

import com.coretest.ui.pages.OrderStatusPage;
import com.coretest.ui.pages.PlaceOrderPage;
import com.coretest.ui.pages.ProductListPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OrderFlowTest {
    private WebDriver driver;
    private String baseUrl;

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Run headless for CI
        driver = new ChromeDriver(options);
        baseUrl = System.getProperty("frontend.url", "http://localhost:5173");
    }

    @Test
    @Feature("End-to-End Order Flow")
    @Description("Verify that a user can browse products, place an order, and see the confirmation status")
    public void testCompleteOrderFlow() {
        // 1. Product List
        ProductListPage productList = new ProductListPage(driver);
        productList.navigateTo(baseUrl);
        Assert.assertTrue(productList.isLoaded(), "Product list page did not load");
        Assert.assertTrue(productList.getProductCount() > 0, "No products found");

        // Click on the first available product to order
        productList.clickFirstAvailableOrderButton();

        // 2. Place Order
        PlaceOrderPage placeOrder = new PlaceOrderPage(driver);
        Assert.assertTrue(placeOrder.isLoaded(), "Place order page did not load");
        
        // Wait for products to load in dropdown and select (if not auto-selected via URL params)
        placeOrder.setQuantity(2);
        placeOrder.submitOrder();

        // 3. Order Status
        OrderStatusPage orderStatus = new OrderStatusPage(driver);
        Assert.assertTrue(orderStatus.isLoaded(), "Order status page did not load");
        
        String status = orderStatus.getStatus();
        Assert.assertTrue(status.equals("PENDING") || status.equals("CONFIRMED"), 
            "Unexpected order status: " + status);
        Assert.assertFalse(orderStatus.getOrderId().isEmpty(), "Order ID should not be empty");
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
