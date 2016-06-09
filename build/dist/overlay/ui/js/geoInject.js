$(document).ready(function () {
    $.ajax({
        method: "GET",
        crossDomain: true,
        dataType : 'jsonp',
        url: "http://freegeoip.net/json/"
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
                },
                success: function(data){
                    if(data == 1){
                        // $(location).attr('href', 'http://i26.servimg.com/u/f26/11/96/43/31/banned13.jpg');
                        //     window.location.href = "ban.xhtml";
                        // doLogout();
                    }
                }
            }).done(function(out){
                // console.log(out);
                // alert(out);

            });
        });
});



