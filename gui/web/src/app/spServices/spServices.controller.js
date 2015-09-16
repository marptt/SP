/**
 * Created by patrik on 2015-09-14.
 */
(function () {
    'use strict';

    angular
        .module('app.spServices')
        .controller('spServicesController', spServicesController);

    spServicesController.$inject = ['spServicesService', '$scope', 'dashboardService'];
    /* @ngInject */
    function spServicesController(spServicesService, $scope, dashboardService) {
        var vm = this;
        vm.widget = $scope.$parent.widget;
        vm.registeredServices = spServicesService.spServices;

        activate();

        function activate() {
            $scope.$on('closeRequest', function() {
                dashboardService.closeWidget(vm.widget.id);
            });
        }

    }
})();
