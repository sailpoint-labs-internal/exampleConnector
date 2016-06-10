/*

 Other available values to build paths, etc. that are obtained from the server and set in the PluginFramework JS object

 PluginFramework.PluginBaseEndpointName = '#{pluginFramework.basePluginEndpointName}';
 PluginFramework.PluginEndpointRoot = '#{pluginFramework.basePluginEndpointName}/#{pluginFramework.basePluginEndpointName}';
 PluginFramework.PluginFolderName = '#{pluginFramework.pluginFolderName}';
 PluginFramework.CurrentPluginUniqueName = '#{pluginFramework.uniqueName}';
 PluginFramework.CsrfToken = Ext.util.Cookies.get('CSRF-TOKEN');

 */


var geoMapUrl = SailPoint.CONTEXT_PATH + '/pluginPage.jsf?pn=GeoMap';

jQuery(document).ready(function(){
    jQuery("ul.navbar-nav > li:contains('Setup') > ul li:last").after(
        '<li role="presentation" aria-hidden="true" class="divider"></li>'+
        '<li role="presentation">'+
        '<a href="'+geoMapUrl+'" role="menuitem" class="menuitem" tabindex="0">'+
        'Geo Map Plugin</a>'+
        '</li>');
    jQuery("ul.navbar-right li:first")
        .before(
            '<li class="dropdown">' +
            '        <a href="' + geoMapUrl + '" tabindex="0" role="menuitem" data-snippet-debug="off">' +
            '            <i role="presenation" class="glyphicon glyphicon-map-marker" style="font-size: 20px"></i>' +
            '        </a>' +
            '</li>'
        );
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
        // $("div[class='row m-b-sm m-t-sm home-header sp-page-header ng-scope']").prepend('<div style="float: left"><h1>Last Login</h1></div><br>');
        $('img.pull-right').replaceWith('<div id="lastLog" class="lastLog" style="display: inline; float: right; color: black">Last Login: '
                + data['login_time'] + " ("+ data['country_code']+") " + data['city'] +", " + data['region_name'] +": "+data['zip_code']
                +'</div>');
    }
});




