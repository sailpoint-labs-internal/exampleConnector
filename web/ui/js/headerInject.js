
/*

Other available values to build paths, etc. that are obtained from the server and set in the PluginFramework JS object

PluginFramework.PluginBaseEndpointName = '#{pluginFramework.basePluginEndpointName}';
PluginFramework.PluginEndpointRoot = '#{pluginFramework.basePluginEndpointName}/#{pluginFramework.basePluginEndpointName}';
PluginFramework.PluginFolderName = '#{pluginFramework.pluginFolderName}';
PluginFramework.CurrentPluginUniqueName = '#{pluginFramework.uniqueName}';
PluginFramework.CsrfToken = Ext.util.Cookies.get('CSRF-TOKEN');

 */


var helloWorldUrl = SailPoint.CONTEXT_PATH + '/pluginPage.jsf?pn=HelloWorld';
jQuery(document).ready(function(){
	jQuery("ul.navbar-right li:first")
		.before(
				'<li class="dropdown">' +
				'		<a href="' + helloWorldUrl + '" tabindex="0" role="menuitem" data-snippet-debug="off">' +
				'			<i role="presenation" class="fa fa-exclamation fa-lg example"></i>' +
				'		</a>' +
				'</li>'
		);
});
