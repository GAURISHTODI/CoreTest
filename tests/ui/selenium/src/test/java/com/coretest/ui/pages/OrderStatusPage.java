package com.coretest.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class OrderStatusPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By statusBadge = By.cssSelector(".badge");
    private final By orderIdSpan = By.xpath("//strong[text()='Order ID:']/following-sibling::span");

    public OrderStatusPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1"))).getText().equals("Order Status");
    }

    public String getStatus() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(statusBadge)).getText();
    }

    public String getOrderId() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(orderIdSpan)).getText();
    }
}
