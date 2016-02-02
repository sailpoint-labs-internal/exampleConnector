jQuery(document).ready(function(){
	jQuery("ul.navbar-right li:first")
		.before(
				'<li>' +
				'		<a href="/identityiq/pluginPage.jsf?pn=HelloWorld" tabindex="0" role="menuitem">' +
				'			<i role="presenation" class="fa fa-exclamation fa-lg"></i>' +
				'		</a>' +
				'</li>'
		);
});