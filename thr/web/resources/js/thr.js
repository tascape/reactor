(function (thr, $, undefined) {

    thr.querySuiteResults = function (suite) {
        var start = PF("wv_start").getDate().getTime();
        var stop = PF("wv_stop").getDate().getTime();
        var number = PF("wv_number").value;
        var invisible = PF("wv_invisible").getSelectedValue();
        var search = "?start=" + start + "&stop=" + stop + "&number=" + number + "&invisible=" + invisible;
        if (suite) {
            search += "&suite=" + suite;
        }
        location.search = search;
    };

}(window.thr = window.thr || {}, jQuery));