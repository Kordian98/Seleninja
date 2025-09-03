package io.github.seleninja;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Interactive;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * Transparent WebDriver proxy automatically converting findElement() and findElements() results to NinjaWebElement instances.
 * 
 * Preserves complete WebDriver API compatibility - functions as seamless drop-in replacement.
 * Eliminates manual retry wrapping requirements for stale element handling.
 */
public class NinjaWebDriver implements InvocationHandler {
    
    private final WebDriver targetDriver;
    private final int retryAttemptCount;
    private final int retryDelayMillis;
    private final boolean verboseLogging;
    
    private NinjaWebDriver(WebDriver targetDriver, int retryAttemptCount, int retryDelayMillis, boolean verboseLogging) {
        this.targetDriver = targetDriver;
        this.retryAttemptCount = retryAttemptCount;
        this.retryDelayMillis = retryDelayMillis;
        this.verboseLogging = verboseLogging;
    }
    
    /**
     * Creates NinjaWebDriver proxy with standard configuration settings.
     */
    public static WebDriver wrap(WebDriver webDriver) {
        return wrap(webDriver, 5, 500, true);
    }
    
    /**
     * Creates NinjaWebDriver proxy with customizable configuration parameters.
     */
    public static WebDriver wrap(WebDriver webDriver, int maxRetryAttempts, int retryDelayMillis, boolean enableVerboseLogging) {
        if (webDriver == null) {
            throw new IllegalArgumentException("WebDriver instance cannot be null");
        }
        
        NinjaWebDriver proxyHandler = new NinjaWebDriver(webDriver, maxRetryAttempts, retryDelayMillis, enableVerboseLogging);
        
        // Extract all interfaces implemented by the driver for complete proxy functionality
        Class<?>[] driverInterfaces = extractAllInterfaces(webDriver.getClass());
        
        return (WebDriver) Proxy.newProxyInstance(
            WebDriver.class.getClassLoader(),
            driverInterfaces,
            proxyHandler
        );
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodIdentifier = method.getName();
        
        // Enhanced findElement handling - returns NinjaWebElement proxy
        if ("findElement".equals(methodIdentifier) && args != null && args.length == 1 && args[0] instanceof By) {
            By elementLocator = (By) args[0];
            return NinjaWebElement.create(() -> targetDriver.findElement(elementLocator), retryAttemptCount, retryDelayMillis, verboseLogging);
        }
        
        // Enhanced findElements handling - returns NinjaElementList with intelligent indexing
        if ("findElements".equals(methodIdentifier) && args != null && args.length == 1 && args[0] instanceof By) {
            By elementsLocator = (By) args[0];
            return new NinjaElementList(() -> targetDriver.findElements(elementsLocator), retryAttemptCount, retryDelayMillis, verboseLogging);
        }
        
        // Standard method delegation for all remaining operations
        try {
            return method.invoke(targetDriver, args);
        } catch (Exception exception) {
            if (exception.getCause() instanceof RuntimeException) {
                throw exception.getCause();
            }
            throw exception;
        }
    }

    /**
     * Extracts the underlying WebDriver instance from NinjaWebDriver proxy.
     * Required for scenarios needing direct driver access.
     */
    public static WebDriver unwrap(WebDriver webDriver) {
        if (Proxy.isProxyClass(webDriver.getClass())) {
            InvocationHandler proxyHandler = Proxy.getInvocationHandler(webDriver);
            if (proxyHandler instanceof NinjaWebDriver) {
                return ((NinjaWebDriver) proxyHandler).targetDriver;
            }
        }
        return webDriver;
    }
    
    /**
     * Determines if WebDriver instance is a NinjaWebDriver proxy.
     */
    public static boolean isNinjaWebDriver(WebDriver webDriver) {
        if (Proxy.isProxyClass(webDriver.getClass())) {
            InvocationHandler proxyHandler = Proxy.getInvocationHandler(webDriver);
            return proxyHandler instanceof NinjaWebDriver;
        }
        return false;
    }
    
    /**
     * Collects all interfaces implemented by the WebDriver class for comprehensive proxy functionality.
     * Essential for preserving capabilities like TakesScreenshot, JavascriptExecutor, etc.
     */
    private static Class<?>[] extractAllInterfaces(Class<?> driverClass) {
        Set<Class<?>> interfaceSet = new java.util.HashSet<>();
        
        // Include primary WebDriver interface
        interfaceSet.add(WebDriver.class);
        
        // Collect all class interfaces
        collectInterfacesRecursively(driverClass, interfaceSet);
        
        // Include essential Selenium interfaces
        includeIfSupported(driverClass, interfaceSet, TakesScreenshot.class);
        includeIfSupported(driverClass, interfaceSet, JavascriptExecutor.class);
        includeIfSupported(driverClass, interfaceSet, Interactive.class);
        includeIfSupported(driverClass, interfaceSet, HasCapabilities.class);
        
        return interfaceSet.toArray(new Class<?>[0]);
    }
    
    private static void collectInterfacesRecursively(Class<?> targetClass, Set<Class<?>> interfaceCollection) {
        if (targetClass == null) return;
        
        // Collect interfaces from current class
        for (Class<?> currentInterface : targetClass.getInterfaces()) {
            interfaceCollection.add(currentInterface);
            collectInterfacesRecursively(currentInterface, interfaceCollection); // Recursive interface collection
        }
        
        // Process parent class interfaces
        collectInterfacesRecursively(targetClass.getSuperclass(), interfaceCollection);
    }
    
    private static void includeIfSupported(Class<?> targetClass, Set<Class<?>> interfaceCollection, Class<?> candidateInterface) {
        if (candidateInterface.isAssignableFrom(targetClass)) {
            interfaceCollection.add(candidateInterface);
        }
    }
    
}
