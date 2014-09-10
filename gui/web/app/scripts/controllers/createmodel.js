'use strict';

/**
 * @ngdoc function
 * @name spGuiApp.controller:CreatemodelCtrl
 * @description
 * # CreatemodelCtrl
 * Controller of the spGuiApp
 */
var CreatemodelCtrl = function ($scope, $modalInstance, spTalker, notificationService, NAME_PATTERN) {
  $scope.enteredModelName = '';
  $scope.existingModels = spTalker.models;
  $scope.namePattern = NAME_PATTERN;

  $scope.saveModel = function(givenName) {
    var newModel = new spTalker.model();
    newModel.model = givenName;
    newModel.attributes = {};
    newModel.$save(function(savedModel) {
      notificationService.success('A new model \"' + savedModel.model + '\" was successfully created');
      spTalker.models.push(savedModel);
      spTalker.activeModel = savedModel;
      createDefaultSPSpec();
    }, function() {
      notificationService.error('The model creation failed.');
    });
    $scope.modelName = '';
  };

  function createDefaultSPSpec() {

    function onItemCreationSuccess(data) {
      spTalker.activeModel.attributes.activeSPSpec = data.id;
      spTalker.activeModel.attributes.children = [data.id];
      spTalker.activeModel.$save({modelID: spTalker.activeModel.model}, function(data) {
        notificationService.success('The new model ' + data.model + ' was successfully saved.');
        $modalInstance.close(data);
      });
    }

    spTalker.createItem('SPSpec', onItemCreationSuccess);
  }

  $scope.dismiss = function () {
    $modalInstance.dismiss('cancel');
  };
};
