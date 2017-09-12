'use strict';

/* Directives */
var dataDirectives = angular.module('dataDirectives', []);

dataDirectives.directive('focusMe', function($timeout) {
    return {
        scope: { trigger: '=focusMe' },
        link: function(scope, element) {
            scope.$watch('trigger', function(value) {
                if(value === true) {
                    element[0].focus();
                    scope.trigger = false;
                }
            });
        }
    };
});

