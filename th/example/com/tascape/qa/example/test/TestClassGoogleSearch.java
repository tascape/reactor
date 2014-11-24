package com.tascape.qa.example.test;

import com.tascape.qa.example.driver.GoogleSearchUi;
import com.tascape.qa.th.data.TestDataProvider;
import com.tascape.qa.th.data.TestIterationData;
import com.tascape.qa.th.test.JUnit4Test;
import org.junit.Test;

/**
 *
 * @author linsong wang
 */
public class TestClassGoogleSearch extends JUnit4Test {
    
    public static final String SEARCH_UI = "SEARCH_UI";

    private final GoogleSearchUi search;
    
    public TestClassGoogleSearch() {
        this.search = this.getDriver(SEARCH_UI, GoogleSearchUi.class);
    }

    @Test
    @TestDataProvider(klass=TestIterationData.class)
    public void testSearch() throws Exception {
        this.search.search("test automation");
    }
}
