
togglePerfLines = function() {
    var lines = document.getElementsByClassName("jqplot-table-legend-label");
    for (i = 0; i < lines.length; i++) {
        lines[i].click();
    }
}