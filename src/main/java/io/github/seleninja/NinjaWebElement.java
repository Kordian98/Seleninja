package io.github.seleninja;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * Advanced WebElement proxy providing automatic retry mechanisms for StaleElementReferenceException scenarios
 * combined with intelligent wait condition strategies.
 *
 * Core capabilities:
 * - Automatic StaleElementReferenceException recovery with configurable retry parameters
 * - Adaptive wait conditions preceding operations (10-second timeout with intelligent polling)
 *   - click() ensures element clickability before interaction
 *   - isDisplayed() validates element visibility state
 *   - sendKeys(), clear() confirm element interaction readiness
 *   - Additional operations ensure element visibility requirements
 *
 * Implements Proxy Pattern for seamless WebElement API compatibility.
 * Eliminates manual retry implementation and explicit WebDriverWait requirements in standard scenarios.
 */
public class NinjaWebElement implements InvocationHandler {

    private static final Logger log = LogManager.getLogger(NinjaWebElement.class);

    // Wait configuration - 10-second timeout for element condition validation
    private static final Duration ELEMENT_WAIT_DURATION = Duration.ofSeconds(10);

    private final Supplier<WebElement> elementProvider;
    private final int retryAttemptLimit;
    private final int retryDelayMillis;
    private final boolean verboseLogging;

    private NinjaWebElement(Supplier<WebElement> elementProvider, int retryAttemptLimit, int retryDelayMillis, boolean verboseLogging) {
        this.elementProvider = elementProvider;
        this.retryAttemptLimit = retryAttemptLimit;
        this.retryDelayMillis = retryDelayMillis;
        this.verboseLogging = verboseLogging;
    }

    /**
     * Constructs ninja WebElement proxy using standard configuration parameters.
     */
    public static WebElement create(Supplier<WebElement> elementProvider) {
        return create(elementProvider, 3, 500, true);
    }

    /**
     * Constructs ninja WebElement proxy with customizable configuration parameters.
     */
    public static WebElement create(Supplier<WebElement> elementProvider, int retryAttemptLimit, int retryDelayMillis, boolean verboseLogging) {
        NinjaWebElement proxyHandler = new NinjaWebElement(elementProvider, retryAttemptLimit, retryDelayMillis, verboseLogging);

        return (WebElement) Proxy.newProxyInstance(
            WebElement.class.getClassLoader(),
            new Class[]{WebElement.class, Locatable.class, WrapsDriver.class}, // Include Locatable and WrapsDriver support
            proxyHandler
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodIdentifier = method.getName();

        // Enhanced handling for WrapsDriver.getWrappedDriver()
        if ("getWrappedDriver".equals(methodIdentifier)) {
            return extractDriverFromElement(elementProvider.get());
        }

        // Enhanced handling for findElement and findElements methods
        if ("findElement".equals(methodIdentifier) && args != null && args.length == 1 && args[0] instanceof By) {
            return processFindElement((By) args[0]);
        }

        if ("findElements".equals(methodIdentifier) && args != null && args.length == 1 && args[0] instanceof By) {
            return processFindElements((By) args[0]);
        }

        // Enhanced element operations with adaptive wait conditions
        return executeWithRetryMechanism(() -> {
            try {
                WebElement targetElement = elementProvider.get();

                // Apply adaptive wait strategies before specific operations
                applyIntelligentWait(targetElement, methodIdentifier);

                return method.invoke(targetElement, args);
            } catch (Exception exception) {
                if (exception instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                }
                throw new RuntimeException("Method invocation error: " + methodIdentifier, exception);
            }
        }, methodIdentifier);
    }

    /**
     * Extracts the underlying WebElement (bypassing proxy wrapper).
     * Essential for Actions class which requires direct element access.
     */
    public static WebElement unwrap(WebElement webElement) {
        if (Proxy.isProxyClass(webElement.getClass())) {
            InvocationHandler proxyHandler = Proxy.getInvocationHandler(webElement);
            if (proxyHandler instanceof NinjaWebElement) {
                return ((NinjaWebElement) proxyHandler).elementProvider.get();
            }
        }
        return webElement;
    }

    /**
     * Determines if WebElement is enhanced with NinjaWebElement proxy.
     */
    public static boolean isNinjaWebElement(WebElement webElement) {
        if (Proxy.isProxyClass(webElement.getClass())) {
            InvocationHandler proxyHandler = Proxy.getInvocationHandler(webElement);
            return proxyHandler instanceof NinjaWebElement;
        }
        return false;
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Implements adaptive wait conditions preceding specific WebElement operations.
     */
    private void applyIntelligentWait(WebElement targetElement, String methodIdentifier) {
        try {
            WebDriver webDriver = extractDriverFromElement(targetElement);
            if (webDriver == null) return;

            WebDriverWait driverWait = new WebDriverWait(webDriver, ELEMENT_WAIT_DURATION);

            switch (methodIdentifier) {
                case "click":
                    // Ensure element clickability before click execution
                    if (verboseLogging) {
                        log.debug("⏳ Validating element clickability state...");
                    }
                    driverWait.until(ExpectedConditions.elementToBeClickable(targetElement));
                    if (verboseLogging) {
                        log.debug("✅ Element clickability confirmed, executing click()");
                    }
                    break;

                case "isDisplayed":
                    // Ensure element visibility for display state validation
                    driverWait.until(ExpectedConditions.visibilityOf(targetElement));
                    if (verboseLogging) {
                        log.debug("✅ Element visibility confirmed, executing isDisplayed()");
                    }
                    break;

                case "getText":
                case "getAttribute":
                case "getDomAttribute":
                case "getCssValue":
                case "getTagName":
                case "getSize":
                case "getLocation":
                case "getRect":
                    // These operations require element DOM presence only
                    ensureElementPresence(targetElement, driverWait);
                    if (verboseLogging) {
                        log.debug("✅ Element DOM presence confirmed, executing {}()", methodIdentifier);
                    }
                    break;

                case "sendKeys":
                case "clear":
                    // Ensure element interaction readiness before operation
                    driverWait.until(ExpectedConditions.elementToBeClickable(targetElement));
                    if (verboseLogging) {
                        log.debug("✅ Element interaction readiness confirmed, executing {}()", methodIdentifier);
                    }
                    break;

                case "isEnabled":
                case "isSelected":
                    // State validation operations require DOM presence only
                    ensureElementPresence(targetElement, driverWait);
                    if (verboseLogging) {
                        log.debug("✅ Element DOM presence confirmed, executing {}()", methodIdentifier);
                    }
                    break;

                default:
                    // Default strategy ensures element DOM presence
                    ensureElementPresence(targetElement, driverWait);
                    if (verboseLogging) {
                        log.debug("✅ Element DOM presence confirmed, executing {}()", methodIdentifier);
                    }
                    break;
            }
        } catch (TimeoutException timeoutException) {
            // Handle wait timeout - log warning but allow operation continuation
            if (verboseLogging) {
                log.warn("⏰ Wait timeout for {}() - proceeding with operation. Element state may not be optimal.", methodIdentifier);
            }
        } catch (Exception exception) {
            // Handle wait errors - log and continue with operation
            if (verboseLogging) {
                log.warn("⚠️ Wait error for {}(): {} - proceeding with operation", methodIdentifier, exception.getMessage());
            }
        }
    }

    /**
     * Extracts WebDriver instance from WebElement for wait operation support.
     */
    private WebDriver extractDriverFromElement(WebElement targetElement) {
        try {
            if (targetElement instanceof WrapsDriver) {
                return ((WrapsDriver) targetElement).getWrappedDriver();
            }
            
            // Return null when driver extraction is not possible
            return null;
        } catch (Exception exception) {
            if (verboseLogging) {
                log.warn("WebDriver extraction failed for wait operations: {}", exception.getMessage());
            }
            return null;
        }
    }

    /**
     * Ensures element presence in DOM (visibility not required).
     * Superior to visibilityOf for operations like getText() that function with non-visible elements.
     */
    private void ensureElementPresence(WebElement targetElement, WebDriverWait driverWait) {
        try {
            // Custom wait condition validating element DOM attachment
            driverWait.until(driver -> {
                try {
                    // Access element properties - fails if element is stale or DOM-detached
                    targetElement.getTagName(); // Basic operation functional even on non-visible elements
                    return true;
                } catch (StaleElementReferenceException staleException) {
                    return false; // Element is stale, continue waiting
                } catch (NoSuchElementException notFoundException) {
                    return false; // Element absent from DOM, continue waiting
                }
            });
        } catch (Exception exception) {
            // Custom wait failure - attempt basic element access fallback
            try {
                targetElement.getTagName(); // Throws if element is inaccessible
            } catch (Exception ignored) {
                // Element inaccessible - proceed anyway
                // Retry mechanism will handle any StaleElementReferenceException
            }
        }
    }

    /**
     * Processes findElement - returns enhanced NinjaWebElement.
     */
    private WebElement processFindElement(By locator) {
        return NinjaWebElement.create(() -> {
            WebElement parentElement = elementProvider.get();
            return parentElement.findElement(locator);
        },
        retryAttemptLimit,
        retryDelayMillis,
        verboseLogging);
    }

    /**
     * Processes findElements - returns NinjaElementList with intelligent indexing.
     */
    private List<WebElement> processFindElements(By locator) {
        return executeWithRetryMechanism(() -> {
            // Return NinjaElementList featuring intelligent element waiting at specific indices
            return new NinjaElementList(() -> {
                WebElement parentElement = elementProvider.get();
                return parentElement.findElements(locator);
            },
            retryAttemptLimit,
            retryDelayMillis,
            verboseLogging);
        },
        "findElements");
    }

    /**
     * Executes operation with intelligent StaleElementReferenceException recovery.
     */
    private <T> T executeWithRetryMechanism(Supplier<T> operation, String operationIdentifier) {
        Exception finalException = null;

        for (int currentAttempt = 1; currentAttempt <= retryAttemptLimit; currentAttempt++) {
            try {
                return operation.get();
            } catch (StaleElementReferenceException staleException) {
                finalException = staleException;

                if (verboseLogging) {
                    log.warn("🔄 StaleElementReferenceException in operation '{}' - execution {} of {}",
                        operationIdentifier, currentAttempt, retryAttemptLimit);
                }

                if (currentAttempt < retryAttemptLimit) {
                    try {
                        Thread.sleep(retryDelayMillis);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interruption during retry waiting period", interruptedException);
                    }
                } else {
                    if (verboseLogging) {
                        log.error("❌ StaleElementReferenceException - exhausted {} attempts for operation '{}'",
                            retryAttemptLimit, operationIdentifier);
                    }
                }
            } catch (ElementClickInterceptedException interceptedException) {
                // Enhanced handling for intercepted clicks - JavaScript fallback strategy
                if ("click".equals(operationIdentifier)) {
                    try {
                        if (verboseLogging) {
                            log.warn("🚫 Element click intercepted (element obscuration detected) - attempting JavaScript fallback");
                        }

                        WebElement targetElement = elementProvider.get();
                        NinjaElementUtils.jsClick(targetElement);

                        if (verboseLogging) {
                            log.info("✅ JavaScript click execution successful - obscuration bypass achieved");
                        }
                        return null; // click() returns void
                    } catch (Exception jsClickException) {
                        if (verboseLogging) {
                            log.error("❌ JavaScript click fallback failed: {}", jsClickException.getMessage());
                        }
                        throw new RuntimeException("Both standard click and JavaScript fallback failed. Element likely obscured by overlay: " + interceptedException.getMessage(), interceptedException);
                    }
                } else {
                    throw new RuntimeException("Element click interception in operation: " + operationIdentifier, interceptedException);
                }
            } catch (Exception exception) {
                if (exception instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                }
                // For other exception types (non-StaleElement and non-ElementClickInterceptedException) - immediate failure
                throw new RuntimeException("Unexpected operation error: " + operationIdentifier, exception);
            }
        }

        // All attempts exhausted
        throw new RuntimeException("StaleElementReferenceException after " + retryAttemptLimit + " attempts in operation: " + operationIdentifier, finalException);
    }
}
