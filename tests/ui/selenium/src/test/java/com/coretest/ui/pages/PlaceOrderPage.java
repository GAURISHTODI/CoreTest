package com.coretest.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PlaceOrderPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By productSelect = By.cssSelector("select");
    private final By quantityInput = By.cssSelector("input[type='number']");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By errorBadge = By.cssSelector(".badge.danger");

    public PlaceOrderPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1"))).getText().equals("Place Order");
    }

    public void selectFirstProduct() {
        wait.until(ExpectedConditions.presenceOfElementLocated(productSelect));
        // Simple selection if not using Select class
        driver.findElements(By.cssSelector("select option")).get(1).click();
    }

    public void setQuantity(int qty) {
        wait.until(ExpectedConditions.presenceOfElementLocated(quantityInput)).clear();
        driver.findElement(quantityInput).sendKeys(String.valueOf(qty));
    }

    public void submitOrder() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }
}
