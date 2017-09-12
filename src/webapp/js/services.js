'use strict';

/* Services */

var dataServices = angular.module('dataServices', ['ngResource']);

dataServices.factory('httpInterceptor', function ($q, $rootScope, $log) {

    var numLoadings = 0;

    return {
        request: function (config) {

            numLoadings++;

            // Show loader
            $rootScope.$broadcast("loader_show");
            return config || $q.when(config)

        },
        response: function (response) {

            if ((--numLoadings) === 0) {
                // Hide loader
                $rootScope.$broadcast("loader_hide");
            }

            return response || $q.when(response);

        },
        responseError: function (response) {

            if (!(--numLoadings)) {
                // Hide loader
                $rootScope.$broadcast("loader_hide");
            }

            return $q.reject(response);
        }
    };
});

dataServices.directive("loader", function ($rootScope) {
        return function ($scope, element, attrs) {
            $scope.$on("loader_show", function () {
                return element.show();
            });
            return $scope.$on("loader_hide", function () {
                return element.hide();
            });
        };
    }
);

dataServices.service('SharedUserSparql', function(){
    var sharedData = "";
    var data = {data : sharedData};
    this.updateData = function(data1) {
        data.data = data1;
    };
    this.getData = function() {
        return data;
    };
});

dataServices.factory('Data', ['$resource',
    function($resource) {
        return $resource('phones/:phoneId.json', {phoneId: 'phones'}
            /* {getData:
             {method:'GET', params:{phoneId:'phones'}, isArray:true}
             } -- not necessary */
        );
    }
]);


dataServices.factory('LoginService', ['$resource',
    function($resource) {
        return $resource('http://130.206.30.27:8080/opendata4/xyz/user/:action', {},
            {
                authenticate: {
                    method: 'POST',
                    params: {'action' : 'authenticate'},
                    headers : {'Content-Type': 'application/x-www-form-urlencoded'}
                }
            }
        );
        /* {getData:
         {method:'GET', params:{phoneId:'phones'}, isArray:true}
         } -- not necessary */
    }
]);

dataServices.factory('UserService', ['$resource',
    function($resource) {
        return $resource('http://130.206.30.27:8080/opendata4/xyz/user/:id', {id: '@id'});
    }
]);


dataServices.factory('MenuService', ['$resource',
    function($resource) {
        return $resource('http://130.206.30.27:8080/opendata4/xyz/user/menu'
            /* {getData:
             {method:'GET', params:{phoneId:'phones'}, isArray:true}
             } -- not necessary */
        );
    }
]);

dataServices.factory('SparqlService',function($http, $q){
    var serverURL = "http://130.206.30.27:8080/opendata4/xyz";
    var data = [];
    var myService = {};
    var count = "";

    myService.count = function(resource) {
        return $http.get(serverURL+'/QueryResource/count/'+resource);
        /*var deffered = $q.defer();
        $http.get(serverURL+'/QueryResource/count/'+resource).success(
            function (d){
                count = d;
                deffered.resolve();
            }
        ).error(function(data,status,headers,config){
                console.log("Error al conectarse al servidor");
            });
        return deffered.promise;*/
    };

    myService.getCount = function(){  //carga datos de prueba
        return count;
    };

    myService.exportquery = function(params){
        return $http.get(serverURL+'/QueryResource/export/'+params);
    };

    myService.listLike = function(param){
        return $http.get(serverURL+'/QueryResource/listlike/'+param);
    };

    myService.plotQuery = function(params){
        return $http.get(serverURL+'/QueryResource/doquery/'+params+"/plot");
    };

    myService.plot = function(params){
        return $http.get(serverURL+'/QueryResource/list/'+params);
    };

    myService.doquery = function(params){
        return $http.get(serverURL+'/QueryResource/doquery/'+params);
        /*var deffered = $q.defer();
        $http.get(serverURL+'/QueryResource/doquery/'+params).success(
            function (d){
                data = d;
                deffered.resolve();
            }
        ).error(function(data,status,headers,config){
                console.log("Error al conectarse al servidor");
            });
        return deffered.promise;*/
    };


    myService.async = function(params){            //como no está en funcionamiento está función da un error
        var deffered = $q.defer();
        $http.get(serverURL+'/QueryResource/list/'+params).success(
            function (d){
                data = d;
                deffered.resolve();
            }
        ).error(function(data,status,headers,config){
                console.log("Error al conectarse al servidor");
            });
        return deffered.promise;
    };
    myService.data = function(){  //carga datos de prueba
        return data;
    };
    return myService;
});


dataServices.factory('SessionIdService', function() {
    var sessionID = '';
    return {
        getSessionId: function() {
            if(sessionID=='' || sessionID==null)
                sessionID = localStorage.getItem("sessionId");

            console.log("Get sessionId => " + sessionID);

            return sessionID;
        },

        setSessionId: function(sessId) {
            console.log("Set sessionId=" + sessId);
            localStorage.setItem("sessionId", sessId);
            sessionID = sessId;
            return;
        }
    }
});