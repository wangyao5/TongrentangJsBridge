package org.jsbridge.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.os.Build;
import android.webkit.WebView;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;

import org.json.JSONArray;
import org.json.JSONException;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.speech.util.ApkInstaller;
import com.iflytek.sunflower.FlowerCollector;

import org.jsbridge.app.R;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {
    // 语音合成对象
    private SpeechSynthesizer mTts;

    // 语记安装助手类
    ApkInstaller mInstaller;

    // 引擎类型
    private static final String mEngineType = SpeechConstant.TYPE_CLOUD;

    //监听事件间隔,当间隔过大调用js显示广告
    private final Timer timer = new Timer();
    private final long NONE_TOUCH_PERIOD = 2*1000*60;
    private long currentTime;
    //TimerTask param
    private final long period = 10;
    private final long delay = 1;



    private final String TAG = "MainActivity";
//    private static final String START_URL = "file:///android_asset/index.html";
    private static final String START_URL = "http://10.0.0.2:8080/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideVirtualButtons();
        setContentView(R.layout.activity_main);
        initTTs();
        final BridgeWebView webView = (BridgeWebView) findViewById(R.id.webView);
        startTimerTask(webView);

        webView.setDefaultHandler(new DefaultHandler());

        webView.loadUrl(START_URL);

        webView.registerHandler("exit", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                MainActivity.this.finish();
            }

        });

        webView.registerHandler("ttsPlay", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                String text = "";
                try {
                    JSONArray jsonArray = new JSONArray(data);
                    text = jsonArray.getString(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final CallBackFunction func = function;
                setParam();
                int code = mTts.startSpeaking(text, new SynthesizerListener(){
                    @Override
                    public void onSpeakBegin() {

                    }

                    @Override
                    public void onBufferProgress(int percent, int beginPos, int endPos, String info) {

                    }

                    @Override
                    public void onSpeakPaused() {

                    }

                    @Override
                    public void onSpeakResumed() {

                    }

                    @Override
                    public void onSpeakProgress(int percent, int beginPos, int endPos) {

                    }

                    @Override
                    public void onCompleted(SpeechError speechError) {
                        if (speechError == null) {
                            func.onCallBack("{status:1}");
                        } else if (speechError != null) {
                            android.util.Log.v(TAG, speechError.getPlainDescription(true));
                        }
                    }

                    @Override
                    public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

                    }
                });

//            /**
//             * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//             * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//            */
//            String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//            int code = mTts.synthesizeToUri(text, path, mTtsListener);

                if (code != ErrorCode.SUCCESS) {
                    if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
                        //未安装则跳转到提示安装页面
//                        mInstaller.install();
                    }else {
                        android.util.Log.v(TAG, "语音合成失败,错误码: " + code);
                    }
                }
            }
        });

        webView.registerHandler("ttsCancel", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                mTts.stopSpeaking();
            }
        });

        webView.registerHandler("ttsPause", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                mTts.pauseSpeaking();
            }
        });

        webView.registerHandler("ttsResume", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                mTts.resumeSpeaking();
            }
        });

        webView.registerHandler("hideAdvert", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                startTimerTask(webView);
            }
        });


        /** 原生调用js方法
         webView.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
        @Override public void onCallBack(String data) {

        }
        });
         */



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
    }

    @Override
    protected void onResume() {
        currentTime = System.currentTimeMillis();
        //移动数据统计分析
        FlowerCollector.onResume(this);
        FlowerCollector.onPageStart(TAG);
        super.onResume();
    }
    @Override
    protected void onPause() {
        //移动数据统计分析
        FlowerCollector.onPageEnd(TAG);
        FlowerCollector.onPause(this);
        super.onPause();
    }


    private void hideVirtualButtons() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    private void initTTs() {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
        mInstaller = new ApkInstaller(this);
    }

    /**
     * 参数设置
     * @return
     */
    private void setParam(){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            String[] mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
            String voicer = mCloudVoicersValue[0];
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, "50");
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, "50");
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, "100");
        }else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /**
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                android.util.Log.v(TAG, "初始化失败,错误码："+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    private void startTimerTask(final BridgeWebView webView){
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - currentTime >= NONE_TOUCH_PERIOD) {
                    webView.callHandler("showAdvert", "", new CallBackFunction() {
                        @Override
                        public void onCallBack(String data) {

                        }
                    });
                    timer.cancel();
                }
            }
        };
        timer.schedule(task,delay*1000, period*1000);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        currentTime = ev.getEventTime();
        return super.dispatchTouchEvent(ev);

    }
}
