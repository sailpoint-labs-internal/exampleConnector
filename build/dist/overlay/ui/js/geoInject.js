$(document).ready(function () {
    $.ajax({
        method: "GET",
        beforeSend: function (request) {
            request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
        },
        url: "plugin/geoMap/processLogin"
    })
        .done(function (msg) {
            console.log(msg);
        });
});
