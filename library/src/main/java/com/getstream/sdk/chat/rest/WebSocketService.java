package com.getstream.sdk.chat.rest;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.channel.Event;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketService extends WebSocketListener {

    private final String TAG = WebSocketService.class.getSimpleName();

    private WSResponseHandler webSocketListener;
    public String wsURL;
    private OkHttpClient client;
    private Request request;
    private EchoWebSocketListener listener;
    private WebSocket webSocket;

    public void setWSResponseHandler(WSResponseHandler responseHandler) {
        if (webSocketListener != null) {
            webSocketListener = null;
        }
        webSocketListener = responseHandler;
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
            stopRepeatingTask();
            startRepeatingTask();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d(TAG, "WebSocket Response : " + text);
            if (webSocketListener != null)
                webSocketListener.handleWSResponse(text);

            if (TextUtils.isEmpty(Global.streamChat.getClientID())) {
                JSONObject json = null;
                try {
                    json = new JSONObject(text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (json == null) return;

                Event event = Parser.parseEvent(json);
                if (!TextUtils.isEmpty(event.getConnection_id())) {
                    String connectionId = event.getConnection_id();
                    Global.streamChat.setClientID(connectionId);
                    Log.d(TAG, "Connection ID: " + connectionId);
                }
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            if (webSocketListener != null)
                webSocketListener.handleWSResponse(bytes);
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

            try {
                if (t.getMessage().contains("Connection reset by peer")) {
                    if (webSocketListener != null)
                        webSocketListener.onFailed(t.getMessage(), t.hashCode());

                    client.dispatcher().cancelAll();// to cancel all requests
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
