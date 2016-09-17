;
(function(win) {

    var ready = function(bridge) {
        WebViewJavascriptBridge.init(function(message, responseCallback) {
                            console.log('JS got a message', message);
                            var data = {
                                'Javascript Responds': '测试中文!'
                            };
                            console.log('JS responding with', data);
                            responseCallback(data);
            responseCallback(data);
        });
        win.device = device;
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


    // WebViewJavascriptBridge.callHandler(suff, p, callBack);
    var callHandler = function(suff, p, callBack) {
    	WebViewJavascriptBridge.callHandler(suff, p, function(msg){
            console.log('WebViewJavascriptBridge');
            console.log(msg);
            callBack(msg);
        });
    };

    /** our js function */
    var device = {
        exit : function() {
            callHandler('exit');
        },
        ttsPay : function(text, payFun) {
            callHandler('ttsPay', [text], function(callBackMsg) {
                console.log('WebViewJavascriptBridge');
                payFun(callBackMsg);
            });
        },
        ttsStop : function() {
            callHandler('ttsStop', [], function(callBackMsg) {
                console.log('WebViewJavascriptBridge');
                payFun(callBackMsg);
            });
        }
    };


})(this);