/**
 * Created by maximilian.roquemore on 5/27/16.
 */
function get_location(callback) {
    console.log("regular");
    $.ajax({
        method: "GET",
        beforeSend: function (request) {
            request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
        },
        url: "plugin/geoMap/getLoginLocations/"
    })
        .done(function (msg) {
            console.log(msg + " from queryMap.js");
            // console.dir(msg);
            callback(msg);
            // return msg;
        });
};



function get_location2(val, callback) {
    console.log("val is " + val);
    $.ajax({
        method: "GET",
        beforeSend: function (request) {
            request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
        },
        url: "plugin/geoMap/getLoginLocations/"+val
    })
        .done(function (msg) {
            console.log(msg + " from queryMap.js");
            // console.dir(msg);
            callback(msg);
            // return msg;
        });
};