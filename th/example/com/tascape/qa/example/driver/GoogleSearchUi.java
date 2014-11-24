package com.tascape.qa.example.driver;

import com.tascape.qa.th.driver.EntityDriver;
import com.tascape.qa.th.driver.WebBrowser;

/**
 *
 * @author linsong wang
 */
public class GoogleSearchUi extends EntityDriver {

    private WebBrowser browser;

    private SearchPage queryPage;

    @Override
    public String getName() {
        return "Google Search UI";
    }

    public void search(String term) throws Exception {
        this.reset();
        this.queryPage.submitSearch(term);
    }

    @Override
    public void reset() throws Exception {
        this.browser.get(SearchPage.URL);
    }

    public WebBrowser getBrowser() {
        return browser;
    }

    public void setBrowser(WebBrowser browser) {
        this.browser = browser;
        this.queryPage = this.browser.getPage(SearchPage.class, this);
    }
}
