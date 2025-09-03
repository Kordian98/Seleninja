package io.github.seleninja;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;

/**
 * Enhanced Actions wrapper providing seamless integration with Ninja framework components.
 * 
 * Key capabilities:
 * - Automatic unwrapping of NinjaWebDriver and NinjaWebElement proxy instances
 * - Complete API compatibility with standard Selenium Actions implementation
 *
 * Integration example:
 * NinjaActions enhancedActions = new NinjaActions(ninjaWebDriver);
 * enhancedActions.dragAndDropBy(ninjaWebElement, 150, 75).perform();
 */
public class NinjaActions {
    
    private final Actions delegateActions;
    
    /**
     * Initializes NinjaActions with automatic NinjaWebDriver proxy unwrapping.
     */
    public NinjaActions(WebDriver webDriver) {
        this.delegateActions = new Actions(NinjaWebDriver.unwrap(webDriver));
    }
    
    // Proxy methods with element unwrapping capabilities
    
    // === KEYBOARD ACTIONS ===
    
    public NinjaActions sendKeys(CharSequence... keySequences) {
        delegateActions.sendKeys(keySequences);
        return this;
    }
    
    public NinjaActions sendKeys(WebElement targetElement, CharSequence... keySequences) {
        delegateActions.sendKeys(unwrapWebElement(targetElement), keySequences);
        return this;
    }
    
    public NinjaActions keyDown(CharSequence keyInput) {
        delegateActions.keyDown(keyInput);
        return this;
    }
    
    public NinjaActions keyDown(WebElement targetElement, CharSequence keyInput) {
        delegateActions.keyDown(unwrapWebElement(targetElement), keyInput);
        return this;
    }
    
    public NinjaActions keyUp(CharSequence keyInput) {
        delegateActions.keyUp(keyInput);
        return this;
    }
    
    public NinjaActions keyUp(WebElement targetElement, CharSequence keyInput) {
        delegateActions.keyUp(unwrapWebElement(targetElement), keyInput);
        return this;
    }
    
    // === MOVEMENT ACTIONS ===
    
    public NinjaActions moveToElement(WebElement targetElement) {
        delegateActions.moveToElement(unwrapWebElement(targetElement));
        return this;
    }
    
    public NinjaActions moveToElement(WebElement targetElement, int horizontalOffset, int verticalOffset) {
        delegateActions.moveToElement(unwrapWebElement(targetElement), horizontalOffset, verticalOffset);
        return this;
    }
    
    public NinjaActions moveByOffset(int horizontalOffset, int verticalOffset) {
        delegateActions.moveByOffset(horizontalOffset, verticalOffset);
        return this;
    }
    
    // === DRAG & DROP ACTIONS ===
    
    public NinjaActions dragAndDrop(WebElement sourceElement, WebElement targetElement) {
        delegateActions.dragAndDrop(unwrapWebElement(sourceElement), unwrapWebElement(targetElement));
        return this;
    }
    
    public NinjaActions dragAndDropBy(WebElement sourceElement, int horizontalOffset, int verticalOffset) {
        delegateActions.dragAndDropBy(unwrapWebElement(sourceElement), horizontalOffset, verticalOffset);
        return this;
    }
    
    // === CLICK ACTIONS ===
    
    public NinjaActions click(WebElement targetElement) {
        delegateActions.click(unwrapWebElement(targetElement));
        return this;
    }
    
    public NinjaActions click() {
        delegateActions.click();
        return this;
    }
    
    public NinjaActions contextClick(WebElement targetElement) {
        delegateActions.contextClick(unwrapWebElement(targetElement));
        return this;
    }
    
    public NinjaActions contextClick() {
        delegateActions.contextClick();
        return this;
    }
    
    public NinjaActions doubleClick(WebElement targetElement) {
        delegateActions.doubleClick(unwrapWebElement(targetElement));
        return this;
    }
    
    public NinjaActions doubleClick() {
        delegateActions.doubleClick();
        return this;
    }
    
    // === HOLD & RELEASE ACTIONS ===
    
    public NinjaActions clickAndHold(WebElement targetElement) {
        delegateActions.clickAndHold(unwrapWebElement(targetElement));
        return this;
    }
    
    public NinjaActions clickAndHold() {
        delegateActions.clickAndHold();
        return this;
    }
    
    public NinjaActions release(WebElement targetElement) {
        delegateActions.release(unwrapWebElement(targetElement));
        return this;
    }
    
    public NinjaActions release() {
        delegateActions.release();
        return this;
    }
    
    // === TIMING & EXECUTION ===
    
    public NinjaActions pause(long pauseMillis) {
        delegateActions.pause(pauseMillis);
        return this;
    }
    
    public NinjaActions pause(Duration pauseDuration) {
        delegateActions.pause(pauseDuration);
        return this;
    }
    
    /**
     * Executes all accumulated actions in the action chain.
     */
    public void perform() {
        delegateActions.perform();
    }
    
    /**
     * Provides access to the underlying Actions instance.
     * Reserved for scenarios requiring direct Actions API access.
     */
    public Actions getActions() {
        return delegateActions;
    }
    
    /**
     * Extracts the underlying WebElement from NinjaWebElement proxy if applicable.
     */
    private WebElement unwrapWebElement(WebElement element) {
        return NinjaWebElement.unwrap(element);
    }
}
