# 🥷 Seleninja

**Enhanced Selenium WebDriver library with automatic retry, smart waits, and transparent proxy wrappers**

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.kordian98/seleninja/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.kordian98/seleninja)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://openjdk.java.net/projects/jdk/17/)

---

## 🚀 Key Features

- 🔄 **Automatic retry** on `StaleElementReferenceException`
- ⏱️ **Smart waits** - each operation automatically waits for the appropriate element state  
- 🎯 **Zero code changes** - 100% compatibility with standard Selenium API
- 📦 **Transparent proxies** - just wrap WebDriver and everything works
- 🧪 **Enhanced assertions** - assertions with automatic retry for dynamic content
- 🛠️ **JavaScript utilities** - advanced element operations with NinjaElementUtils
- 🎨 **Additional locators** - `cssWithText()`, `byText()` with auto-wait

---

## 📦 Installation

### Maven
```xml
<dependency>
    <groupId>io.github.kordian98</groupId>
    <artifactId>seleninja</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```kotlin
Not yet supported
<!-- implementation 'io.github.kordian98:seleninja:1.0.0' -->
```

---

## 🏃‍♂️ Quick Start

```java
import io.github.seleninja.*;
import static io.github.seleninja.NinjaAssertions.*;
import static io.github.seleninja.NinjaSoftAssertions.*;

// 1. Wrap your WebDriver
WebDriver driver = NinjaWebDriver.wrap(new ChromeDriver());

// 2. Use normally - automatic retry and wait included!
WebElement button = driver.findElement(By.id("submit"));
button.click(); // ✅ Auto waits for clickable + retry on stale

// 3. Enhanced assertions with retry
assertEquals(() -> element.getText(), "Expected Text");

// 4. Soft assertions with retry
assertSoftly(softly -> {
    softly.assertThat(() -> title.getText()).isEqualTo("Dashboard");
    softly.assertThat(() -> menu.isDisplayed()).isTrue();
});
```

**That's it!** No additional code changes needed - Seleninja works transparently.

---

## 🎯 Core Components

### 🛡️ NinjaWebDriver

Automatically wraps all `findElement()` operations in `NinjaWebElement`:

```java
WebDriver driver = NinjaWebDriver.wrap(new ChromeDriver());

// These calls automatically return NinjaWebElement:
WebElement element = driver.findElement(By.id("test"));
List<WebElement> elements = driver.findElements(By.className("item"));
```

### ⚡ NinjaWebElement

Transparent wrapper with intelligent wait and retry:

```java
WebElement element = driver.findElement(By.id("button"));

// Each operation automatically:
element.click();        // 1. Waits for clickable 2. Retry on stale
element.sendKeys("hi"); // 1. Waits for clickable 2. Retry on stale  
element.getText();      // 1. Waits for presence 2. Retry on stale
element.isDisplayed();  // 1. Waits for visible 2. Retry on stale
```

### 🎭 NinjaActions

Actions wrapper that automatically unwraps Ninja* components:

```java
NinjaActions actions = new NinjaActions(driver);
actions.dragAndDropBy(ninjaElement, 100, 50).perform();
// ✅ Automatically unwraps NinjaWebElement for Actions API
```

### 📋 NinjaElementList

Intelligent element list with wait for specific indices:

```java
List<WebElement> items = driver.findElements(By.className("item"));

// Waits up to 10 seconds for element to appear at index 5
WebElement fifthItem = items.get(5); // ✅ Doesn't throw IndexOutOfBounds
```

---

## 🧪 Enhanced Assertions

### Standard assertions with retry

```java
import static io.github.seleninja.NinjaAssertions.*;

// ❌ Old approach - immediate failure
assertTrue(element.getText().equals("Loading..."));

// ✅ New approach - retry for 10 seconds  
assertTrue(() -> element.getText().equals("Loaded!"));
assertEquals(() -> element.getText(), "Success");
assertContains(() -> status.getText(), "Complete");
```

### Soft assertions with retry

```java
import static io.github.seleninja.NinjaSoftAssertions.*;

assertSoftly(softly -> {
    // Lambda = auto retry for 10 seconds (dynamic content):
    softly.assertThat(() -> pageTitle.getText()).isEqualTo("Dashboard");
    softly.assertThat(() -> loadingSpinner.isDisplayed()).isFalse();
    
    // Direct value = immediate evaluation (static content):
    softly.assertThat("constant").isEqualTo("constant");
    softly.assertThat(true).isTrue();
});
```

---

## 🎨 Extended Locators

```java
import io.github.seleninja.NinjaBy;

// Find button with text "Save" - automatic wait 10s
WebElement saveButton = driver.findElement(
    NinjaBy.cssWithText("button", "Save")
);

// Find element with exact text
WebElement header = driver.findElement(
    NinjaBy.byText("Welcome to Dashboard")
);
```

---

## ⚙️ Configuration

### Default NinjaWebDriver Settings

```java
// Basic wrapping
WebDriver driver = NinjaWebDriver.wrap(chromeDriver);

// Advanced wrapping  
WebDriver driver = NinjaWebDriver.wrap(
    chromeDriver,
    5,      // maxAttempts
    500,    // delayInMillis  
    true    // enableLogging
);
```

### Default NinjaWebElement Settings

```java
// Basic creation
WebElement ninja = NinjaWebElement.create(() -> driver.findElement(By.id("test")));

// Advanced creation
WebElement ninja = NinjaWebElement.create(
    () -> driver.findElement(By.id("test")),
    3,      // maxAttempts
    500,    // delayInMillis
    true    // enableLogging  
);
```

---

## 🛠️ Utilities

### NinjaElementUtils - JavaScript operations

```java
// JavaScript click (bypasses overlays)
NinjaElementUtils.jsClick(element);

// JavaScript text (more reliable)  
String text = NinjaElementUtils.getElementText(element);

// Smooth scroll to element
NinjaElementUtils.scrollTo(element);

// Drag & drop with NinjaActions
NinjaElementUtils.performDragAndDrop(source, target);
```

---

## 📊 Intelligent Wait Behavior

| Operation | Automatic Wait |
|----------|------------------|
| `click()` | `elementToBeClickable()` |
| `sendKeys()` | `elementToBeClickable()` |
| `clear()` | `elementToBeClickable()` |
| `isDisplayed()` | `visibilityOf()` |
| `getText()` | `presenceOf()` (works with invisible elements) |
| `getAttribute()` | `presenceOf()` |
| `isEnabled()` | `presenceOf()` |

---

## 🔧 Usage Examples

### Page Object Pattern

```java
public class LoginPage {
    private WebDriver driver;
    
    public LoginPage(WebDriver driver) {
        // Selenium driver automatically wrapped in NinjaWebDriver
        this.driver = driver;
    }
    
    public void login(String username, String password) {
        // All operations have automatic retry + wait
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);  
        driver.findElement(By.id("submit")).click();
        
        // Enhanced assertion with retry
        assertEquals(() -> 
            driver.findElement(By.id("welcome")).getText(), 
            "Welcome " + username
        );
    }
}
```

### Test with multiple assertions

```java
@Test
public void testDashboard() {
    driver.get("https://app.example.com/dashboard");
    
    // All assertions with automatic retry for 10s
    assertSoftly(softly -> {
        softly.assertThat(() -> pageTitle.getText())
              .as("Page title")
              .isEqualTo("Dashboard");
              
        softly.assertThat(() -> userMenu.isDisplayed())
              .as("User menu visibility")  
              .isTrue();
              
        softly.assertThat(() -> notificationCount.getText())
              .as("Notification count")
              .contains("3 new");
    });
}
```

---

## 📈 Benefits

### ❌ Before Seleninja
```java
// Manual retry
for (int i = 0; i < 3; i++) {
    try {
        element.click();
        break;
    } catch (StaleElementReferenceException e) {
        element = driver.findElement(locator);
        Thread.sleep(500);
    }
}

// Manual wait
WebDriverWait wait = new WebDriverWait(driver, 10);
wait.until(ExpectedConditions.elementToBeClickable(element));
element.click();

// Manual assertions
assertTrue(element.getText().equals("Expected")); // ❌ Immediate failure
```

### ✅ With Seleninja  
```java
// Automatic retry + wait
element.click(); // ✅ Everything works automatically

// Assertions with retry
assertEquals(() -> element.getText(), "Expected"); // ✅ Retry for 10s
```

---

## 📝 Requirements

- **Java 17+**  
- **Selenium 4.15+**
- **JUnit 5** (for NinjaAssertions)
- **AssertJ** (for NinjaSoftAssertions)
- **Log4j2** (for logging)
- **Awaitility** (for retry mechanisms)

---

## 🤝 Contributing

1. Fork the project
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)  
5. Open Pull Request

---

## 📄 License

MIT License - see [LICENSE](LICENSE) for details.

---

## 🙏 Authors

Created by Kordian Goldman

---

**Seleninja - Make your Selenium tests more stable without changing a single line of code! 🥷**