package io.github.seleninja;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Robust assertion framework featuring automatic retry mechanisms with 10-second timeout thresholds.
 * 
 * Ideal for validating dynamic content with loading delays, including:
 * - Real-time updated page titles
 * - Live filtering and search functionality
 * 
 * Integration pattern:
 * import static io.github.seleninja.NinjaAssertions.*;
 * 
 * // Traditional approach (immediate failure):
 * assertTrue(element.getText().equals("Expected"));
 * 
 * // Enhanced approach (intelligent retry for 10 seconds):
 * assertTrue(() -> element.getText().equals("Expected"));
 * assertEquals(() -> element.getText(), "Expected");
 */
public class NinjaAssertions {
    
    private static final Logger log = LogManager.getLogger(NinjaAssertions.class);
    private static final Duration RETRY_TIMEOUT_DURATION = Duration.ofSeconds(10);
    private static final Duration POLLING_INTERVAL_DURATION = Duration.ofMillis(200);
    
    /**
     * Validates string containment with automatic retry for up to 10 seconds.
     */
    public static void assertContains(Supplier<String> actualStringProvider, String expectedSubstring) {
        assertContains(actualStringProvider, expectedSubstring, "");
    }
    
    /**
     * Validates string containment with automatic retry for up to 10 seconds.
     */
    public static void assertContains(Supplier<String> actualStringProvider, String expectedSubstring, String validationMessage) {
        try {
            Awaitility.await()
                .atMost(RETRY_TIMEOUT_DURATION)
                .pollInterval(POLLING_INTERVAL_DURATION)
                .ignoreExceptions()
                .until(() -> {
                    String actualString = actualStringProvider.get();
                    return actualString != null && actualString.contains(expectedSubstring);
                });
                
            log.debug("✅ assertContains validation successful: '{}' contains '{}'", actualStringProvider.get(), expectedSubstring);
        } catch (ConditionTimeoutException timeoutException) {
            String finalActualString = actualStringProvider.get();
            String failureMessage = validationMessage.isEmpty() ? 
                String.format("Expected string to contain <%s> but found <%s> after %d seconds", expectedSubstring, finalActualString, RETRY_TIMEOUT_DURATION.getSeconds()) :
                String.format("%s: Expected string to contain <%s> but found <%s> after %d seconds", validationMessage, expectedSubstring, finalActualString, RETRY_TIMEOUT_DURATION.getSeconds());
            throw new AssertionError(failureMessage);
        }
    }
    
    /**
     * Validates condition truthfulness with automatic retry for up to 10 seconds.
     */
    public static void assertTrue(Supplier<Boolean> conditionProvider) {
        assertTrue(conditionProvider, "");
    }
    
    /**
     * Validates condition truthfulness with automatic retry for up to 10 seconds.
     */
    public static void assertTrue(Supplier<Boolean> conditionProvider, String validationMessage) {
        try {
            Awaitility.await()
                .atMost(RETRY_TIMEOUT_DURATION)
                .pollInterval(POLLING_INTERVAL_DURATION)
                .ignoreExceptions()
                .until(conditionProvider::get, conditionResult -> conditionResult == true);
                
            log.debug("✅ assertTrue validation successful: {}", validationMessage);
        } catch (ConditionTimeoutException timeoutException) {
            String failureMessage = validationMessage.isEmpty() ? 
                "Condition evaluation failed after " + RETRY_TIMEOUT_DURATION.getSeconds() + " seconds" :
                validationMessage + " (retry duration: " + RETRY_TIMEOUT_DURATION.getSeconds() + " seconds)";
            throw new AssertionError(failureMessage);
        }
    }
    
    /**
     * Validates condition falseness with automatic retry for up to 10 seconds.
     */
    public static void assertFalse(Supplier<Boolean> conditionProvider) {
        assertFalse(conditionProvider, "");
    }
    
    /**
     * Validates condition falseness with automatic retry for up to 10 seconds.
     */
    public static void assertFalse(Supplier<Boolean> conditionProvider, String validationMessage) {
        try {
            Awaitility.await()
                .atMost(RETRY_TIMEOUT_DURATION)
                .pollInterval(POLLING_INTERVAL_DURATION)
                .ignoreExceptions()
                .until(conditionProvider::get, conditionResult -> conditionResult == false);
                
            log.debug("✅ assertFalse validation successful: {}", validationMessage);
        } catch (ConditionTimeoutException timeoutException) {
            String failureMessage = validationMessage.isEmpty() ? 
                "Condition evaluation did not become false after " + RETRY_TIMEOUT_DURATION.getSeconds() + " seconds" :
                validationMessage + " (retry duration: " + RETRY_TIMEOUT_DURATION.getSeconds() + " seconds)";
            throw new AssertionError(failureMessage);
        }
    }
    
    /**
     * Validates value equality with automatic retry for up to 10 seconds.
     */
    public static <T> void assertEquals(Supplier<T> actualValueProvider, T expectedValue) {
        assertEquals(actualValueProvider, expectedValue, "");
    }
    
    /**
     * Validates value equality with automatic retry for up to 10 seconds.
     */
    public static <T> void assertEquals(Supplier<T> actualValueProvider, T expectedValue, String validationMessage) {
        try {
            Awaitility.await()
                .atMost(RETRY_TIMEOUT_DURATION)
                .pollInterval(POLLING_INTERVAL_DURATION)
                .ignoreExceptions()
                .until(() -> {
                    T actualValue = actualValueProvider.get();
                    return Objects.equals(actualValue, expectedValue);
                });
                
            log.debug("✅ assertEquals validation successful: {} = {}", actualValueProvider.get(), expectedValue);
        } catch (ConditionTimeoutException timeoutException) {
            T finalActualValue = actualValueProvider.get();
            String failureMessage = validationMessage.isEmpty() ? 
                String.format("Expected value <%s> but found <%s> after %d seconds", expectedValue, finalActualValue, RETRY_TIMEOUT_DURATION.getSeconds()) :
                String.format("%s: Expected value <%s> but found <%s> after %d seconds", validationMessage, expectedValue, finalActualValue, RETRY_TIMEOUT_DURATION.getSeconds());
            throw new AssertionError(failureMessage);
        }
    }
    
    /**
     * Validates value inequality with automatic retry for up to 10 seconds.
     */
    public static <T> void assertNotEquals(Supplier<T> actualValueProvider, T unexpectedValue) {
        assertNotEquals(actualValueProvider, unexpectedValue, "");
    }
    
    /**
     * Validates value inequality with automatic retry for up to 10 seconds.
     */
    public static <T> void assertNotEquals(Supplier<T> actualValueProvider, T unexpectedValue, String validationMessage) {
        try {
            Awaitility.await()
                .atMost(RETRY_TIMEOUT_DURATION)
                .pollInterval(POLLING_INTERVAL_DURATION)
                .ignoreExceptions()
                .until(() -> {
                    T actualValue = actualValueProvider.get();
                    return !Objects.equals(actualValue, unexpectedValue);
                });
                
            log.debug("✅ assertNotEquals validation successful: {} != {}", actualValueProvider.get(), unexpectedValue);
        } catch (ConditionTimeoutException timeoutException) {
            T finalActualValue = actualValueProvider.get();
            String failureMessage = validationMessage.isEmpty() ? 
                String.format("Expected value not to be <%s> but was <%s> after %d seconds", unexpectedValue, finalActualValue, RETRY_TIMEOUT_DURATION.getSeconds()) :
                String.format("%s: Expected value not to be <%s> but was <%s> after %d seconds", validationMessage, unexpectedValue, finalActualValue, RETRY_TIMEOUT_DURATION.getSeconds());
            throw new AssertionError(failureMessage);
        }
    }
    
    /**
     * Convenience method for element visibility validation with retry capability.
     */
    public static void assertDisplayed(Supplier<Boolean> visibilityProvider) {
        assertDisplayed(visibilityProvider, "Element visibility validation failed");
    }
    
    /**
     * Convenience method for element visibility validation with retry capability.
     */
    public static void assertDisplayed(Supplier<Boolean> visibilityProvider, String validationMessage) {
        assertTrue(visibilityProvider, validationMessage);
    }
    
    // Legacy compatibility methods for immediate assertions (without retry functionality)
    
    /**
     * Traditional assertTrue with immediate evaluation (no retry mechanism).
     * Maintains JUnit compatibility.
     */
    public static void assertTrue(boolean conditionValue) {
        Assertions.assertTrue(conditionValue);
    }
    
    /**
     * Traditional assertTrue with immediate evaluation (no retry mechanism).
     * Maintains JUnit compatibility.
     */
    public static void assertTrue(boolean conditionValue, String failureMessage) {
        Assertions.assertTrue(conditionValue, failureMessage);
    }
    
    /**
     * Traditional assertFalse with immediate evaluation (no retry mechanism).
     * Maintains JUnit compatibility.
     */
    public static void assertFalse(boolean conditionValue) {
        Assertions.assertFalse(conditionValue);
    }
    
    /**
     * Traditional assertFalse with immediate evaluation (no retry mechanism).
     * Maintains JUnit compatibility.
     */
    public static void assertFalse(boolean conditionValue, String failureMessage) {
        Assertions.assertFalse(conditionValue, failureMessage);
    }
    
    /**
     * Traditional assertEquals with immediate evaluation (no retry mechanism).
     * Maintains JUnit compatibility.
     */
    public static <T> void assertEquals(T expectedValue, T actualValue) {
        Assertions.assertEquals(expectedValue, actualValue);
    }
    
    /**
     * Traditional assertEquals with immediate evaluation (no retry mechanism).
     * Maintains JUnit compatibility.
     */
    public static <T> void assertEquals(T expectedValue, T actualValue, String failureMessage) {
        Assertions.assertEquals(expectedValue, actualValue, failureMessage);
    }
}
