(function (thr, $, undefined) {

    thr.querySuiteResults = function (suite, job) {
        var start = PF("wv_start").getDate().getTime();
        var stop = PF("wv_stop").getDate().getTime();
        var number = PF("wv_number").value;
        var invisible = PF("wv_invisible").getSelectedValue();
        var search = "?start=" + start + "&stop=" + stop + "&number=" + number + "&invisible=" + invisible;
        if (suite) {
            search += "&suite=" + suite;
        }
        if (job) {
            search += "&job=" + job;
        }
        location.search = search;
    };

    thr.querySuiteResultDetail = function (suite, job) {
        var start = PF("wv_start").getDate().getTime();
        var stop = PF("wv_stop").getDate().getTime();
        var number = PF("wv_number").value;
        var invisible = PF("wv_invisible").getSelectedValue();
        var search = "?start=" + start + "&stop=" + stop + "&number=" + number + "&invisible=" + invisible
                + "&suite=" + suite + "&job=" + job;
        location.replace("suite_result_history_detail.xhtml" + search);
    };

}(window.thr = window.thr || {}, jQuery));