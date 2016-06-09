/**
 * Created by maximilian.roquemore on 5/27/16.
 */
function get_locations(callback) {
    $.ajax({
        method: "GET",
        beforeSend: function (request) {
            request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
        },
        url: "plugin/geoMap/getLoginLocations/"
    })
        .done(function (msg) {
            callback(msg);
        });
}
function get_shapes(callback) {
    $.ajax({
        method: "GET",
        beforeSend: function (request) {
            request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
        },
        url: "plugin/geoMap/getShapes/"
    })
        .done(function (msg) {
            callback(msg);
        });
}
function getDB(callback) {
    $.ajax({
        method: "GET",
        beforeSend: function (request) {
            request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
        },
        url: "plugin/geoMap/getDB"
    }).done(function (msg) {
        // callback(JSON.stringify(msg));
        // var x = JSON.parse(msg);
        callback(msg);
    });
}

