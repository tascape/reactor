package com.tascape.qa.th.comm;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class WebBrowser extends EntityCommunication implements WebDriver {

    private static final Logger LOG = LoggerFactory.getLogger(WebBrowser.class);

    public static final String PROP_WEB_BROWSER_TYPE = "qa.comm.WebBrowser.TYPE";

    public static final int AJAX_TIMEOUT_SECONDS = 180;

    public static final int WIDTH = 1920;

    public static final int HEIGHT = 1080;

    private WebDriver webDriver;

    private Actions actions;

    public void setWebDriver(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public enum Type {
        Firefox,
        Chrome,
        Html,
    }

    public static WebBrowser getWebBrowser(Type type) throws Exception {
        return WebBrowser.getWebBrowser(type, false);
    }

    public static WebBrowser getFirefox(boolean extEnabled) throws Exception {
        return getWebBrowser(Type.Firefox, extEnabled);
    }

    public static WebBrowser getWebBrowser(Type type, boolean extEnabled) throws Exception {
        WebBrowser wb;
        switch (type) {
            case Firefox:
                wb = new Firefox(extEnabled);
                break;

            case Chrome:
                wb = new Chrome();
                break;

            default:
                throw new RuntimeException("Browser type " + type + " is not supported");
        }
        wb.setDefaults();
        return wb;
    }

    @Override
    public void connect() throws IOException {
    }

    @Override
    public void disconnect() throws IOException {
        this.webDriver.quit();
    }

    @Override
    public void get(String url) {
        LOG.debug("Open url {}", url);
        this.webDriver.get(url);
    }

    public abstract int getPageLoadTimeMillis(String url) throws Exception;

    public abstract int getAjaxLoadTimeMillis(Ajax ajax) throws Exception;

    @Override
    public String getCurrentUrl() {
        String url = this.webDriver.getCurrentUrl();
        LOG.debug("Current url is {}", url);
        return url;
    }

    @Override
    public String getTitle() {
        String title = this.webDriver.getTitle();
        LOG.debug("Title is {}", title);
        return title;
    }

    @Override
    public List<WebElement> findElements(By by) {
        return this.webDriver.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return this.webDriver.findElement(by);
    }

    @Override
    public String getPageSource() {
        String src = this.webDriver.getPageSource();
        LOG.debug("Page src length {}", src.length());
        return src;
    }

    @Override
    public void close() {
        LOG.debug("Close browser");
        this.webDriver.close();
    }

    @Override
    public void quit() {
        LOG.debug("Quit browser");
        this.webDriver.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return this.webDriver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return this.webDriver.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return this.webDriver.switchTo();
    }

    @Override
    public Navigation navigate() {
        return this.webDriver.navigate();
    }

    @Override
    public Options manage() {
        return this.webDriver.manage();
    }

    /**
     * @param <T>
     *               For an HTML element, this method returns a WebElement
     *               For a number, a Long is returned
     *               For a boolean, a Boolean is returned
     *               For all other cases, a String is returned.
     *               For an array, return a List of Object, with each object following the rules above.
     *               Unless the value is null or there is no return value, in which null is returned
     *
     * @param type   return type
     * @param script
     * @param args   Arguments must be a number, a boolean, a String, a WebElement, or a List of any combination of the
     *               above. An exception will be thrown if the arguments do not meet these criteria. The arguments will
     *               be made available to the JavaScript via the "arguments" magic variable, as if the function were
     *               called via "Function.apply"
     *
     * @return
     */
    public <T extends Object> T executeScript(Class<T> type, String script, Object... args) {
        Object object = ((JavascriptExecutor) webDriver).executeScript(script, args);
        return type.cast(object);
    }

    public void landscape() {
        this.manage().window().setPosition(new Point(0, 0));
        this.manage().window().setSize(new Dimension(WIDTH, HEIGHT));
    }

    public void portrait() {
        this.manage().window().setPosition(new Point(0, 0));
        this.manage().window().setSize(new Dimension(HEIGHT, WIDTH));
    }

    public void hide() {
        this.manage().window().setPosition(new Point(WIDTH, WIDTH));
    }

    public void scrollToTop() {
        ((JavascriptExecutor) webDriver).executeScript("window.scrollTo(0, 0 - document.body.scrollHeight);");
    }

    public void scrollToBottom() {
        ((JavascriptExecutor) webDriver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    public String getHtml(WebElement element) {
        return this.executeScript(String.class, "return arguments[0].innerHTML;", element);
    }

    public Actions getActions() {
        return actions;
    }

    public WebElement waitForElement(By by, int seconds) {
        LOG.debug("Wait for element {} to appear", by);
        WebDriverWait wait = new WebDriverWait(this, seconds);
        WebElement e = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        Assert.assertNotNull("Cannot find element " + by, e);
        return e;
    }

    public void waitForNoElement(final By by, int seconds) {
        LOG.debug("Wait for element {} to disappear", by);
        WebDriverWait wait = new WebDriverWait(this, seconds);
        wait.until((WebDriver t) -> {
            List<WebElement> es = t.findElements(by);
            if (es.isEmpty()) {
                return true;
            } else {
                return es.stream().noneMatch((e) -> (!e.getCssValue("display").equals("none")));
            }
        });
    }

    private void setDefaults() {
        this.actions = new Actions(this.webDriver);
        this.webDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
        this.webDriver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
        this.webDriver.manage().timeouts().setScriptTimeout(120, TimeUnit.SECONDS);
        this.hide();
    }

    public WebDriver getWebDriver() {
        return this.webDriver;
    }

    public static interface Ajax {
        public void doRequest();

        public By getByAppear();

        public By getByDisapper();
    }

    public static class AbstractAjax implements Ajax {
        @Override
        public void doRequest() {
        }

        @Override
        public By getByAppear() {
            return null;
        }

        @Override
        public By getByDisapper() {
            return null;
        }
    }
}
