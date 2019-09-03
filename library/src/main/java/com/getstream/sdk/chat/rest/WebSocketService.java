package com.getstream.sdk.chat.rest;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.google.gson.Gson;

import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketService extends WebSocketListener {
    private final String TAG = WebSocketService.class.getSimpleName();

    private static final int NORMAL_CLOSURE_STATUS = 1000;

    private WSResponseHandler webSocketListener;
    private String wsURL;
    private OkHttpClient httpClient;

    private WebSocket webSocket;
    protected EchoWebSocketListener listener;

    /** The connection is considered resolved after the WS connection returned a good message */
    private boolean connectionResolved;

    /** We only make 1 attempt to reconnect at the same time.. */
    private boolean isConnecting;

    /** Boolean that indicates if we have a working connection to the server */
    private boolean isHealthy;

    /** Store the last event time for health checks */
    private Date lastEvent;

    /** Send a health check message every 30 seconds */
    private int healthCheckInterval = 30 * 1000;

    /** consecutive failures influence the duration of the timeout */
    private int consecutiveFailures;

    private boolean shuttingDown;

    private int wsId;

    private boolean isConnecting() {
        return isConnecting;
    }

    private void setConnecting(boolean connecting) {
        isConnecting = connecting;
    }

    private boolean isHealthy() {
        return isHealthy;
    }

    private void setHealthy(boolean healthy) {
        isHealthy = healthy;
    }

    private Date getLastEvent() {
        return lastEvent;
    }

    private void setLastEvent(Date lastEvent) {
        this.lastEvent = lastEvent;
    }

    private int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    private void resetConsecutiveFailures() {
        this.consecutiveFailures = 0;
    }

    private EventHandlerThread eventThread;

    public WebSocketService(String wsURL, String userID, WSResponseHandler webSocketListener) {
        this.wsURL = wsURL;
        this.webSocketListener = webSocketListener;
    }

    public void connect() {
        Log.i(TAG, "connect...");

        if (isConnecting()) {
            Log.w(TAG, "already connecting");
            return;
        }

        wsId = 1;
        setConnecting(true);
        resetConsecutiveFailures();
        setupWS();

        shuttingDown = false;
        eventThread = new EventHandlerThread();
        eventThread.start();
        eventThread.setName("WSS - event handler thread");
    }

    public void disconnect() {
        shuttingDown = true;
        eventThread.mHandler.removeCallbacksAndMessages(null);
        webSocket.close(NORMAL_CLOSURE_STATUS, "");
        webSocket = null;
        listener = null;
        httpClient = null;
    }

    public void reconnect() {
        reconnect(false);
    }

    private void setupWS(){
        Log.i(TAG, "setupWS");

        httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(wsURL).build();
        listener = new EchoWebSocketListener();
        webSocket = httpClient.newWebSocket(request, listener);
        httpClient.dispatcher().executorService().shutdown();
    }

    private void setHealth(boolean healthy) {
        Log.i(TAG, "setHealth " + healthy);
        if (healthy && !isHealthy()) {
            setHealthy(true);
            Event wentOnline = new Event(true);
            sendEventToHandlerThread(wentOnline);
        }
        if (!healthy && isHealthy()) {
            setHealthy(false);
            Log.i(TAG, "spawn mOfflineNotifier");
            eventThread.mHandler.postDelayed(mOfflineNotifier, 5000);
        }
    }

    private int getRetryInterval() {
		int max = Math.min(500 + getConsecutiveFailures() * 2000, 25000);
		int min = Math.min(Math.max(250, (getConsecutiveFailures() - 1) * 2000), 25000);
        return (int) Math.floor(Math.random() * (max - min) + min);
    }

    private void reconnect(boolean delay){
        Log.i(TAG, "reconnecting...");
        if (isConnecting() || isHealthy()) {
            Log.i(TAG, "nevermind, we are already connecting...");
            return;
        }
        Log.i(TAG, "schedule reconnection in " + getRetryInterval() + "ms");
        eventThread.mHandler.postDelayed(mReconnect, delay ? getRetryInterval() : 0);
    }

    private void sendEventToHandlerThread(Event event){
        Message eventMsg = new Message();
        eventMsg.obj = event;
        eventThread.mHandler.sendMessage(eventMsg);
    }

//    private Handler mHandler = new Handler();

    private Runnable mOfflineNotifier = () -> {
        if (!isHealthy()) {
            Event wentOffline = new Event(false);
            sendEventToHandlerThread(wentOffline);
        }
    };

    private Runnable mMonitor = new Runnable() {
        @Override
        public void run() {
            long millisNow = new Date().getTime();
            int monitorInterval = 1000;
            if (getLastEvent() != null) {
                if (millisNow - getLastEvent().getTime() > (healthCheckInterval + 10 * 1000)) {
                    consecutiveFailures += 1;
                    setHealth(false);
                    reconnect(true);
                }
            }
            eventThread.mHandler.postDelayed(mHealthCheck, monitorInterval);
        }
    };

    private Runnable mHealthCheck = new Runnable() {
        @Override
        public void run() {
            try {
                Event event = new Event();
                event.setType(EventType.HEALTH_CHECK);
                webSocket.send(new Gson().toJson(event));
            } finally {
                eventThread.mHandler.postDelayed(mHealthCheck, healthCheckInterval);
            }
        }
    };

    private Runnable mReconnect = () -> {
        if (isConnecting() || isHealthy()) {
            return;
        }

        destroyCurrentWSConnection();
        setupWS();
    };

    private void startMonitor() {
        mHealthCheck.run();
        mMonitor.run();
    }

    private boolean isConnectionResolved() {
        return connectionResolved;
    }

    private void setConnectionResolved() {
        this.connectionResolved = true;
        startMonitor();
    }

    private class EchoWebSocketListener extends WebSocketListener {

        @Override
        public synchronized void onOpen(WebSocket webSocket, Response response) {
            if (shuttingDown) return;
            setHealth(true);
            setConnecting(false);
            resetConsecutiveFailures();
            if (wsId > 1) {
                eventThread.mHandler.post(() -> webSocketListener.connectionRecovered());
            }
            Log.d(TAG, "WebSocket #" + wsId + " Connected : " + response);
        }

        @Override
        public synchronized void onMessage(WebSocket webSocket, String text) {
            if (shuttingDown) return;
            Log.d(TAG, "WebSocket Response : " + text);
            Event event = GsonConverter.Gson().fromJson(text, Event.class);
            setLastEvent(new Date());

            if (isConnectionResolved()) {
                sendEventToHandlerThread(event);
            } else {
                eventThread.mHandler.post(() -> {
                    webSocketListener.connectionResolved(event);
                    setConnectionResolved();
                });
            }
        }

        @Override
        public synchronized void onClosing(WebSocket webSocket, int code, String reason) {
            if (shuttingDown) return;
            Log.d(TAG, "Closing : " + code + " / " + reason);
            // this usually happens only when the connection fails for auth reasons
            if (code == NORMAL_CLOSURE_STATUS) {
                // TODO: propagate this upstream
                webSocket.close(code, reason);
            } else {
                consecutiveFailures++;
                setConnecting(false);
                setHealth(false);
                reconnect(true);
                webSocket.close(code, reason);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            if (shuttingDown) return;
            try {
                Log.i(TAG, "Error: " + t.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            consecutiveFailures++;
            setConnecting(false);
            setHealth(false);
            reconnect(true);
        }
    }

    private void destroyCurrentWSConnection() {
        wsId++;
        try {
            httpClient.dispatcher().cancelAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class EventHandlerThread extends Thread {
        Handler mHandler;

        @SuppressLint("HandlerLeak")
        public void run() {
            Looper.prepare();

            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    webSocketListener.onWSEvent((Event) msg.obj);
                }
            };

            Looper.loop();
        }
    }

}
