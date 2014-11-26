package com.tascape.qa.example.suite;

import com.tascape.qa.example.driver.GoogleSearchUi;
import com.tascape.qa.example.test.TestClassGoogleSearch;
import com.tascape.qa.th.comm.WebBrowser;
import com.tascape.qa.th.suite.AbstractSuite;

/**
 *
 * @author linsong wang
 */
public class TestSuiteOne extends AbstractSuite {

    private WebBrowser browser;

    @Override
    public void setUpTestClasses() {
        this.addTestClass(TestClassGoogleSearch.class);
    }

    @Override
    protected void setUpEnvironment() throws Exception {
        browser = WebBrowser.getFirefox(false);
        browser.landscape();
        GoogleSearchUi search = new GoogleSearchUi();
        search.setEntityCommunication(browser);

        this.putDirver(TestClassGoogleSearch.class, TestClassGoogleSearch.SEARCH_UI, search);
    }

    @Override
    protected void tearDownEnvironment() {
        if (browser != null) {
            browser.quit();
        }
    }
}
