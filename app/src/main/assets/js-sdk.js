;
(function(win) {

    // WebViewJavascriptBridge.callHandler(suff, p, callBack);
    var callHandler = function(suff, p, callBack) {
    	WebViewJavascriptBridge.callHandler(suff, p, function(msg){
            callBack(msg);
        });
    };

    /** our js function */
    var device = {
        exit : function() {
            callHandler('exit');
        },
        ttsPlay : function(text, payFun) {
            callHandler('ttsPlay', [text], function(callBackMsg) {
                payFun(callBackMsg);
            });
        },
        ttsCancel : function() {
            callHandler('ttsCancel');
        },
        ttsPause : function() {
            callHandler('ttsPause');
        },
        ttsResume : function() {
            callHandler('ttsResume');
        },
        hideAdvert : function() {
            callHandler('hideAdvert');
        }

    };

    var ready = function(bridge) {
        WebViewJavascriptBridge.init(function(message, responseCallback) {
            var data = {
                'Javascript Responds': '测试中文!'
            };
            responseCallback(data);
        });
        win.device = device;
        win.showAdvert = function() {
            var showAdvertEvent = document.createEvent('Events');
            showAdvertEvent.initEvent('showAdvert');
            document.dispatchEvent(showAdvertEvent);
        };
        var readyEvent = document.createEvent('Events');
        readyEvent.initEvent('onDeviceReady');
        document.dispatchEvent(readyEvent);
        /** 注册js监听
        bridge.registerHandler("functionInJs", function(data, responseCallback) {
            document.getElementById("show").innerHTML = ("data from Java: = " + data);
            var responseData = "Javascript Says Right back aka!";
            responseCallback(responseData);
        });
        */
    };

    if (window.WebViewJavascriptBridge) {
        ready(WebViewJavascriptBridge);
    } else {
        document.addEventListener(
            'WebViewJavascriptBridgeReady',
            function() {
                ready(WebViewJavascriptBridge);
            },
            false
        );
    }

})(this);