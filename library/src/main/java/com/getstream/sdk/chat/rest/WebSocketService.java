package com.getstream.sdk.chat.rest;

import android.os.Handler;
import android.util.Log;

import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketService extends WebSocketListener {

    private final String TAG = WebSocketService.class.getSimpleName();

    public interface WSResponseHandler {
        void handleWSResponse(Object response);
    }

    private WSResponseHandler mResponseHandler;
    public String wsURL;
    private OkHttpClient client;
    private Request request;
    private EchoWebSocketListener listener;
    private WebSocket webSocket;

    public void setWSResponseHandler(WSResponseHandler responseHandler) {
        if (mResponseHandler != null) {
            mResponseHandler = null;
        }
        mResponseHandler = responseHandler;
    }

    public void connect() {
        Global.streamChat.setClientID(null);
        client = new OkHttpClient();
        request = new Request.Builder().url(this.wsURL).build();
        listener = new EchoWebSocketListener();
        webSocket = client.newWebSocket(request, listener);
        Log.d(TAG, "WebSocket Connecting...");
        client.dispatcher().executorService().shutdown();
    }

    // region Health Check
    private Handler mHandler = new Handler();
    private Runnable mHealthChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (Global.noConnection) {
                    connect();
                } else {
                    webSocket.send("");
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                int interval = Global.noConnection ? Constant.HEALTH_CHECK_INTERVAL / 3 : Constant.HEALTH_CHECK_INTERVAL;
                mHandler.postDelayed(mHealthChecker, interval);
            }
        }
    };

    void startRepeatingTask() {
        mHealthChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mHealthChecker);
    }

    private class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d(TAG, "WebSocket Connected : " + response);
            Global.noConnection = false;
            stopRepeatingTask();
            startRepeatingTask();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d(TAG, "WebSocket Response : " + text);
            mResponseHandler.handleWSResponse(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            mResponseHandler.handleWSResponse(bytes);
            Log.d(TAG, "Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            Log.d(TAG, "Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.d(TAG, "Error: " + t.getMessage());
            if (t.getMessage().contains("Connection reset by peer")){
                Global.channels = new ArrayList<>();
                Global.streamChat.setClientID(null);
                Global.noConnection = true;
                try {
                    mResponseHandler.handleWSResponse(Constant.CONNECTION_ERROR);
                    client.dispatcher().cancelAll();// to cancel all requests
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
