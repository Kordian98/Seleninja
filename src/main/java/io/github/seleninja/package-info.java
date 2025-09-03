/**
 * Selenium Ninja Framework - Advanced WebDriver Enhancement Suite
 * 
 * <p>This framework delivers seamless proxy-based wrappers for standard Selenium WebDriver APIs,
 * introducing intelligent retry patterns, adaptive wait strategies, and bulletproof test stability.
 * 
 * <h2>Core Components:</h2>
 * <ul>
 *   <li><strong>NinjaBy</strong> - Smart locator strategies with built-in element waiting mechanisms</li>
 *   <li><strong>NinjaWebDriver</strong> - Proxy wrapper automatically converting findElement results to resilient components</li>
 *   <li><strong>NinjaWebElement</strong> - Resilient element wrapper with StaleElementReferenceException recovery and adaptive waits</li>
 *   <li><strong>NinjaElementList</strong> - Smart collection implementation featuring index-based element waiting</li>
 *   <li><strong>NinjaActions</strong> - Enhanced Actions API seamlessly integrating with all Ninja components</li>
 *   <li><strong>NinjaAssertions</strong> - Robust assertions featuring 10-second retry cycles for dynamic content validation</li>
 *   <li><strong>NinjaSoftAssertions</strong> - Flexible soft assertion framework with automatic retry capabilities</li>
 *   <li><strong>NinjaElementUtils</strong> - JavaScript-powered utility operations for complex element interactions</li>
 * </ul>
 * 
 * <h2>Integration Example:</h2>
 * <pre>{@code
 * // Initialize with enhanced WebDriver wrapper
 * WebDriver webDriver = NinjaWebDriver.wrap(new ChromeDriver());
 * 
 * // Standard usage with automatic enhancements active!
 * WebElement submitBtn = webDriver.findElement(By.id("submit"));
 * submitBtn.click(); // Auto-waits for clickability + handles stale element recovery
 * 
 * // Dynamic content assertions with retry intelligence
 * NinjaAssertions.assertEquals(() -> element.getText(), "Expected Text");
 * 
 * // Flexible soft assertion patterns with retry support
 * NinjaSoftAssertions.assertSoftly(softAssertions -> {
 *     softAssertions.assertThat(() -> titleElement.getText()).isEqualTo("Dashboard");
 *     softAssertions.assertThat(() -> menuElement.isDisplayed()).isTrue();
 * });
 * }</pre>
 * 
 * <h2>Zero-Migration Philosophy:</h2>
 * <p>This framework preserves complete API compatibility with vanilla Selenium WebDriver components.
 * Integration requires only driver wrapping - existing code continues working with enhanced reliability.
 * 
 * <h2>Intelligent Wait Strategies:</h2>
 * <ul>
 *   <li>click() → ensures element clickability before interaction</li>
 *   <li>sendKeys() → verifies element interaction readiness</li>
 *   <li>getText() → confirms element DOM presence (supports non-visible elements)</li>
 *   <li>isDisplayed() → validates element visibility state</li>
 * </ul>
 * 
 * @author Kordian Goldman
 * @version 1.0.0
 * @since 1.0.0
 */
package io.github.seleninja;
