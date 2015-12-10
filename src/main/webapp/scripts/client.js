/*jslint plusplus: true */
/*global $, console, WebSocket, alert, FormData, BigInteger, isPrime*/
var BACKEND_IP = "10.160.2.31",
    webs,
    item,
    app;
$(document).ready(function () {

    "use strict";
    var onMessage = function (event) {
            var messageJSON = JSON.parse(event.data);
            console.log("Received message " + messageJSON.type);
            switch (messageJSON.type) {
            case "ITEMS":
                //item = messageJSON.data;
                $rootScope.$broadcast('items-received', { item: messageJSON.data });
                console.log(item.title);
                break;
            default:
                break;
            }
        };
    
    webs = new WebSocket("ws://" + BACKEND_IP + ":9988");
    webs.onmessage = onMessage;
    
    webs.onopen = function (event) {
        console.log("Connected to feedreader backend");
    };
    
    webs.onerror = function (event) {
        console.log("Error while connecting to feedreader");
    }
});