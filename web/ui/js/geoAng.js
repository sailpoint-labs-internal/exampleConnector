'use strict';

var geo = angular.module('geoApp', ['ui.router', 'ngAnimate'])
    .constant('REST_SERVICE_URL_BASE', SailPoint.CONTEXT_PATH + '/' + PluginFramework.PluginBaseEndpointName + '/'
        + PluginFramework.CurrentPluginUniqueName)
    .constant('PLUGIN_URL_BASE', SailPoint.CONTEXT_PATH + '/' + PluginFramework.PluginFolderName + '/'
        + PluginFramework.CurrentPluginUniqueName)
    .constant('PLUGIN_ROOT_FOLDER_URL', PluginFramework.PluginFolderName)


    .config(function($stateProvider, $urlRouterProvider, PLUGIN_ROOT_FOLDER_URL) {
        $urlRouterProvider.when("", "/mainMenu");
        $stateProvider
            .state('mainMenu', {
                url: '/mainMenu',
                views: {
                    'main': { templateUrl: PLUGIN_ROOT_FOLDER_URL + '/GeoMap/ui/htmlTemplates/hello.html',
                        controller: function ($scope) {
                            console.log('loaded /mainMenu');
                        }
                    }
                }
            })
            .state('mapView', {
                url: '/mapView',
                views: {
                    'main': { templateUrl: PLUGIN_ROOT_FOLDER_URL + '/GeoMap/ui/htmlTemplates/theMap.html',
                        controller: function ($scope) {
                            $scope.id = 33;
                            console.log("loaded map");
                        }
                    }
                }
            })
            .state('particleView',{
                url: '/particleView',
                views: {
                    'main': { templateUrl: PLUGIN_ROOT_FOLDER_URL + '/GeoMap/ui/htmlTemplates/part.html',
                        controller: function ($scope) {
                            $scope.id = 33;
                            console.log("loaded map");
                        }
                    }
                }
            })
            .state('reportView', {
                url: '/reports',
                views: {
                    'main': { templateUrl: PLUGIN_ROOT_FOLDER_URL + '/GeoMap/ui/htmlTemplates/reports.html',
                        controller: function ($scope, $stateParams) {
                            $scope.id = 34;
                            console.log("loaded reports");
                        }
                    }
                }
            });
    })



.directive('slideable', function () {
    return {
        restrict:'C',
        compile: function (element, attr) {
            // wrap tag
            var contents = element.html();
            element.html('<div class="slideable_content" style="margin:0 !important; padding:0 !important" >' + contents + '</div>');

            return function postLink(scope, element, attrs) {
                // default properties
                attrs.duration = (!attrs.duration) ? '1s' : attrs.duration;
                attrs.easing = (!attrs.easing) ? 'ease-in-out' : attrs.easing;
                element.css({
                    'overflow': 'hidden',
                    'height': '0px',
                    'transitionProperty': 'height',
                    'transitionDuration': attrs.duration,
                    'transitionTimingFunction': attrs.easing
                });
            };
        }
    };
})
    .directive('slideToggle', function() {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                var target = document.querySelector(attrs.slideToggle);
                attrs.expanded = false;
                element.bind('click', function() {
                    var content = target.querySelector('.slideable_content');
                    if(!attrs.expanded) {
                        content.style.border = '1px solid rgba(0,0,0,0)';
                        var y = content.clientHeight;
                        content.style.border = 0;
                        target.style.height = y + 'px';
                    } else {
                        target.style.height = '0px';
                    }
                    attrs.expanded = !attrs.expanded;
                });
            }
        }
    });






