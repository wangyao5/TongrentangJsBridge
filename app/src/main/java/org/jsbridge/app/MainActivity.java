package org.jsbridge.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.Build;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;

import org.jsbridge.app.R;

public class MainActivity extends Activity {

    private final String TAG = "MainActivity";
//    private static final String START_URL = "file:///android_asset/demo.html";
    private static final String START_URL = "http://10.0.0.2:8080/demo.html";
    BridgeWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideVirtualButtons();
        setContentView(R.layout.activity_main);

        webView = (BridgeWebView) findViewById(R.id.webView);

        webView.setDefaultHandler(new DefaultHandler());

        webView.loadUrl(START_URL);

        webView.registerHandler("exit", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "aahandler = submitFromWeb, data from web = " + data);
                function.onCallBack("submitFromWeb exe, response data 中文 from Java");
                MainActivity.this.finish();
            }

        });

        webView.registerHandler("ttsPay", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {

            }
        });

        webView.registerHandler("ttsStop", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {

            }
        });

        /** 原生调用js方法
         webView.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
        @Override public void onCallBack(String data) {

        }
        });
         */

    }

    private void hideVirtualButtons() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

}
