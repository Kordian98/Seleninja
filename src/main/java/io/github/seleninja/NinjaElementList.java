package io.github.seleninja;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

/**
 * Advanced List<WebElement> wrapper featuring intelligent index-based element waiting capabilities.
 * 
 * Upon invoking .get(index), automatically waits up to 10 seconds for element availability at the specified index,
 * preventing immediate IndexOutOfBoundsException failures.
 */
public class NinjaElementList implements List<WebElement> {
    
    private static final Logger log = LogManager.getLogger(NinjaElementList.class);
    private static final Duration ELEMENT_WAIT_DURATION = Duration.ofSeconds(10);
    
    private final Supplier<List<WebElement>> elementsProvider;
    private final int retryAttemptCount;
    private final int retryDelayMillis;
    private final boolean verboseLogging;
    
    public NinjaElementList(Supplier<List<WebElement>> elementsProvider, int retryAttemptCount, int retryDelayMillis, boolean verboseLogging) {
        this.elementsProvider = elementsProvider;
        this.retryAttemptCount = retryAttemptCount;
        this.retryDelayMillis = retryDelayMillis;
        this.verboseLogging = verboseLogging;
    }
    
    @Override
    public WebElement get(int targetIndex) {
        if (verboseLogging) {
            log.debug("🔍 Retrieving element at index {} with intelligent waiting", targetIndex);
        }
        
        // Apply intelligent waiting for element availability at target index
        awaitElementAtIndex(targetIndex);
        
        // Enhance element with NinjaWebElement wrapper for additional resilience
        return NinjaWebElement.create(() -> {
            List<WebElement> currentElementList = elementsProvider.get();
            if (targetIndex < currentElementList.size()) {
                return currentElementList.get(targetIndex);
            }
            throw new org.openqa.selenium.NoSuchElementException("Element at index " + targetIndex + " is no longer available");
        }, 
        retryAttemptCount, 
        retryDelayMillis, 
        verboseLogging);
    }
    
    // Delegate remaining List methods to the current element collection
    
    @Override
    public int size() {
        return elementsProvider.get().size();
    }
    
    @Override
    public boolean isEmpty() {
        return elementsProvider.get().isEmpty();
    }
    
    @Override
    public boolean contains(Object targetObject) {
        return elementsProvider.get().contains(targetObject);
    }
    
    @Override
    public Iterator<WebElement> iterator() {
        return new EnhancedIterator();
    }
    
    @Override
    public Object[] toArray() {
        return elementsProvider.get().toArray();
    }
    
    @Override
    public <T> T[] toArray(T[] targetArray) {
        return elementsProvider.get().toArray(targetArray);
    }
    
    @Override
    public boolean add(WebElement elementToAdd) {
        throw new UnsupportedOperationException("NinjaElementList modification not permitted");
    }
    
    @Override
    public boolean remove(Object objectToRemove) {
        throw new UnsupportedOperationException("NinjaElementList modification not permitted");
    }
    
    @Override
    public boolean containsAll(Collection<?> targetCollection) {
        return elementsProvider.get().containsAll(targetCollection);
    }
    
    @Override
    public boolean addAll(Collection<? extends WebElement> elementsToAdd) {
        throw new UnsupportedOperationException("NinjaElementList modification not permitted");
    }
    
    @Override
    public boolean addAll(int targetIndex, Collection<? extends WebElement> elementsToAdd) {
        throw new UnsupportedOperationException("NinjaElementList modification not permitted");
    }
    
    @Override
    public boolean removeAll(Collection<?> elementsToRemove) {
        throw new UnsupportedOperationException("NinjaElementList modification not permitted");
    }
    
    @Override
    public boolean retainAll(Collection<?> elementsToRetain) {
        throw new UnsupportedOperationException("NinjaElementList modification not permitted");
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("NinjaElementList modification not permitted");
    }
    
    @Override
    public WebElement set(int targetIndex, WebElement replacementElement) {
        throw new UnsupportedOperationException("NinjaElementList modification not permitted");
    }
    
    @Override
    public void add(int targetIndex, WebElement elementToAdd) {
        throw new UnsupportedOperationException("NinjaElementList modification not permitted");
    }
    
    @Override
    public WebElement remove(int targetIndex) {
        throw new UnsupportedOperationException("NinjaElementList modification not permitted");
    }
    
    @Override
    public int indexOf(Object targetObject) {
        return elementsProvider.get().indexOf(targetObject);
    }
    
    @Override
    public int lastIndexOf(Object targetObject) {
        return elementsProvider.get().lastIndexOf(targetObject);
    }
    
    @Override
    public ListIterator<WebElement> listIterator() {
        return new EnhancedListIterator(0);
    }
    
    @Override
    public ListIterator<WebElement> listIterator(int startIndex) {
        return new EnhancedListIterator(startIndex);
    }
    
    @Override
    public List<WebElement> subList(int fromIndex, int toIndex) {
        // Return standard list for subList - enhanced wrapper would be overly complex
        return elementsProvider.get().subList(fromIndex, toIndex);
    }
    
    // ========== PRIVATE HELPER METHODS ==========
    
    private void awaitElementAtIndex(int targetIndex) {
        try {
            // Extract WebDriver for wait operations - simplified without WebDriverManager dependency
            WebDriver webDriver = extractWebDriverFromCurrentElement();
            if (webDriver == null) {
                // Fallback to immediate access when driver extraction fails
                List<WebElement> currentElementList = elementsProvider.get();
                if (targetIndex >= currentElementList.size()) {
                    throw new IndexOutOfBoundsException("Element at index " + targetIndex + " is not available");
                }
                return;
            }
            
            WebDriverWait driverWait = new WebDriverWait(webDriver, ELEMENT_WAIT_DURATION);
            
            // Await element availability at the target index
            driverWait.until(driver -> {
                try {
                    List<WebElement> currentElementList = elementsProvider.get();
                    if (targetIndex < currentElementList.size()) {
                        WebElement targetElement = currentElementList.get(targetIndex);
                        // Validate element accessibility
                        targetElement.getTagName(); // Fails if element is stale
                        return targetElement;
                    }
                    return null; // Insufficient elements available, continue waiting
                } catch (Exception exception) {
                    return null; // Error encountered, continue waiting
                }
            });
            
        } catch (Exception exception) {
            if (verboseLogging) {
                log.warn("⏰ Wait timeout for element at index {} - attempting immediate access", targetIndex);
            }
            
            // Fallback to immediate access
            List<WebElement> currentElementList = elementsProvider.get();
            if (targetIndex >= currentElementList.size()) {
                throw new IndexOutOfBoundsException("Element at index " + targetIndex + " unavailable after waiting period");
            }
        }
    }
    
    private WebDriver extractWebDriverFromCurrentElement() {
        try {
            List<WebElement> currentElementList = elementsProvider.get();
            if (!currentElementList.isEmpty() && currentElementList.get(0) instanceof WrapsDriver) {
                return ((WrapsDriver) currentElementList.get(0)).getWrappedDriver();
            }
        } catch (Exception exception) {
            // Ignore exception and return null
        }
        return null;
    }
    
    // ========== PRIVATE INNER CLASSES ==========
    
    /**
     * Enhanced iterator that wraps elements in NinjaWebElement for additional protection.
     */
    private class EnhancedIterator implements Iterator<WebElement> {
        private int iterationIndex = 0;
        
        @Override
        public boolean hasNext() {
            return iterationIndex < size();
        }
        
        @Override
        public WebElement next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            return get(iterationIndex++); // Utilize our enhanced get() method
        }
    }
    
    /**
     * Enhanced list iterator that wraps elements in NinjaWebElement for additional resilience.
     */
    private class EnhancedListIterator implements ListIterator<WebElement> {
        private int iteratorPosition;
        
        public EnhancedListIterator(int startPosition) {
            this.iteratorPosition = startPosition;
        }
        
        @Override
        public boolean hasNext() {
            return iteratorPosition < size();
        }
        
        @Override
        public WebElement next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            return get(iteratorPosition++);
        }
        
        @Override
        public boolean hasPrevious() {
            return iteratorPosition > 0;
        }
        
        @Override
        public WebElement previous() {
            if (!hasPrevious()) {
                throw new java.util.NoSuchElementException();
            }
            return get(--iteratorPosition);
        }
        
        @Override
        public int nextIndex() {
            return iteratorPosition;
        }
        
        @Override
        public int previousIndex() {
            return iteratorPosition - 1;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException("NinjaElementList modification not permitted");
        }
        
        @Override
        public void set(WebElement replacementElement) {
            throw new UnsupportedOperationException("NinjaElementList modification not permitted");
        }
        
        @Override
        public void add(WebElement elementToAdd) {
            throw new UnsupportedOperationException("NinjaElementList modification not permitted");
        }
    }
}
