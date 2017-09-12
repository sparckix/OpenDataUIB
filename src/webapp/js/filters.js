'use strict';

/* Filters */

var myFilter = angular.module('OpendataFilter', []);
myFilter.filter('iif', function () {
    return function(input, trueValue, falseValue) {
        return input ? trueValue : falseValue;
    };
});

myFilter.filter('namespaces', function(){
    //return text.replace(/\n/g, '<br/>');
    /* Conversion tableuib
     uib      http://datesobertes.uib.es/memoria2012.owl#
     rdfs     http://www.w3.org/2000/01/rdf-schema#
     psys     http://proton.semanticweb.org/protonsys#
     owl      http://www.w3.org/2002/07/owl#
     xsd      http://www.w3.org/2001/XMLSchema#
     rdf      http://www.w3.org/1999/02/22-rdf-syntax-ns#
     pext     http://proton.semanticweb.org/protonext#
     */

    var changeNamespace = function(value) {
        var index = "";
        if( (index = value.indexOf("memoria2012.owl#")) >= 0 ) { value =  value.replace("http://datesobertes.uib.es/memoria2012.owl#","uib:"); return value; }
        if( (index = value.indexOf("rdf-schema#")) >= 0 ) { value =  value.replace("http://www.w3.org/2000/01/rdf-schema#","rdfs:"); return value; }
        if( (index = value.indexOf("protonsys#")) >= 0 ) {  value =  value.replace("http://proton.semanticweb.org/protonsys#","psys:"); return value; }
        if( (index = value.indexOf("XMLSchema#")) >= 0 ) { value =  value.replace("http://www.w3.org/2001/XMLSchema#","xsd:");  return value; }
        if( (index = value.indexOf("07/owl#")) >= 0 ) { value =  value.replace("http://www.w3.org/2002/07/owl#","owl:"); return value; }
        if( (index = value.indexOf("rdf-syntax-ns#")) >= 0 ) { value =  value.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:"); return value; }
        if( (index = value.indexOf("protonext#")) >= 0 ) { value =  value.replace("http://proton.semanticweb.org/protonext#","pext:"); return value; }
        return value;
    };

    return function(SPOObject) {
        if (SPOObject instanceof Object) {
            SPOObject.subject = changeNamespace(SPOObject.subject);
            SPOObject.predicate = changeNamespace(SPOObject.predicate);
            SPOObject.object = changeNamespace(SPOObject.object);
            return SPOObject;
        }
        else {
            return changeNamespace(SPOObject);
        }
    };
});