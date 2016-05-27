/**
 * Created by maximilian.roquemore on 5/27/16.
 */
$(document).ready(function () {
    $.ajax({
        method: "GET",
        beforeSend: function (request) {
            request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
        },
        url: "plugin/geoMap/getLoginLocations"
    })
        .done(function (msg) {
            console.log(msg);
            console.dir(msg);
        });
});
