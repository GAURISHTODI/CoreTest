package com.coretest.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ProductListPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By productCards = By.className("card");
    private final By firstOrderButton = By.cssSelector(".card button");

    public ProductListPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public ProductListPage navigateTo(String baseUrl) {
        driver.get(baseUrl + "/");
        return this;
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1"))).getText().equals("Product Catalog");
    }

    public int getProductCount() {
        wait.until(ExpectedConditions.presenceOfElementLocated(productCards));
        return driver.findElements(productCards).size();
    }

    public void clickFirstAvailableOrderButton() {
        wait.until(ExpectedConditions.elementToBeClickable(firstOrderButton)).click();
    }
}
