var geoMapUrl = SailPoint.CONTEXT_PATH + '/pluginPage.jsf?pn=GeoMap';

jQuery(document).ready(function(){
    jQuery("ul.navbar-left li:last").after('<li role="presentation" aria-hidden="true" class="divider"></li><li role="presentation"> <a href="'+geoMapUrl+'" role="menuitem" class="menuitem" tabindex="0"> Geo Map Plugin </a></li>');
});


function getDBhome(callback) {
    $.ajax({
        method: "GET",
        beforeSend: function (request) {
            request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
        },
        url: "plugin/geoMap/getLastLogin"
    }).done(function (msg) {
        var x = JSON.parse(msg);
        console.log(x + " is db homne");
        callback(x);
    });
}
getDBhome(function insertLogin(data){
    console.log(data + " is data");
    if(data) {
        data = data[0];
        $('img.pull-right').replaceWith('<div id="lastLog" class="lastLog" style="display: inline; float: right; color: black">Last Login: '
                + data['login_time'] + " ("+ data['country_code']+") " + data['city'] +", " + data['region_name'] +": "+data['zip_code']
                +'</div>');
    }
});

