package com.getstream.sdk.chat.rest;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketService extends WebSocketListener {

    private final String TAG = WebSocketService.class.getSimpleName();

    private List<WSResponseHandler> webSocketListeners;
    private String wsURL;
    private OkHttpClient client;
    private Request request;
    private EchoWebSocketListener listener;
    private WebSocket webSocket;

    public void setWSResponseHandler(WSResponseHandler responseHandler) {
        if (webSocketListeners == null) webSocketListeners = new ArrayList<>();
        if (!webSocketListeners.contains(responseHandler)) {
            webSocketListeners.add(responseHandler);
        }
    }

    public void removeWSResponseHandler(WSResponseHandler responseHandler) {
        if (webSocketListeners == null || webSocketListeners.isEmpty()) return;
        if (webSocketListeners.contains(responseHandler)) {
            webSocketListeners.remove(responseHandler);
        }
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

    public void setWsURL(String wsURL) {
        this.wsURL = wsURL;
    }

    // region Health Check
    private Handler mHandler = new Handler();
    private Runnable mHealthChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (Global.noConnection) {
                    clearWSClient();
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

    private void startRepeatingTask() {
        mHealthChecker.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mHealthChecker);
    }

    private class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d(TAG, "WebSocket Connected : " + response);
            Global.noConnection = false;
//            stopRepeatingTask();
            startRepeatingTask();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d(TAG, "WebSocket Response : " + text);
            JSONObject json = null;
            try {
                json = new JSONObject(text);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (json == null) return;

            Event event = Parser.parseEvent(json);
            if (event == null) return;

            if (TextUtils.isEmpty(Global.streamChat.getClientID())) {
                Global.noConnection = false;
                if (!TextUtils.isEmpty(event.getConnection_id())) {
                    String connectionId = event.getConnection_id();

                    if (event.getMe() != null)
                        Global.streamChat.setUser(event.getMe());

                    Global.streamChat.setClientID(connectionId);

                    for (WSResponseHandler webSocketListener : webSocketListeners)
                        webSocketListener.handleConnection();
                }
            } else {
                for (WSResponseHandler webSocketListener : webSocketListeners)
                    webSocketListener.handleEventWSResponse(event);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            for (WSResponseHandler webSocketListener : webSocketListeners)
                webSocketListener.handleByteStringWSResponse(bytes);
            Log.d(TAG, "Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            Log.d(TAG, "Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            try {
                Log.d(TAG, "Error: " + t.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }

            clearWSClient();
            if (webSocketListeners == null) return;
            for (WSResponseHandler webSocketListener : webSocketListeners)
                webSocketListener.onFailed(t.getMessage(), t.hashCode());

        }
    }

    public void clearWSClient() {
        try {
            client.dispatcher().cancelAll();
        } catch (Exception e) {
        }
        Global.noConnection = true;
        Global.streamChat.setClientID(null);
    }
}
