/*

 Other available values to build paths, etc. that are obtained from the server and set in the PluginFramework JS object

 PluginFramework.PluginBaseEndpointName = '#{pluginFramework.basePluginEndpointName}';
 PluginFramework.PluginEndpointRoot = '#{pluginFramework.basePluginEndpointName}/#{pluginFramework.basePluginEndpointName}';
 PluginFramework.PluginFolderName = '#{pluginFramework.pluginFolderName}';
 PluginFramework.CurrentPluginUniqueName = '#{pluginFramework.uniqueName}';
 PluginFramework.CsrfToken = Ext.util.Cookies.get('CSRF-TOKEN');

 */


// var geoMapUrl = SailPoint.CONTEXT_PATH + '/pluginPage.jsf?pn=GeoMap';
//
// jQuery(document).ready(function(){
//     jQuery("ul.navbar-right li:first")
//         .before(
//             '<li class="dropdown">' +
//             '        <a href="' + geoMapUrl + '" tabindex="0" role="menuitem" data-snippet-debug="off">' +
//             '            <i role="presenation" class="fa fa-exclamation fa-lg example"></i>' +
//             '        </a>' +
//             '</li>'
//         );
// });

// console.log("I AM STILL RUNNING!!!>");
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

//run this ^ 1x on home page
//every other doc we need to check that session is still the same

// console.log("PATH IS !!!!");
// console.log(top.location.pathname);
// if (top.location.pathname === '/my/path')
// {

//
// if(top.location.pathname === '/identityiq/home.jsf'){
//
//
// }

//
// // console.log("I AM STILL RUNNING!!!>");
// $(document).ready(function () {
//     $.ajax({
//         method: "GET",
//         beforeSend: function (request) {
//             request.setRequestHeader("X-XSRF-TOKEN", PluginFramework.CsrfToken);
//         },
//         url: "plugin/geoMap/loadMap"
//     })
//         .done(function (msg) {
//             console.log(msg);
//         });
// });
