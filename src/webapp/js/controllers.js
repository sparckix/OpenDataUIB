'use strict';


function doAfter(toWhere) {
    var i = 0;
    while(((window.location.href != "http://130.206.30.28:8080/opendata4/#/sparql")
        ||  (window.location.href != "http://130.206.30.28:8080/opendata4/#/userpage")) && i < 10) {
        console.log(window.location.href);
        window.location.href = toWhere;
        i++;
    }
}

function show_coords(event)
{
window.setTimeout(function() {
    doAfter(event.target.href)},0);
}
/* Controllers */

var phonecatApp = angular.module('phonecatApp', []);
var sparql = angular.module('sparql', []);


phonecatApp.controller('PhoneListCtrl', function($scope) {
    $scope.phones = [
        {'name': 'Nexus S',
            'snippet': 'Fast just got faster with Nexus S.',
            'age': 1},
        {'name': 'Motorola XOOM™ with Wi-Fi',
            'snippet': 'The Next, Next Generation tablet.',
            'age': 2},
        {'name': 'MOTOROLA XOOM™',
            'snippet': 'The Next, Next Generation tablet.',
            'age': 3}
    ];

    $scope.orderProp = 'age';
});

var mainModule = angular.module('main', ['ui.bootstrap.tpls', 'ui.bootstrap.pagination', 'ui.bootstrap.alert', 'ui.bootstrap.buttons', 'ui.bootstrap.dropdownToggle',
    'ui.bootstrap.tooltip', 'ui.bootstrap.popover', 'ngRoute', 'ui.ace', 'pascalprecht.translate',
    'ngAnimate', 'dataServices', 'dataDirectives', 'ngSanitize', 'OpendataFilter', 'LocalStorageModule', 'fundoo.services', 'ngCookies', 'duScroll', 'ui.filters', 'decipher.history']);

mainModule.config(['$routeProvider', '$locationProvider', '$httpProvider', '$translateProvider',
    function($routeProvider, $locationProvider, $httpProvider, $translateProvider) {
        $translateProvider.useStaticFilesLoader({
            prefix: 'languages/',
            suffix: '.json'
        });
        $translateProvider.preferredLanguage('ca');

        $routeProvider.
            when('/main', {
                templateUrl: 'main.html'
                //controller: 'PhoneListCtrl'
            }).
            when('/sparql', {
                templateUrl: 'sparql.html',
                controller: 'SparqlCtrl'
            }).
            when('/admin', {
                templateUrl: 'admin.html',
                controller: 'AdminCtrl'
            }).
            when('/explore', {
                templateUrl: 'explore.html',
                controller: 'ExploreCtrl'
            }).
            when('/team', {
                templateUrl: 'team.html'
                //controller: 'ExploreCtrl'
            }).
            when('/project', {
                templateUrl: 'project.html'
                //controller: 'ExploreCtrl'
            }).
            when('/result', {
                templateUrl: 'result.html',
                controller: 'SparqlCtrl'
            }).
            when('/resource/:resourceId', {
                templateUrl: 'detail.html',
                controller: 'ResourceDetailCtrl'
            }).
            when('/login', {
                templateUrl: 'login.html',
                controller: 'LoginController'
            }).
            when('/catalogo', {
                templateUrl: 'catalogo.html',
                controller: 'CatalogoCtrl'
            }).
            when('/userpage', {
                templateUrl: 'userpage.html',
                controller: 'UserpageCtrl'
            }).
            otherwise({
                redirectTo: '/main'
            });
        /* Intercept http errors */
        var interceptor = function ($rootScope, $q, $location) {

            function success(response) {
                return response;
            }
            function error(response) {

                var status = response.status;
                var config = response.config;
                var method = config.method;
                var url = config.url;

                if (status == 401) {
                    $cookieStore.remove('user');
                    $location.path( "/main" );
                } else {
                    //$rootScope.error = method + " on " + url + " failed with status " + status;
                }

                return $q.reject(response);
            }

            return function (promise) {
                return promise.then(success, error);
            };
        }
        $httpProvider.interceptors.push('httpInterceptor');
        $httpProvider.responseInterceptors.push(interceptor);
    }]);

mainModule.run(function($rootScope, $http, $location, $cookieStore, localStorageService) {

    /* Reset error when a new view is loaded */
    $rootScope.$on('$viewContentLoaded', function() {
        delete $rootScope.error;
    });

    $rootScope.hasRole = function(role) {

        if ($rootScope.user === undefined) {
            return false;
        }

        if ($rootScope.user.roles[role] === undefined) {
            return false;
        }

        return $rootScope.user.roles[role];
    };

    $rootScope.logout = function() {
        delete $rootScope.user;
        delete $http.defaults.headers.common['X-Auth-Token'];
        $cookieStore.remove('user');
        $location.path("/main");
    };

    /* Try getting valid user from cookie or go to login page -> NOT NEEDED*/
    var originalPath = $location.path();
//    $location.path("/main");
    var user = $cookieStore.get('user');
    if (user !== undefined) {
        $rootScope.user = user;
        $http.defaults.headers.common['X-Auth-Token'] = user.token;

        $location.path(originalPath);
    }
    // register listener to watch route changes
    /*$rootScope.$on("$routeChangeStart", function(event, next, current) {

     console.log("Routechanged sessionId="+ localStorageService.get("sessionId"));

     if (localStorageService.get("sessionId") == '' || localStorageService.get("sessionId") == null) {

     // no logged user, we should be going to #login
     if (next.templateUrl == "login") {
     // already going to #login, no redirect needed
     } else {
     // not going to #login, we should redirect now
     $location.path("/login");
     }
     }
     });*/
});
mainModule.service('JSONService', function($http){
    return {
        getJSON: function() {
            return $http.get('extras/local.json');
        }
    };
});

mainModule.controller('ResourceDetailCtrl',
    function($scope, $routeParams, Data) {
        $scope.resource = Data.get({phoneId: $routeParams.resourceId});
    });


mainModule.controller('LoginController', function ($scope, $rootScope, $http, $location, $cookieStore, localStorageService, createDialog, LoginService) {
    $scope.user1 = {};
    $scope.user1.email = '';
    $scope.user1.password = '';

    $scope.login = function() {
        LoginService.authenticate($.param({username: $scope.user1.email, password: $scope.user1.password}), function(user) {
            $rootScope.user = user;
            $http.defaults.headers.common['X-Auth-Token'] = user.token;
            $cookieStore.put('user', user);
            $scope.$modalClose();
            $location.path("/");
        });
    };
    $scope.loginUser = function(user1) {
        var config = {headers:  {'Content-Type': 'application/json'}};

        $scope.resetError();

        $http.post("http://130.206.30.27:8080/opendata4/xyz/user/authenticate", $scope.user1,config).success(function(login) {
            if(login.sessionId===null) {
                $scope.setError(login.status);
                return;
            }
            localStorageService.add("sessionId",login.sessionId);
            localStorageService.add("email",$scope.user1.email);

            $scope.user1.email = '';
            $scope.user1.password = '';


            $location.path("main");

        }).error(function() {
                $scope.setError('Invalid user/password combination');
            });
    }

    $scope.resetError = function() {
        $scope.error = false;
        $scope.errorMessage = '';
    }

    $scope.setError = function(message) {
        $scope.error = true;
        $scope.errorMessage = message;
    }

    $scope.registerModal = function()
    {
        createDialog('signin.html',{
            id : 'registerModal',
            title: 'Register',
            backdrop: true
            /*,
             success: {label: 'Submit',fn: function(){console.log('Successfully closed modal');}}*/
        });
    }
});

mainModule.controller('CatalogoCtrl', function($scope, $timeout, $http, scroller, Data) {

});
mainModule.controller('UserpageCtrl', function($scope, $timeout, $http, SparqlService, scroller, Data, namespacesFilter, $location, $rootScope, SharedUserSparql) {
    $scope.alerts = [];
    $scope.showForm = false;
    $scope.queryList = [];
    $scope.submitted = 'false';
    $scope.notSubmitted = 'true';

    $scope.numPerPage = 10;
    $scope.currentPage = 1;
    $scope.maxSize = 5;

    //$scope.people = Data.query();
    $http.get("http://130.206.30.27:8080/opendata4/xyz/querys/list").then(function (result) {
        $scope.queryList =  result.data;
        $scope.filteredList = $scope.queryList.slice(0, $scope.numPerPage);
    });
    $scope.updateQuery = function(snippet) {
        SharedUserSparql.updateData(decodeURIComponent(snippet));
    };

    $scope.getTotalItems = function () {
        return $scope.queryList.length;
    };

    $scope.$watch('currentPage + numPerPage', function() {
        var begin = (($scope.currentPage - 1) * $scope.numPerPage)
            , end = begin + $scope.numPerPage;
        $scope.filteredList = $scope.queryList.slice(begin,end);
        //$scope.filteredList.$promise.then(function (result) {
         //   $scope.people =  result;
         //   $scope.filteredPeople = $scope.people.slice(begin, end);

        //});
    });
});

mainModule.controller('SparqlCtrl', function($scope, $timeout, $http, SparqlService, scroller, Data, namespacesFilter, $location, $rootScope, SharedUserSparql) {
    $scope.alerts = [];
    $scope.showForm = false;
    $scope.people = [];
    $scope.submitted = false;
    $scope.notSubmitted = 'true';
    //$scope.people = Data.query();
    /*$http.get("http://130.206.30.27:8080/opendata4/xyz/querys/list").then(function (result) {
        $scope.people =  result.data;
        $scope.filteredPeople = $scope.people.slice(0, 10);
    });*/

    $scope.numPerPage = 10;
    $scope.currentPage = 1;
    $scope.maxSize = 5;


    $scope.goTo = function (where){
        $scope.goTo = function() {
            scroller.scrollToElement(document.getElementById(where), 30, 1500);
        }
    };

    /*$scope.getTotalItems = function () {
        return $scope.people.length;
    };*/

    $scope.getJSON = function(){
        $scope.people = Data.query();
    };

    /*$scope.$watch('currentPage + numPerPage', function() {
        var begin = (($scope.currentPage - 1) * $scope.numPerPage)
            , end = begin + $scope.numPerPage;
        $scope.people.$promise.then(function (result) {
            $scope.people =  result;
            $scope.filteredPeople = $scope.people.slice(begin, end);

        });
    });*/

    $scope.removeList = function(){
        //$scope.people = [];
        $scope.submitted = false;
        $scope.notSubmitted = true;
    };

    $scope.modes = ['Scheme', 'XML', 'Javascript'];
    $scope.mode = $scope.modes[0];


    // The ui-ace option
    $scope.aceOption = {
        mode: $scope.mode.toLowerCase(),
        onLoad: function (_ace) {

            // HACK to have the ace instance in the scope...
            $scope.modeChanged = function () {
                _ace.getSession().setMode("ace/mode/" + $scope.mode.toLowerCase());
            };

        }
    };

    $scope.addAlert = function(alert) {
        $scope.alerts.push(alert);
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.updateQuery = function(snippet) {
        $scope.sparqlQuery = snippet;
        $scope.goTo('sparql-1');
    };

    $scope.saveQuery = function(what) {
        if (what !== undefined) {
            $scope.consulta = {};
            $scope.consulta.nombreCons = what;
            $scope.consulta.consulta = encodeURIComponent($scope.sparqlQuery.data);
            $http.post("http://130.206.30.27:8080/opendata4/xyz/querys",$scope.consulta).success(function(data) {
                $scope.addAlert({type: "success", msg: "Query successfully saved. Check it out in your query page"});
            }).error(function(data) {
                    $scope.addAlert({type: "danger", msg: "An error occurred, please try saving it again later"});
                });
        }
    };

    $scope.clearQuery = function() {
        $scope.sparqlQuery.data = "";
        $scope.submitted = false;
        $scope.recordList = [];
    };

    $scope.submitQuery = function() {
        $scope.submitted = true;
        //$scope.notSubmitted = 'false';
        //$scope.getJSON();
        var realQuery = encodeURIComponent($scope.sparqlQuery.data);
        SparqlService.doquery(realQuery).then(function(data) {
            $scope.exportURL = "http://130.206.30.27:8080/opendata4/xyz/QueryResource/export/" + encodeURIComponent($scope.sparqlQuery.data);
            $scope.recordList = data.data;
            $scope.recordList.forEach(function(value) {
                for(var property in value) {
                    value[property] = namespacesFilter(value[property]);
                }
            });
        },function(error) {
            $scope.addAlert({type: "danger", msg: "An error occurred, please check your syntax"});
            $scope.submitted = false;
        });

        /*
         $scope.consulta = {};
         $scope.consulta.nombreCons = "nombre consulta";
         $scope.consulta.consulta = "consulta toguapa en si";
         $http.post("http://130.206.30.27:8080/opendata4/xyz/querys",$scope.consulta).success(function(data) {
         $scope.setError(data);
         }).error(function(data) {
         $scope.setError(data);
         });
         */
    };
    $scope.plotQuery = function() {
        var realQuery = encodeURIComponent($scope.sparqlQuery.data);
        SparqlService.plotQuery(realQuery).then(function(data) {
            var result = data.data.nodes;
            result.forEach(function(value) {
                value["name"] = namespacesFilter(value["name"]);
            });
            localStorage.setItem("myData", JSON.stringify(data.data));
            console.log(data.data);
            window.open("catalogo.html","","width=1024,height=1024");
        });
    };

    // Initial code content...
    SharedUserSparql.updateData("select ?personas ?publicacion ?fecha  WHERE  { \n\t ?personas rdf:type uib:Investigador . \n\t ?personas rdfs:label "
        + "\"JuizGarciaCarlos\" .  \n\t ?personas uib:producir ?publicacion .  \n\t ?publicacion uib:fecha ?fecha . \n }");
    $scope.sparqlQuery = SharedUserSparql.getData();

    $scope.goExplore = function(what) {
        //tweaked. instead of events handle it with rootScope (safer and better actually)
//        $rootScope.$broadcast("myEvent", {querywhat: what});
//        $timeout(function() { $location.path("/explore"); }, 0);
        $rootScope.querywhat = what;
        $location.path("/explore");
    }


});

/*mainModule.controller('TypeaheadCtrl', ['$scope', function ($scope) {

    $scope.selected = undefined;
    $scope.states = ['Isaac Lera', 'mola mucho', 'Yo', 'loves', 'Maria', 'Isaac Gobernador', 'SPARQL bonito' ];
}]);*/

mainModule.controller('HeaderController', function ($scope, $rootScope, $translate, $location, $route, $animate, localStorageService, createDialog, MenuService, SparqlService) {
    MenuService.get(function data(result) {
        $scope.nav = result.menuItem;
    });
    $scope.username = localStorageService.get("email");
    $scope.loggedIn = (localStorageService.get("sessionId") != null || localStorageService.get("sessionId") != '');

    /*$scope.logout = function() {
     localStorageService.clearAll();
     $location.path("/login");
     }*/

    $scope.changeLanguage = function (langKey) {
        $translate.use(langKey);
    };

    $scope.listLike = function() {
        $rootScope.searchLike = $scope.selected;
        delete $scope.selected;
        if ($location.path().indexOf("explore") != -1) {
            // tweak. we're already in explore. simply reload
            $route.reload();
        }
        else {
            $location.path("/explore");
        }
    };

    $scope.isActive = function (viewLocation) {
        return viewLocation === $location.path();
    };

    $scope.registerModal = function()
    {
        createDialog('login.html',{
            id : 'registerLoginModal',
            title: 'Log in',
            backdrop: true
            /*,
             success: {label: 'Submit',fn: function(){console.log('Successfully closed modal');}}*/
        });
    }
});

mainModule.controller('ExploreCtrl', function ($scope, $timeout, $sanitize, namespacesFilter, UserService, SparqlService, $rootScope, History) {
    $scope.focus = true;
    $scope.numberList = ['10','50','100','200','300'];
    $scope.currentPage = 1;
    $scope.totalItems = 0;
    $scope.shouldDisable = false;
    $scope.resultsExist = false;
    /* Query parameters management*/
    $scope.perPage = 50;
	$scope.maxSize = 5;
//    $scope.lastSearch = "uib:producir";
    $scope.offset = 0;
    $scope.queryParams = [$scope.lastSearch,$scope.perPage,$scope.offset];

    History.watch('recordList', $scope);
    History.watch('shouldDisable',$scope);

    if($rootScope.searchLike !== undefined) {
        SparqlService.listLike($rootScope.searchLike).then(function(data) {
            $scope.recordList = data.data
            $scope.recordList.forEach(function(value) {
                var value1 = namespacesFilter(value);
                //value.enlace = value1.subject;
            });
            $scope.shouldDisable = true;
        });
        delete $rootScope.searchLike;
    }


    /*    SparqlService.count(encodeURIComponent($scope.queryParams[0])).then(function() {
     $scope.totalItems = SparqlService.getCount();
     });
     */
    /*    SparqlService.async($scope.queryParams.join("/")).then(function() {
     $scope.fullList = SparqlService.data();
     $scope.swap = [];
     angular.copy($scope.fullList,$scope.swap);
     $scope.recordList = $scope.swap;
     $scope.recordList.forEach(function(value) {
     var value1 = namespacesFilter(value);
     value.enlace = $sanitize(value1.subject);
     });
     });
     */
    $scope.showNamespace = function() {
        if(!$scope.showFullURI) { //recordList tiene lo que queremos conservar
            $scope.recordList = $scope.fullList;
//          var temp = [];
//          angular.copy($scope.recordList,temp);
//            $scope.recordList = $scope.fullList;
//          $scope.fullList = temp;
//          angular.copy(temp,$scope.fullList);
        }
        else { //hay que recuperar el contenido de fullList y conservar recordList
            $scope.recordList = $scope.swap;
//          var temp = [];
//          angular.copy($scope.recordList,temp);
//          $scope.recordList = $scope.fullList;
//          $scope.fullList = temp;
//          angular.copy($scope.recordList,$scope.fullList);
//            angular.forEach($scope.recordList, function(value) {
            //                value = namespacesFilter(value);
//            });
        }
    }

    $scope.filterExplore = function() {
        if (!$scope.predicateFilter) {
            /*alert("Deberia llamar a la API con texto de filtro: " + $scope.filterText + ' ' +
             'y filtrado por el predicado: ' + $scope.predicateFilter.predicate +
             " y resultados por pagina: " + $scope.perPage);*/
            //$scope.goExplore($scope.predicateFilter.predicate);
            if ($scope.filterText) {
                $scope.goExplore($scope.filterText);
            }
            else {
                //No hay filtro de predicate ni filtro de text
                return;
            }
        }
        else {
            if ($scope.filterText) {
                //Predicado de filtro y texto
                $scope.goExplore($scope.filterText, $scope.predicateFilter.predicate);
            }
            else {
                //Predicado de filtro sin texto
                $scope.goExplore($scope.lastSearch);

            }

        }
    };

    $scope.goExplore = function(what,predicateFilter) {
        $scope.shouldDisable = false;
        var predicate = "";
        if ($scope.lastSearch != what) {
            //nueva busqueda, resetear pagina (refire listener on watch and handle next time).
            $scope.lastSearch = what;
            $scope.queryParams[0] = what;
            if ($scope.currentPage != 1) {
                $scope.currentPage = 1;
                return;
            }
        }
        if($scope.showFullURI) {
            what = namespacesFilter(what);
        }
        //alert("Deberia llamar a la API para pedir más info acerca de: " + what);
        $scope.queryParams[0] = encodeURIComponent(what);
        $scope.queryParams[1] = $scope.perPage;

        if(typeof(predicateFilter) !== 'undefined') {
            predicate = "/"+encodeURIComponent(predicateFilter);
            $scope.lastFilter = predicateFilter;
            $scope.predicateFilter = null;
        }

        SparqlService.count($scope.queryParams[0].concat(predicate)).then(function(data) {
            $scope.totalItems = data.data;
            if ($scope.totalItems == 0) {
                $scope.predicateFilter = null;
                $scope.resultsExist = false;
            }
            else {
                $scope.resultsExist = true;
            }
            SparqlService.async($scope.queryParams.join("/").concat(predicate)).then(function() {
                $scope.fullList = SparqlService.data();
                $scope.swap = [];
                angular.copy($scope.fullList,$scope.swap);
                $scope.recordList = $scope.swap;

                $scope.recordList.forEach(function(value) {
                    var value1 = namespacesFilter(value);
                    //value.enlace = value1.subject;
                });
                $scope.showFullURI = false;
                //$scope.recordList = SparqlService.data();
            });

        },function(error) {
            //TODO SHOW SOMETHING
            $scope.resultsExist = false;
        });


        /*SparqlService.async($scope.queryParams.join("/").concat(predicate)).then(function() {
            $scope.fullList = SparqlService.data();
            $scope.swap = [];
            angular.copy($scope.fullList,$scope.swap);
            $scope.recordList = $scope.swap;

            $scope.recordList.forEach(function(value) {
                var value1 = namespacesFilter(value);
                //value.enlace = value1.subject;
            });
            $scope.showFullURI = false;
            //$scope.recordList = SparqlService.data();
        });*/

    };

    if($rootScope.querywhat !== undefined) {
        $scope.lastSearch = $rootScope.querywhat;
        $scope.goExplore($rootScope.querywhat);
        delete $rootScope.querywhat;
    }

    $scope.searchKey = function (){
    }

    $scope.setRecord = function(record) {
        $scope.recordList1 = record;
    };
    /*$rootScope.$on("myEvent", function (event, args) {
        event.currentScope.lastSearch = args.querywhat;
        //TODO fix this ugly hack that might not work actually
        event.currentScope.currentPage = 1;
    });*/

    $scope.$watch('currentPage + perPage', function() {
        if ($scope.lastSearch !== undefined) {
            $scope.queryParams[1] = $scope.perPage;
            $scope.queryParams[2] = ($scope.currentPage-1)* $scope.queryParams[1];
            $scope.goExplore($scope.lastSearch);
        }
    });

    $scope.$on("$locationChangeStart", function(event, next, current) {
        //Ugly hack next == sparql. Find alternative way and fix
        if(History.canUndo('recordList', $scope) && ((next.indexOf("sparql") != -1) || (next.indexOf("userpage") != -1))) {
            History.undo('recordList', $scope);
            //TODO bug shouldDisable back
            History.undo('shouldDisable',$scope);
            if ($scope.recordList !== undefined) {
                event.preventDefault();
                $scope.$digest();
            }
        }
    });

    $scope.plot = function() {
        var predicate = "";
        if(typeof($scope.lastFilter) !== 'undefined') {
            predicate = "/"+encodeURIComponent($scope.lastFilter);
            $scope.predicateFilter = null;
        }

//        SparqlService.plot($scope.queryParams.join("/").concat(predicate).concat("/plot")).then(function(data) {     
        SparqlService.plot($scope.queryParams.join("/").concat("/plot").concat(predicate)).then(function(data) {
            var result = data.data.nodes;
            result.forEach(function(value) {
                for(var property in value) {
                    value[property] = namespacesFilter(value[property]);
                }
            });
            localStorage.setItem("myData", JSON.stringify(data.data));
            console.log(data.data);
            window.open("catalogo.html");
        });
    };


});


mainModule.controller('AdminCtrl', function ($scope, UserService, $timeout) {
    $scope.nothingSelected = true;
    $scope.userList = UserService.query();

    $scope.selectAll = function() {
        $scope.master = true;
        $scope.onMasterChange1();
    };

    $scope.unselectAll = function() {
        $scope.master = false;
        $scope.onMasterChange1();
    };

    $scope.selectAllowed = function() {
        for(var i=0; i < $scope.userList.length; i++) {
            if ($scope.userList[i].accountNonLocked)
                $scope.userList[i].cb = true;
            else
                $scope.userList[i].cb = false;
        }
        $scope.onMasterChange();
    };

    $scope.selectBlocked = function() {
        for(var i=0; i < $scope.userList.length; i++) {
            if (!$scope.userList[i].accountNonLocked)
                $scope.userList[i].cb = true;
            else
                $scope.userList[i].cb = false;
        }
        $scope.onMasterChange();
    };

    $scope.deleteUsers = function() {
        for(var i=0; i < $scope.userList.length; i++) {
            if ($scope.userList[i].cb) {
                $scope.userList[i].$remove();
            }
        }
        $scope.nothingSelected = true;
        $scope.master = false;
        $timeout(function() { $scope.refreshUserList(); }, 1000);
    };

    $scope.allowUsers = function(allow) {
        for(var i=0; i < $scope.userList.length; i++) {
            if ($scope.userList[i].cb) {
                $scope.userList[i].accountNonLocked = allow;
                $scope.userList[i].$save();
            }
        }
        $scope.nothingSelected = true;
        $scope.master = false;
        $timeout(function() { $scope.refreshUserList(); }, 1000);
    };

    $scope.refreshUserList = function() {
        $scope.userList = UserService.query();
    };

    $scope.onMasterChange1 = function() {
        for(var i=0; i < $scope.userList.length; i++) {
            $scope.userList[i].cb = $scope.master;
        }
        if ($scope.master)
            $scope.nothingSelected = false;
        else
            $scope.nothingSelected = true;

    };

    $scope.onMasterChange = function() {
        var found = false;
        if ($scope.master)
            found = true;
        for(var i=0; !found && i < $scope.userList.length; i++) {
            if ($scope.userList[i].cb)
                found = true;
        }
        if (found) {
            $scope.nothingSelected = false;
        }
        else {
            $scope.nothingSelected = true;

        }

    };

});


function SigninCtrl($scope, $http) {
    //$scope.user = new User();
    var config = {headers:  {'Content-Type': 'application/json'}};
    $scope.user = {};
    $scope.user.username = '';
    $scope.user.password = '';
    $scope.submitRegistration = function (user) {
        $scope.resetError();
        $http.post("http://130.206.30.27:8080/opendata4/xyz/user/new",$scope.user,config).success(function(data) {
            $scope.setError(data);
        }).error(function(data) {
                $scope.setError(data);
            });
        //$scope.user.$save();
    };
    $scope.resetError = function() {
        $scope.error = false;
        $scope.errorMessage = '';
    }

    $scope.setError = function(message) {
        $scope.error = true;
        $scope.errorMessage = message;
        //$rootScope.SessionId='';
    }
};
