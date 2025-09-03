package io.github.seleninja;

import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Advanced locator strategies featuring adaptive waiting and content-based filtering.
 * 
 * Extends standard Selenium By capabilities with specialized locator methods:
 * - cssWithText(): Combines CSS selectors with text content matching
 * - byText(): Locates elements through exact text content matching
 * 
 * All locator strategies incorporate automatic 10-second element waiting.
 */
public class NinjaBy {
    
    private static final Duration WAIT_DURATION = Duration.ofSeconds(10);
    private static final Duration CHECK_INTERVAL = Duration.ofMillis(100);
    
    private NinjaBy() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Generates a locator that identifies elements by CSS selector and text content.
     * Combines CSS selection with text content filtering into a unified locator strategy.
     * @param cssSelector target CSS selector pattern
     * @param expectedText text content to match within elements
     * @return Locator compatible with standard Selenium element finding methods
     */
    public static By cssWithText(String cssSelector, String expectedText) {
        
        return new By() {
            private List<WebElement> locateElementsWithTextContent(SearchContext searchContext, String selector, String textContent) {
                return searchContext.findElements(By.cssSelector(selector)).stream()
                        .filter(element -> {
                            try {
                                return element.getText().contains(textContent);
                            } catch (Exception exception) {
                                return false;
                            }
                        })
                        .toList();
            }
            
            @Override
            public List<WebElement> findElements(SearchContext context) {
                try {
                    return Awaitility.await()
                            .atMost(WAIT_DURATION)
                            .pollInterval(CHECK_INTERVAL)
                            .ignoreExceptions()
                            .until(() -> locateElementsWithTextContent(context, cssSelector, expectedText),
                                    resultList -> !resultList.isEmpty());
                } catch (Exception exception) {
                    return Collections.emptyList();
                }
            }

            @Override
            public String toString() {
                return "NinjaBy.cssWithText: " + cssSelector + " containing text '" + expectedText + "'";
            }
        };
    }
    
    /**
     * Constructs a locator that matches elements by their exact text content.
     * @param exactText The precise text content to locate.
     * @return Locator compatible with standard Selenium element finding methods.
     */
    public static By byText(String exactText) {
        return new By() {
            private List<WebElement> locateByExactTextMatch(SearchContext searchContext, String textToMatch) {
                return searchContext.findElements(By.xpath("//*[text()='" + textToMatch + "']"));
            }

            @Override
            public List<WebElement> findElements(SearchContext context) {
                try {
                    return Awaitility.await()
                            .atMost(WAIT_DURATION)
                            .pollInterval(CHECK_INTERVAL)
                            .ignoreExceptions()
                            .until(() -> locateByExactTextMatch(context, exactText),
                                    resultList -> !resultList.isEmpty());
                } catch (Exception exception) {
                    return Collections.emptyList();
                }
            }

            @Override
            public String toString() {
                return "NinjaBy.byText: '" + exactText + "'";
            }
        };
    }
} 
