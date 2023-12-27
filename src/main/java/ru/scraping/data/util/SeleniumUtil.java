package ru.scraping.data.util;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.scraping.data.exceptions.ServiceException;
import java.time.Duration;

public final class SeleniumUtil {

    private SeleniumUtil() {

    }

    public static void goToPage(String url, WebDriver driver) {
        driver.get(url);
    }

    public static void setImplicitlyWait(int timeout, WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeout));
    }

    public static void scrollDown(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    public static void scrollUp(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -100)");
    }

    public static WebElement findElement(String xPath, WebDriver driver) {
        try {
            return driver.findElement(By.xpath(xPath));
        } catch (Exception e) {
            return null;
        }
    }

    public static WebElement getElement(String xPath, WebDriver driver) {
        return driver.findElement(By.xpath(xPath));
    }

    public static void waitUntil(String xPath, int timeout, WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
    }

    public static void timeoutInSec(int timeout) {
        try {
            Thread.sleep(timeout * 1_000);
        } catch (InterruptedException e) {
            throw new ServiceException(e);
        }
    }
}
