package com.tascape.qa.example.driver;

import com.tascape.qa.th.driver.WebPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 *
 * @author linsong wang
 */
public class SearchPage extends WebPage {
    
    public static final String URL = "http://google.com";

    @FindBy(id = "gbqfq")
    private WebElement searchBox;

    public void submitSearch(String term) {
        this.searchBox.sendKeys(term);
    }
}
