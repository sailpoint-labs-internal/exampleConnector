var helloWorldUrl = SailPoint.CONTEXT_PATH + '/pluginPage.jsf?pn=customConnector';
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

