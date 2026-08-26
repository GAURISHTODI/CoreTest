const { test, expect } = require('@playwright/test');

test.describe('Order Flow', () => {
  test('Complete order flow from catalog to status', async ({ page }) => {
    // 1. Product Catalog
    await page.goto('/');
    
    // Verify page loaded
    await expect(page.locator('h1')).toHaveText('Product Catalog');
    
    // Find the first available "Order Now" button
    const firstOrderButton = page.locator('.card button', { hasText: 'Order Now' }).first();
    await expect(firstOrderButton).toBeVisible();
    await firstOrderButton.click();

    // 2. Place Order Page
    await expect(page).toHaveURL(/.*\/order/);
    await expect(page.locator('h1')).toHaveText('Place Order');
    
    // Set quantity
    await page.locator('input[type="number"]').fill('2');
    
    // Submit order
    await page.locator('button[type="submit"]').click();

    // 3. Order Status Page
    await expect(page).toHaveURL(/.*\/status\?id=.*/);
    await expect(page.locator('h1')).toHaveText('Order Status');
    
    // Verify status is PENDING or CONFIRMED
    const statusText = await page.locator('.badge').textContent();
    expect(['PENDING', 'CONFIRMED']).toContain(statusText.trim());
    
    // Verify Order ID is present
    const orderId = await page.locator("xpath=//strong[text()='Order ID:']/following-sibling::span").textContent();
    expect(orderId.trim()).not.toBe('');
  });
});
