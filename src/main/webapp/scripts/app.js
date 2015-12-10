/*global angular, console, WebSocket*/
(function () {
    "use strict";
    var BACKEND_IP = "192.168.1.1",
        webs,
        defaultItems = [{
            title : "Здесь будут новостные сообщения",
            description : "Войдите или зарегистрируйтесь, чтобы начать работать с системой",
        }],
        defaultFeeds = [],
		defaultCategories = [{
			new_items: 30,
			title: "Ваши фиды"
		}],
		items = [],
		feeds = [],
		categories = [],
        app;
    
    app = angular.module('feedView', []);
    
    
    app.controller('userControllers',  ['$scope', '$rootScope', function ($scope, $rootScope) {
		$scope.userControllers = this;
		
		categories = defaultCategories;
		feeds = defaultFeeds;
		items = defaultItems;
		this.showControls = false;
		this.loggedOut = true;
		this.loginClicked = false;
		this.registerClicked = false;
		this.showSignIn = true;
		this.showSignUp = true;
		this.signin_title = "Sign In";
		
		$scope.$on('auth_success', function () {
			console.log("Received auth_success");
			$scope.userControllers.loggedOut = false;
			$scope.userControllers.showControls = false;
			$scope.userControllers.loginClicked = false;
			$scope.userControllers.registerClicked = false;
			$scope.userControllers.showSignIn = true;
			$scope.userControllers.showSignUp = false;
			$scope.userControllers.signin_title = "Sign out";
		
		});
		
		$scope.$on('logged-out', function () {
			categories = defaultCategories;
			feeds = defaultFeeds;
			items = defaultItems;
			$scope.userControllers.loggedOut = true;
			$scope.userControllers.showControls = false;
			$scope.userControllers.loginClicked = false;
			$scope.userControllers.registerClicked = false;
			$scope.userControllers.showSignIn = true;
			$scope.userControllers.showSignUp = true;
			$scope.userControllers.signin_title = "Sign in";
		});
		
		$scope.register = function () {
			console.log("Register button clicked");
			$scope.userControllers.showSignIn = true;
			$scope.userControllers.showSignUp = false;
			$scope.userControllers.loginClicked = false;
			$scope.userControllers.registerClicked = $scope.userControllers.registerClicked ? false : true;
		};
		
		$scope.registerSend = function () {
			console.log("registerSend clicked");
			var login = $("#registerLogin").val(),
				name = $("#registerName").val(),
				password = $("#registerPassword").val(),
				message;
				
				//TODO verify register data
				
				message = {action: "register",
								login: login,
								name: name,
								password: password
								};
			console.log(JSON.stringify(message));
			$rootScope.$broadcast('register-clicked', {message: message});
			$scope.userControllers.registerClicked = false;
		};
		
		$scope.login = function () {
			if($scope.userControllers.loggedOut) {
				console.log("Login button clicked");
				$scope.userControllers.showSignIn = false;
				$scope.userControllers.showSignUp = true;
				$scope.userControllers.registerClicked = false;
				$scope.userControllers.loginClicked = $scope.userControllers.loginClicked ? false : true;
			} else {
				$rootScope.$broadcast('sign-out');
			}
		};
		
		$scope.loginSend = function () {
			console.log("loginSend clicked");
			var login = $("#loginLogin").val(),
				password = $("#loginPassword").val(),
				message;
				
				//TODO verify login data
				
				message = {action: "login",
									login: login,
									password: password
								};
			$rootScope.$broadcast('login-clicked', {message: message});
			$scope.userControllers.loginClicked = false;
		};
		
		$scope.showhideControls = function () {
			if($scope.userControllers.showControls){ //hided controls window
				$scope.userControllers.showControls = false;
				$scope.userControllers.LoginClicked = false;
				$scope.userControllers.registerClicked = false;
			} else {
				$scope.userControllers.showControls = true;
			}
			$scope.userControllers.LoginClicked = false;
			$scope.userControllers.registerClicked = false;
		};
		
		$scope.addFeed = function () {
			var feedUrl = $("#feedUrl").val(),
			categoriesCheckboxes = document.getElementsByClassName("categoryCheckbox"),
			categoryToAdd,
			i = 0;
			
			for(; i < categoriesCheckboxes.length; i++) {
				if(categoriesCheckboxes[i].checked) {
					if(typeof categoryToAdd == "undefined") {
						categoryToAdd = categoriesCheckboxes[i].getAttribute("category");
					} else {
						alert("Фид можно добавить только в одну категорию!");
						return;
					}
				}
			}
			if(typeof categoryToAdd == "undefined") {
				alert("Выберите фид, куда нужно добавить директорию");
				return;
			}
			
			$rootScope.$broadcast('addFeed-clicked', {feedUrl: feedUrl, categoryToAdd: categoryToAdd});
		};
		
		$scope.removeFeed = function () {
			var i = 0,
			feedCheckboxes = document.getElementsByClassName("feedCheckbox"),
			categoryName,
			feedName,
			feedObject,
			feedsToRemove = [];
			
			for (;i < feedCheckboxes.length; i++) {
				if(feedCheckboxes[i].checked) {
					 feedName = feedCheckboxes[i].getAttribute("feed");
					 categoryName = feedCheckboxes[i].getAttribute("category");
					 feedObject = {
						 feed: feedName,
						 category: categoryName
					 };
					 feedsToRemove.push(feedObject);
				}
			}
			$rootScope.$broadcast('removeFeeds-clicked', {feedsToRemove: feedsToRemove});
		};
		
		$scope.addCategory = function () {
			var categoryName = $("#categoryName").val();
			$rootScope.$broadcast("addCategory-clicked", {categoryName: categoryName});
		};
		
		$scope.removeCategory = function () {
			var i = 0,
			categoriesCheckboxes = document.getElementsByClassName("categoryCheckbox"),
			categoryName,
			categoriesToRemove = [];
			
			for(; i < categoriesCheckboxes.length; i++) {
				categoryName = categoriesCheckboxes[i].getAttribute("category");
				categoriesToRemove.push(categoryName);
			}
			$rootScope.$broadcast("removeCategories-clicked", {categoriesToRemove: categoriesToRemove});
		};
	}]);
	
	app.controller('FeedController', ['$scope', '$rootScope', function ($scope, $rootScope) {
        $scope.FeedController = this;
		this.categories = categories;
        this.feeds = feeds;
		
		$scope.redrawFeeds = function ($event, categoryName) {
			console.log($event.target.nodeName);
			console.log($event.target.className);
			if($event.target.className === "categoryCheckbox" || $event.target.className === "feedCheckbox") return;
			console.log("Trying to redraw feeds. Category " + categoryName);
			$rootScope.$broadcast('category-clicked', {categoryName: categoryName});
		};
        
        $scope.redrawItems = function ($event, feedName) {
			if($event.target.className === "categoryCheckbox" || $event.target.className === "feedCheckbox") return;
            console.log("Trying to redraw items");
            $rootScope.$broadcast('feed-clicked', { feedName: feedName });
        };
        
        $scope.$apply();
        
		$scope.$on('categories-received', function (event, args) {
			$scope.FeedController.categories = args.categories;
			$scope.$apply();			
			console.log("Categories length: " + $scope.FeedController.categories.length);
		});
		
        $scope.$on('feeds-received', function (event, args) {
            $scope.FeedController.feeds = args.feeds;
			$scope.$apply();
            console.log("Feeds length: " + $scope.FeedController.feeds.length);
            console.log("Feeds received listener");
        });
    }]);
    
    app.controller('ItemsController', ['$scope', '$sce', function ($scope, $sce) {
        $scope.itemsController = this;
        this.items = items;
    
        $scope.trustAsHtml = $sce.trustAsHtml;
        $scope.$on('items-received', function (event, args) {
            $scope.itemsController.items = args.items;
            console.log("Items length: " + $scope.itemsController.items.length);
            $scope.$apply();
            console.log("Items received listener");
        });
    }]);
    
    app.controller("WebSocketController", ['$scope', '$rootScope', function ($scope, $rootScope) {
        var onMessage = function (event) {
            var messageJSON = JSON.parse(event.data);
			console.log(event.data);
            console.log("Received message " + messageJSON.action);
            switch (messageJSON.action) {
			case "auth_success":
				$rootScope.$broadcast("auth_success");
				break;
			case "categories":
				$rootScope.$broadcast("categories-received", {categories: messageJSON.set});
				break;
            case "feed_items":
                $rootScope.$broadcast('items-received', { items: messageJSON.set });
                break;
            case "feeds":
                $rootScope.$broadcast('feeds-received', { feeds: messageJSON.set });
                break;
			case "logged_out":
				$rootScope.$broadcast('logged-out');
				break;
            default:
                break;
            }
        
        };
    
        webs = new WebSocket("ws://" + BACKEND_IP + ":8080/feedsreader/feeds");
        webs.onmessage = onMessage;
    
        webs.onopen = function (event) {
            console.log("WS connection opened");
        };
    
        webs.onerror = function (event) {
            console.log("Error while connecting to feedsreader");
        };
		
		webs.onclose = function (event) {
			console.log("WS connection closed");
		};
        
		//TODO make separate function for sending message
		$scope.$on('login-clicked', function (event, args) {
			/*var message = {
				action: "login",
				login: "testuser01@mail.ru"
			};*/
			var message = args.message;
			webs.send(JSON.stringify(message));
		});
		
		$scope.$on('register-clicked', function (event, args) {
			var message = args.message;
			webs.send(JSON.stringify(message));
		});
		
		$scope.$on('category-clicked', function (event, args) {
			console.log("WS cat clicked");
			var message = {
				action: "feeds",
				category: args.categoryName
			};
			webs.send(JSON.stringify(message));
		});
		
        $scope.$on('feed-clicked', function (event, args) {
            //request items for feed from server
            console.log("feed " + args.feedName + " clicked");
            var message = {
                action : "items",
                feed : args.feedName
            };
            webs.send(JSON.stringify(message));
        });
		
		$scope.$on("addFeed-clicked", function (event, args) {
			//adds feed for user in server
			var feedUrl = args.feedUrl,
				categoryToAdd = args.categoryToAdd;
			console.log("Adding feed " + feedUrl);
			var message = {
				action: "add_feed",
				feedurl: feedUrl,
				categorytoadd: categoryToAdd
			};
			webs.send(JSON.stringify(message));
		});
		
		$scope.$on("removeFeeds-clicked", function (event, args) {
			console.log("Removing feeds");
			var message = {
				action: "remove_feeds",
				feedstoremove: args.feedsToRemove
			};
			webs.send(JSON.stringify(message));
		});
		
		$scope.$on("addCategory-clicked", function (event, args) {
			var categoryName = args.categoryName;
			console.log("Adding category " + categoryName);
			var message = {
				action: "add_category",
				categoryName: categoryName
			};
			webs.send(JSON.stringify(message));
		});
		
		$scope.$on("removeCategories-clicked", function (event, args) {
			console.log("Removing categories");
			var message = {
				action: "remove_categories",
				categoriestoremove: args.categoriesToRemove
			};
			webs.send(JSON.stringify(message));
		});
		
		$scope.$on("sign-out", function (event, args) {
			console.log("Logging out");
			var message = {
				action: "sign-out"
			};
			webs.send(JSON.stringify(message));
		});
    }]);    
})();