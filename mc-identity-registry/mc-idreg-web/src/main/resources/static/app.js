
angular.module('app', [])
    .controller('OidcCtrl', function($scope, $http) {

        $scope.user = undefined;
        $scope.error = undefined;

        $scope.init = function() {
            $http.get('/user')
                .success(function(data, status, headers, config) {
                    $scope.user = data;
                })
                .error(function(data, status, headers, config) {
                    console.error("Error fetching current user");
                });

            $http.get('/error')
                .success(function(data, status, headers, config) {
                    $scope.error = data;
                })
                .error(function(data, status, headers, config) {
                    console.error("Error fetching last error");
                });
        }
    });