'use strict';

/**
 * @ngdoc directive
 * @name spGuiApp.directive:window
 * @description
 * # window
 */
angular.module('spGuiApp')
  .directive('windows', function () {
    return {
      templateUrl: 'views/window.html',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {



        scope.closeWindow = function(window) {
          var index = scope.windows.indexOf(window);
          scope.windows.splice(index, 1);
        };

        scope.toggleWindowWidth = function(window) {
          if(window.width === 'small'){
            window.width = 'large';
          } else {
            window.width = 'small';
          }
        };

        scope.toggleWindowHeight = function(window) {
          if(window.height === 'small'){
            window.height = 'large';
          } else {
            window.height = 'small';
          }
        };

        scope.sortableOptions = {
          /*start: function(event, ui) {
           ui.item.removeClass('sizeTransition');
           },
           stop: function(event, ui) {
           ui.item.addClass('sizeTransition');
           },*/
          handle: '.draggable'
        };

      }
    };
  });
