package com.tascape.qa.th.comm;

import com.tascape.qa.th.SystemConfiguration;
import java.util.Arrays;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public class Chrome extends WebBrowser {
    private static final Logger LOG = LoggerFactory.getLogger(Chrome.class);

    private static final SystemConfiguration CONFIG = SystemConfiguration.getInstance();

    public Chrome() {
        String chromeServer = System.getProperty("webdriver.chrome.driver");
        if (chromeServer == null) {
            throw new RuntimeException("Cannot find system property webdriver.chrome.driver");
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments(Arrays.asList("allow-running-insecure-content", "ignore-certificate-errors"));
        //options.addExtensions(new File("/path/to/extension.crx"));
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        this.setWebDriver(new ChromeDriver(capabilities));
    }

    @Override
    public int getPageLoadTimeMillis(String url) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getAjaxLoadTimeMillis(Ajax ajax) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
