package io.github.seleninja;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.StringAssert;
import org.assertj.core.api.SoftAssertions;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Advanced SoftAssertions framework with built-in retry functionality spanning up to 10 seconds.
 * 
 * Optimized for dynamic content validation scenarios with loading delays:
 * - Real-time title updates
 * - Live search and filtering results
 * - Time-sensitive element text modifications
 * 
 * Integration pattern:
 * import static io.github.seleninja.NinjaSoftAssertions.*;
 * 
 * // Adaptive behavior determined by parameter type:
 * assertSoftly(softAssertions -> {
 *     // Supplier lambda = automatic retry for dynamic content:
 *     softAssertions.assertThat(() -> element.getText()).isEqualTo("Expected");
 *     softAssertions.assertThat(() -> element.isDisplayed()).isTrue();
 *     
 *     // Direct value = immediate evaluation for static content:
 *     softAssertions.assertThat("constant").isEqualTo("constant");
 *     softAssertions.assertThat(true).isTrue();
 * });
 */
public class NinjaSoftAssertions {
    
    private static final Logger log = LogManager.getLogger(NinjaSoftAssertions.class);
    private static final Duration RETRY_TIMEOUT_DURATION = Duration.ofSeconds(10);
    private static final Duration POLLING_INTERVAL_DURATION = Duration.ofMillis(200);
    
    /**
     * Enhanced SoftAssertions proxy featuring intelligent retry mechanisms for dynamic content validation.
     */
    public static class RetryingSoftAssertions {
        private final SoftAssertions assertionsDelegate = new SoftAssertions();
        
        /**
         * Advanced assertThat with retry capabilities - compatible with any Supplier lambda.
         * Returns a comprehensive assert supporting all validation types.
         */
        public <T> RetryingUniversalAssert<T> assertThat(Supplier<T> valueProvider) {
            return new RetryingUniversalAssert<>(valueProvider, assertionsDelegate);
        }
        
        /**
         * Traditional assertThat for immediate validation (no retry).
         */
        public StringAssert assertThat(String actualValue) {
            return assertionsDelegate.assertThat(actualValue);
        }
        
        /**
         * Traditional assertThat for immediate validation (no retry).
         */
        public BooleanAssert assertThat(Boolean actualValue) {
            return assertionsDelegate.assertThat(actualValue);
        }
        
        /**
         * Traditional assertThat for immediate validation (no retry).
         */
        public <T> ObjectAssert<T> assertThat(T actualValue) {
            return assertionsDelegate.assertThat(actualValue);
        }
        
        /**
         * Validates all accumulated assertions. Required final step.
         */
        public void assertAll() {
            assertionsDelegate.assertAll();
        }
    }
    
    /**
     * Comprehensive assertion framework with retry capabilities - unified support for all data types.
     */
    public static class RetryingUniversalAssert<T> {
        private final Supplier<T> valueProvider;
        private final SoftAssertions assertionsDelegate;
        private String assertionDescription = "";
        
        RetryingUniversalAssert(Supplier<T> valueProvider, SoftAssertions assertionsDelegate) {
            this.valueProvider = valueProvider;
            this.assertionsDelegate = assertionsDelegate;
        }
        
        public RetryingUniversalAssert<T> as(String description) {
            this.assertionDescription = description;
            return this;
        }
        
        // String validation methods
        
        public void contains(String expectedSubstring) {
            try {
                Awaitility.await()
                    .atMost(RETRY_TIMEOUT_DURATION)
                    .pollInterval(POLLING_INTERVAL_DURATION)
                    .ignoreExceptions()
                    .until(() -> {
                        T actualValue = valueProvider.get();
                        return actualValue instanceof String && ((String) actualValue).contains(expectedSubstring);
                    });
                    
                log.debug("✅ SoftAssertions contains validation successful: '{}' contains '{}'", valueProvider.get(), expectedSubstring);
            } catch (ConditionTimeoutException timeoutException) {
                T finalActualValue = valueProvider.get();
                assertionsDelegate.assertThat(finalActualValue).as(assertionDescription).asString().contains(expectedSubstring);
            }
        }
        
        // Boolean validation methods
        public void isTrue() {
            try {
                Awaitility.await()
                    .atMost(RETRY_TIMEOUT_DURATION)
                    .pollInterval(POLLING_INTERVAL_DURATION)
                    .ignoreExceptions()
                    .until(() -> {
                        T actualValue = valueProvider.get();
                        return actualValue instanceof Boolean && ((Boolean) actualValue);
                    });
                    
                log.debug("✅ SoftAssertions isTrue validation successful: {}", assertionDescription);
            } catch (ConditionTimeoutException timeoutException) {
                T finalActualValue = valueProvider.get();
                assertionsDelegate.assertThat(finalActualValue).as(assertionDescription).isEqualTo(true);
            }
        }
        
        public void isFalse() {
            try {
                Awaitility.await()
                    .atMost(RETRY_TIMEOUT_DURATION)
                    .pollInterval(POLLING_INTERVAL_DURATION)
                    .ignoreExceptions()
                    .until(() -> {
                        T actualValue = valueProvider.get();
                        return actualValue instanceof Boolean && !((Boolean) actualValue);
                    });
                    
                log.debug("✅ SoftAssertions isFalse validation successful: {}", assertionDescription);
            } catch (ConditionTimeoutException timeoutException) {
                T finalActualValue = valueProvider.get();
                assertionsDelegate.assertThat(finalActualValue).as(assertionDescription).isEqualTo(false);
            }
        }
        
        // Object validation methods
        public void isEqualTo(T expectedValue) {
            try {
                Awaitility.await()
                    .atMost(RETRY_TIMEOUT_DURATION)
                    .pollInterval(POLLING_INTERVAL_DURATION)
                    .ignoreExceptions()
                    .until(() -> {
                        T actualValue = valueProvider.get();
                        return java.util.Objects.equals(actualValue, expectedValue);
                    });
                    
                log.debug("✅ SoftAssertions isEqualTo validation successful: {} = {}", valueProvider.get(), expectedValue);
            } catch (ConditionTimeoutException timeoutException) {
                T finalActualValue = valueProvider.get();
                assertionsDelegate.assertThat(finalActualValue).as(assertionDescription).isEqualTo(expectedValue);
            }
        }
        
        public void isNotNull() {
            try {
                Awaitility.await()
                    .atMost(RETRY_TIMEOUT_DURATION)
                    .pollInterval(POLLING_INTERVAL_DURATION)
                    .ignoreExceptions()
                    .until(() -> valueProvider.get() != null);
                    
                log.debug("✅ SoftAssertions isNotNull validation successful: {}", assertionDescription);
            } catch (ConditionTimeoutException timeoutException) {
                T finalActualValue = valueProvider.get();
                assertionsDelegate.assertThat(finalActualValue).as(assertionDescription).isNotNull();
            }
        }
    }
    
    /**
     * Advanced assertSoftly with adaptive retry behavior.
     * 
     * Usage pattern:
     * assertSoftly(softAssertions -> {
     *     // Supplier lambda = automatic retry for 10 seconds (dynamic content):
     *     softAssertions.assertThat(() -> element.getText()).isEqualTo("Expected");
     *     softAssertions.assertThat(() -> element.isDisplayed()).isTrue();
     *     
     *     // Direct value = immediate validation (static content):
     *     softAssertions.assertThat("constant").isEqualTo("constant");
     *     softAssertions.assertThat(true).isTrue();
     * });
     * 
     * Java intelligently selects appropriate behavior based on parameter type!
     */
    public static void assertSoftly(Consumer<RetryingSoftAssertions> assertionsConsumer) {
        RetryingSoftAssertions retryingSoftAssertions = new RetryingSoftAssertions();
        assertionsConsumer.accept(retryingSoftAssertions);
        retryingSoftAssertions.assertAll();
    }
}
