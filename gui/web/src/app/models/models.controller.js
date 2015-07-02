(function () {
    'use strict';

    angular
        .module('app.models')
        .controller('ModelsController', ModelsController);

    ModelsController.$inject = ['model', '$window', '$modal'];
    /* @ngInject */
    function ModelsController(model, $window, $modal) {
        const vm = this;
        vm.title = 'Models';
        vm.models = model.models;
        vm.displayedModels = [];
        vm.createModel = createModel;
        vm.updateName = model.updateName;
        vm.deleteModel = deleteModel;
        vm.setActiveModel = model.setActiveModel;

        function deleteModel(id) {
            const sure = $window.confirm('Are you sure you want to delete the whole model?');
            if(sure) {
                model.deleteModel(id);
            }
        }

        function createModel() {
            var modalInstance = $modal.open({
                templateUrl: '/app/models/createmodel.html',
                controller: 'CreateModelController',
                controllerAs: 'vm'
            });

            modalInstance.result.then(function(chosenName) {
                model.createModel(chosenName)
            });
        }

    }
})();
