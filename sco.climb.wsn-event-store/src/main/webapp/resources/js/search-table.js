var searchTableApp = angular.module('search-table', ['DataService']);

var searchTableCtrl = searchTableApp.controller('userCtrl', function($scope, $http, $window, DataService) {
	DataService.getProfile().then(
	function(p) {
		$scope.initData(p);
	},
	function(e) {
		console.log(e);
		$scope.error = true;
		$scope.errorMsg = e.errorMsg;
	});

	$scope.selectedMenu = "ricerca";
	$scope.selectedTab = "menu-search-table";
	$scope.language = "it";
	$scope.defaultLang = "it";
	$scope.itemToDelete = "";

	$scope.edit = false;
	$scope.create = false;
	$scope.view = false;
	$scope.search = "";
	$scope.incomplete = false;

	$scope.error = false;
	$scope.errorMsg = "";

	$scope.ok = false;
	$scope.okMsg = "";

	$scope.data = null;
	$scope.status = 200;
	
	$scope.fEventType = -1;
	$scope.fRouteId = "5187f1cf-a6f0-4e4a-a025-cb2fe52a1061";
	$scope.eventTypeList = [];
	$scope.events = null;

	$scope.initData = function(profile) {
		$scope.profile = profile;
		$scope.eventTypeList = [
		 {
			 'name' : 'TUTTI',
			 'value' : -1
		 },
		 {
			 	'name' : 'NODE_IN_RANGE',
     		'value' : 101
     	},
     	{
     		'name' : 'NODE_CHECKIN',
     		'value' : 102
     	},
     	{
     		'name' : 'NODE_CHECKOUT',
     		'value' : 103
     	},
     	{
     		'name' : 'NODE_AT_DESTINATION',
     		'value' : 104
     	},
     	{
     		'name' : 'NODE_OUT_OF_RANGE',
     		'value' : 105
     	},
 		 {
 			 'name' : 'ANCHOR_IN_RANGE',
 			 'value' : 201
 		 },
		 {
			 'name' : 'STOP_REACHED',
			 'value' : 202
		 },
		 {
			 'name' : 'SET_DRIVER',
			 'value' : 301
		 },
		 {
			 'name' : 'SET_HELPER',
			 'value' : 302
		 },
		 {
			 'name' : 'DRIVER_POSITION',
			 'value' : 303
		 },
		 {
			 'name' : 'START_ROUTE',
			 'value' : 401
		 },
		 {
			 'name' : 'END_ROUTE',
			 'value' : 402
		 }
		];
	};
	
	$scope.doSearch = function(item) {
		var q = $scope.search.toLowerCase();
		var text;
		
		text = $scope.getEventName(item).toLowerCase();
		if(text.indexOf(q) != -1) {
			return true;
		}
		
		text = $scope.getEventTimestamp(item);
		if(text.indexOf(q) != -1) {
			return true;
		}
		
		if(item.wsnNodeId) {
			text = item.wsnNodeId.toLowerCase();
			if(text.indexOf(q) != -1) {
				return true;
			}
		}
		
		if(item.payload) {
			text = JSON.stringify(item.payload);
			if(text.indexOf(q) != -1) {
				return true;
			}
		}
		
		return false;
	};
	
	$scope.setNameMap = function(array) {
		var map = {};
		for (var d = 0, len = array.length; d < len; d += 1) {
			var key = array[d].objectId;
			var name = array[d].nome;
			map[key] = name;
		}
		return map;
	};

	$scope.setLocalNameMap = function(array) {
		var map = {};
		for (var d = 0, len = array.length; d < len; d += 1) {
			var key = array[d].objectId;
			var name = array[d].nome[$scope.language];
			map[key] = name;
		}
		return map;
	};

	$scope.resetError = function() {
		$scope.error = false;
		$scope.errorMsg = "";
	};

	$scope.resetOk = function() {
		$scope.ok = false;
		$scope.okMsg = "";
	};

	$scope.getModalHeaderClass = function() {
		if($scope.view) {
			return "view";
		}
		if($scope.edit) {
			return "edit";
		}
		if($scope.create) {
			return "create";
		}
	};

	$scope.setItemToDelete = function(id) {
		$scope.itemToDelete = id;
	};

	$scope.changeLanguage = function(language) {
		$scope.language = language;
	};

	$scope.resetUI = function() {
		$scope.search = "";
		$scope.incomplete = false;
		$('html,body').animate({scrollTop:0},0);
	};

	$scope.resetForm = function() {
		$sopne.fEventType = -1;
		$scope.fRouteId = "";
	};

	$scope.searchItem = function() {
		$window.spinner.spin($window.spinTarget);
		var dateFrom = "2016-03-11T09:00:00";
		var dateTo = "2016-03-11T12:00:00";
		
		var urlSearch = "api/event/" + $scope.profile.ownerId + "?routeId=" + $scope.fRouteId
		+ "&dateFrom=" + dateFrom + "&dateTo=" + dateTo;
		
		if($scope.fEventType > 0) {
			urlSearch = urlSearch + "&eventType[]=" + $scope.fEventType;
		}
		
		//console.log("urlSearch:" + urlSearch);
		$http.get(urlSearch, {headers: {'X-ACCESS-TOKEN': $scope.profile.token}}).then(
		function (response) {
			$scope.events = response.data;
			$window.spinner.stop();
		},
		function(response) {
			console.log(response.data);
			$scope.error = true;
			$scope.errorMsg = response.data.errorMsg;
			$window.spinner.stop();
		});
	}

	$scope.getEventName = function(item) {
		var result = "";
		switch (item.eventType) {
		case 101:
			result = "NODE_IN_RANGE";
			break;
		case 102:
			result = "NODE_CHECKIN";
			break;
		case 103:
			result = "NODE_CHECKOUT";
			break;
		case 104:
			result = "NODE_AT_DESTINATION";
			break;
		case 105:
			result = "NODE_OUT_OF_RANGE";
			break;
		case 201:
			result = "ANCHOR_IN_RANGE";
			break;
		case 202:
			result = "STOP_REACHED";
			break;
		case 301:
			result = "SET_DRIVER";
			break;
		case 302:
			result = "SET_HELPER";
			break;
		case 303:
			result = "DRIVER_POSITION";
			break;
		case 401:
			result = "START_ROUTE";
			break;
		case 402:
			result = "END_ROUTE";
			break;
		}
		return result;
	};
	
	$scope.getEventTimestamp = function(item) {
		var day = moment(item.timestamp);
		var result = day.format('DD/MM/YYYY, hh:mm:ss'); 
		return result;
	};
	
	$scope.copyItem = function(item) {
		text = JSON.stringify(item);
		
	};

	$scope.$watch('fRouteId',function() {$scope.test();}, true);

	$scope.test = function() {
		if(($scope.fRouteId == null) ||
		($scope.fRouteId.length < 3)) {
			$scope.incomplete = true;
		} else {
			$scope.incomplete = false;
		}
	};

	$scope.findByObjectId = function(array, id) {
    for (var d = 0, len = array.length; d < len; d += 1) {
      if (array[d].objectId === id) {
          return array[d];
      }
    }
    return null;
	};

	$scope.findIndex = function(array, id) {
		for (var d = 0, len = array.length; d < len; d += 1) {
			if (array[d].objectId === id) {
				return d;
			}
		}
		return -1;
	};

});
