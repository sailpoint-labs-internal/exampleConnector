$(document).ready(function () {
    $.ajax({
        method: "GET",
        crossDomain: true,
        dataType : 'jsonp',
        url: "http://freegeoip.net/json/"
        // url: "plugin/geoMap/processLoginUsingHeaders"
    })
        .done(function (jsonObj) {
            var json = JSON.stringify(jsonObj);
            // console.log(json);

            $.ajax({
                type: "POST",
                contentType:"application/x-www-form-urlencoded",
                url: "plugin/geoMap/processLogin",
                beforeSend: function (request) {
                    request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
                },
                data: {
                    json: json
                }
            }).done(function(out){
                // console.log(out);
                // alert(out);

            });
        });
});



