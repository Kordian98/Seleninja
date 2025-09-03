package io.github.seleninja;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WrapsDriver;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Comprehensive utility suite for JavaScript-powered element operations and resilience mechanisms.
 * Designed for seamless integration with NinjaWebElement and NinjaWebDriver components.
 */
public class NinjaElementUtils {
    
    private static final Duration STANDARD_WAIT_DURATION = Duration.ofSeconds(10);
    
    // === CORE RETRY UTILITIES ===
    
    /**
     * Executes provided action with automatic StaleElementReferenceException recovery.
     *
     * @param actionToExecute The operation to perform (via Runnable functional interface).
     * @param maxRetryAttempts The maximum retry attempt threshold.
     * @param retryDelayMillis The pause duration between attempts in milliseconds.
     * @throws StaleElementReferenceException if operation fails after exhausting all retry attempts.
     */
    public static void retryActionOnStaleElement(Runnable actionToExecute, int maxRetryAttempts, int retryDelayMillis) {
        int currentAttempt = 0;
        while (currentAttempt < maxRetryAttempts) {
            try {
                actionToExecute.run();
                return;
            } catch (StaleElementReferenceException staleException) {
                currentAttempt++;
                if (currentAttempt >= maxRetryAttempts) {
                    throw staleException;
                }
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interruption during action retry waiting period", interruptedException);
                }
            }
        }
    }

    /**
     * Executes provided function with automatic StaleElementReferenceException recovery.
     *
     * @param functionToExecute The operation to execute and return result from.
     * @param maxRetryAttempts The maximum retry attempt threshold.
     * @param retryDelayMillis The pause duration between attempts in milliseconds.
     * @return The successful execution result.
     * @throws StaleElementReferenceException if operation fails after exhausting all retry attempts.
     */
    public static <T> T retryFunctionOnStaleElement(Supplier<T> functionToExecute, int maxRetryAttempts, int retryDelayMillis) {
        int currentAttempt = 0;
        while (currentAttempt < maxRetryAttempts) {
            try {
                return functionToExecute.get();
            } catch (StaleElementReferenceException staleException) {
                currentAttempt++;
                if (currentAttempt >= maxRetryAttempts) {
                    throw staleException;
                }
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interruption during function retry waiting period", interruptedException);
                }
            }
        }
        throw new RuntimeException("Function execution retry exhausted all available attempts");
    }
    
    // === SCROLLING OPERATIONS ===
    
    /**
     * Scrolls target element into viewport with smooth animation.
     */
    public static void scrollTo(WebElement targetElement) {
        WebDriver webDriver = extractWebDriverFromElement(targetElement);
        if (webDriver == null) {
            throw new RuntimeException("Unable to extract WebDriver from element for scrolling operation");
        }
        
        JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
        // Extract underlying element from NinjaWebElement proxy for JavaScript operations
        WebElement unwrappedElement = NinjaWebElement.unwrap(targetElement);
        jsExecutor.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", unwrappedElement);
    }

    /**
     * Scrolls page viewport to the bottom position.
     */
    public static void scrollToBottom(WebDriver webDriver) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) NinjaWebDriver.unwrap(webDriver);
        jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }
    
    /**
     * Scrolls element's internal content to the bottom.
     */
    public static void scrollElementToBottom(WebElement targetElement) {
        WebDriver webDriver = extractWebDriverFromElement(targetElement);
        if (webDriver == null) {
            throw new RuntimeException("Unable to extract WebDriver from element for content scrolling");
        }
        
        JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
        // Extract underlying element from NinjaWebElement proxy for JavaScript operations
        WebElement unwrappedElement = NinjaWebElement.unwrap(targetElement);
        jsExecutor.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", unwrappedElement);
    }
    
    // === BASIC ELEMENT OPERATIONS ===
    
    /**
     * Executes element click via JavaScript injection.
     * Particularly effective when standard clicks are blocked by overlay elements.
     */
    public static void jsClick(WebElement targetElement) {
        WebDriver webDriver = extractWebDriverFromElement(targetElement);
        if (webDriver == null) {
            throw new RuntimeException("Unable to extract WebDriver from element for JavaScript click execution");
        }
        
        JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
        // Extract underlying element from NinjaWebElement proxy for JavaScript operations
        WebElement unwrappedElement = NinjaWebElement.unwrap(targetElement);
        jsExecutor.executeScript("arguments[0].click();", unwrappedElement);
    }
    
    /**
     * Retrieves element text content via JavaScript execution.
     * Often more dependable than standard getText() for dynamically updating content.
     */
    public static String getElementText(WebElement targetElement) {
        WebDriver webDriver = extractWebDriverFromElement(targetElement);
        if (webDriver == null) {
            throw new RuntimeException("Unable to extract WebDriver from element for JavaScript text extraction");
        }
        
        JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
        // Extract underlying element from NinjaWebElement proxy for JavaScript operations
        WebElement unwrappedElement = NinjaWebElement.unwrap(targetElement);
        return (String) jsExecutor.executeScript("return arguments[0].innerText;", unwrappedElement);
    }
    
    // === MOVEMENT & INTERACTION ===
    
    /**
     * Positions mouse cursor over target element using Actions API.
     */
    public static void hoverOverElement(WebElement targetElement) {
        WebDriver webDriver = extractWebDriverFromElement(targetElement);
        if (webDriver == null) {
            throw new RuntimeException("Unable to extract WebDriver for hover operation");
        }
        
        // Wait for element to be visible before hover action
        WebDriverWait driverWait = new WebDriverWait(webDriver, STANDARD_WAIT_DURATION);
        driverWait.until(ExpectedConditions.visibilityOf(targetElement));
        
        // Execute hover using NinjaActions for enhanced reliability
        new NinjaActions(webDriver).moveToElement(targetElement).perform();
    }
    
    /**
     * Relocates element position using Actions API offset movement.
     */
    public static void moveElement(int horizontalOffset, int verticalOffset, WebElement targetElement) {
        WebDriver webDriver = extractWebDriverFromElement(targetElement);
        if (webDriver == null) {
            throw new RuntimeException("Unable to extract WebDriver for element movement");
        }
        
        // Wait for element clickability before movement action
        WebDriverWait driverWait = new WebDriverWait(webDriver, STANDARD_WAIT_DURATION);
        driverWait.until(ExpectedConditions.elementToBeClickable(targetElement));
        
        // Execute movement using NinjaActions for enhanced reliability  
        new NinjaActions(webDriver)
            .clickAndHold(targetElement)
            .moveByOffset(horizontalOffset, verticalOffset)
            .release()
            .perform();
    }
    
    // === COMPLEX DRAG & DROP OPERATIONS ===
    
    /**
     * Executes drag-and-drop operation between two elements using Actions API.
     */
    public static void performDragAndDrop(WebElement sourceElement, WebElement targetElement) {
        WebDriver webDriver = extractWebDriverFromElement(sourceElement);
        if (webDriver == null) {
            throw new RuntimeException("Unable to extract WebDriver for drag and drop operation");
        }
        
        // Ensure both elements are ready for interaction
        WebDriverWait driverWait = new WebDriverWait(webDriver, STANDARD_WAIT_DURATION);
        driverWait.until(ExpectedConditions.elementToBeClickable(sourceElement));
        driverWait.until(ExpectedConditions.visibilityOf(targetElement));
        
        // Execute drag-and-drop using NinjaActions for enhanced reliability
        new NinjaActions(webDriver).dragAndDrop(sourceElement, targetElement).perform();
    }
    
    /**
     * Executes drag-and-drop with precise offset positioning.
     */
    public static void performDragAndDropWithOffset(WebElement sourceElement, WebElement targetElement, int horizontalOffset, int verticalOffset) {
        WebDriver webDriver = extractWebDriverFromElement(sourceElement);
        if (webDriver == null) {
            throw new RuntimeException("Unable to extract WebDriver for drag and drop with offset operation");
        }
        
        // Ensure source element is ready for interaction
        WebDriverWait driverWait = new WebDriverWait(webDriver, STANDARD_WAIT_DURATION);
        driverWait.until(ExpectedConditions.elementToBeClickable(sourceElement));
        
        // Execute complex drag-and-drop sequence with offset using NinjaActions
        new NinjaActions(webDriver)
            .clickAndHold(sourceElement)
            .moveToElement(targetElement)
            .moveByOffset(horizontalOffset, verticalOffset)
            .release()
            .perform();
    }


    
    /**
     * Extracts WebDriver instance from WebElement proxy.
     */
    private static WebDriver extractWebDriverFromElement(WebElement element) {
        try {
            if (element instanceof WrapsDriver) {
                return ((WrapsDriver) element).getWrappedDriver();
            }
            return null;
        } catch (Exception exception) {
            return null;
        }
    }
} 
