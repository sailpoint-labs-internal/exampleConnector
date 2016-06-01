$(document).ready(function () {
    $.ajax({
        method: "GET",
        beforeSend: function (request) {
            request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
        },
        url: "plugin/geoMap/processLoginUsingHeaders"
    })
        .done(function (msg) {
            console.log(msg);
        });
});
