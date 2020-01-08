package com.getstream.sdk.chat.rest;

import android.os.Message;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.response.ErrorResponse;
import com.getstream.sdk.chat.rest.response.WsErrorMessage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class StreamWebSocketService extends WebSocketListener implements WebSocketService {
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private final String TAG = StreamWebSocketService.class.getSimpleName();

    protected EchoWebSocketListener listener;
    private WSResponseHandler webSocketListener;
    private String wsURL;
    private OkHttpClient httpClient;
    private WebSocket webSocket;
    /**
     * The connection is considered resolved after the WS connection returned a good message
     */
    private boolean connectionResolved;

    /**
     * We only make 1 attempt to reconnectWebSocket at the same time..
     */
    private boolean isConnecting;

    /**
     * Boolean that indicates if we have a working connection to the server
     */
    private boolean isHealthy;

    /**
     * Store the last event time for health checks
     */
    private Date lastEvent;

    /**
     * Send a health check message every 30 seconds
     */
    private int healthCheckInterval = 30 * 1000;

    /**
     * consecutive failures influence the duration of the timeout
     */
    private int consecutiveFailures;

    private boolean isShuttingDown() {
        return shuttingDown;
    }

    private boolean shuttingDown;

    private int wsId;
    private EventHandlerThread eventThread;
    private Runnable mOfflineNotifier = () -> {
        if (!isHealthy()) {
            Event wentOffline = new Event(false);
            sendEventToHandlerThread(wentOffline);
        }
    };

    private Runnable mHealthCheck = new Runnable() {
        @Override
        public void run() {
            if (isShuttingDown()) {
                StreamChat.getLogger().logI(this, "connection is shutting down, quit health check");
                return;
            }
            StreamChat.getLogger().logI(this, "send health check");
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
        if (isConnecting() || isHealthy() || isShuttingDown()) {
            return;
        }
        destroyCurrentWSConnection();
        setupWS();
    };

    private Runnable mMonitor = new Runnable() {
        @Override
        public void run() {
            if (isShuttingDown()) {
                StreamChat.getLogger().logI(this, "connection is shutting down, quit monitor");
                return;
            }
            StreamChat.getLogger().logI(this, "check connection health");
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

    public StreamWebSocketService(String wsURL, WSResponseHandler webSocketListener) {
        this.wsURL = wsURL;
        this.webSocketListener = webSocketListener;
    }

    @Override
    public void connect() {
        StreamChat.getLogger().logI(this, "connect...");

        if (isConnecting()) {
            StreamChat.getLogger().logW(this, "already connecting");
            return;
        }

        wsId = 0;
        setConnecting(true);
        resetConsecutiveFailures();

        // start the thread before setting up the websocket connection
        eventThread = new EventHandlerThread(this);
        eventThread.setName("WSS - event handler thread");
        eventThread.start();

        // WS connection
        setupWS();

        shuttingDown = false;
    }

    @Override
    public void disconnect() {
        StreamChat.getLogger().logI(this, "disconnect was called");
        webSocket.close(1000, "bye");
        shuttingDown = true;
        eventThread.mHandler.removeCallbacksAndMessages(null);
        destroyCurrentWSConnection();
    }

    @Override
    public WSResponseHandler getWebSocketListener() {
        return webSocketListener;
    }

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

    private void setupWS() {
        StreamChat.getLogger().logI(this, "setupWS");
        wsId++;
        httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(wsURL).build();
        listener = new EchoWebSocketListener();
        webSocket = httpClient.newWebSocket(request, listener);
        httpClient.dispatcher().executorService().shutdown();
    }

    private void setHealth(boolean healthy) {
        StreamChat.getLogger().logI(this, "setHealth " + healthy);
        if (healthy && !isHealthy()) {
            setHealthy(true);
            Event wentOnline = new Event(true);
            sendEventToHandlerThread(wentOnline);
        }
        if (!healthy && isHealthy()) {
            setHealthy(false);
            StreamChat.getLogger().logI(this, "spawn mOfflineNotifier");
            eventThread.mHandler.postDelayed(mOfflineNotifier, 5000);
        }
    }

    private int getRetryInterval() {
        int max = Math.min(500 + getConsecutiveFailures() * 2000, 25000);
        int min = Math.min(Math.max(250, (getConsecutiveFailures() - 1) * 2000), 25000);
        return (int) Math.floor(Math.random() * (max - min) + min);
    }

    private void reconnect(boolean delay) {
        if (isConnecting() || isHealthy() || shuttingDown) {
            return;
        }
        StreamChat.getLogger().logI(this, "schedule reconnection in " + getRetryInterval() + "ms");
        eventThread.mHandler.postDelayed(mReconnect, delay ? getRetryInterval() : 0);
    }

    private void sendEventToHandlerThread(Event event) {
        Message eventMsg = new Message();
        eventMsg.obj = event;
        eventThread.mHandler.sendMessage(eventMsg);
    }

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

    private void destroyCurrentWSConnection() {
        try {
            httpClient.dispatcher().cancelAll();
        } catch (Exception e) {
            StreamChat.getLogger().logT(this, e);
        }
    }

    private class EchoWebSocketListener extends WebSocketListener {

        @Override
        public synchronized void onOpen(WebSocket webSocket, Response response) {
            if (isShuttingDown()) return;
            setHealth(true);
            setConnecting(false);
            resetConsecutiveFailures();
            if (wsId > 1) {
                eventThread.mHandler.post(() -> webSocketListener.connectionRecovered());
            }
            StreamChat.getLogger().logD(this, "WebSocket #" + wsId + " Connected : " + response);
        }

        @Override
        public synchronized void onMessage(WebSocket webSocket, String text) {
            // TODO: synchronized onMessage is not great for performance when receiving many messages at once. Minor concern since its pretty fast at handling a message
            StreamChat.getLogger().logD(this, "WebSocket # " + wsId + " Response : " + text);

            if (isShuttingDown()) return;

            WsErrorMessage errorMessage;

            try {
                errorMessage = GsonConverter.Gson().fromJson(text, WsErrorMessage.class);
            } catch (JsonSyntaxException ignored) {
                errorMessage = null;
            }

            Boolean isError = errorMessage != null && errorMessage.getError() != null;

            if (isError) {
                // token expiration is handled separately (allowing you to refresh the token from your backend)
                if (errorMessage.getError().getCode() == ErrorResponse.TOKEN_EXPIRED_CODE) {
                    // the server closes the connection after sending an error, so we don't need to close it here
                    // webSocket.close(NORMAL_CLOSURE_STATUS, "token expired");
                    eventThread.mHandler.post(() -> webSocketListener.tokenExpired());
                    return;
                } else {
                    // other errors are passed to the callback
                    // the server closes the connection after sending an error, so we don't need to close it here
                    // webSocket.close(NORMAL_CLOSURE_STATUS, String.format("error with code %d", errorMessage.getError().getCode()));
                    WsErrorMessage finalErrorMessage = errorMessage;
                    eventThread.mHandler.post(() -> webSocketListener.onError(finalErrorMessage));
                    return;
                }

            }

            Event event;

            try {
                event = GsonConverter.Gson().fromJson(text, Event.class);
                // set received at, prevents clock issues from breaking our ability to remove old typing indicators
                Date now = new Date();
                event.setReceivedAt(now);
                setLastEvent(now);
            } catch (JsonSyntaxException e) {
                StreamChat.getLogger().logT(this, e);
                return;
            }

            StreamChat.getLogger().logD(this, String.format("Received event of type %s", event.getType().toString()));

            // resolve on the first good message
            if (!isConnectionResolved()) {
                eventThread.mHandler.post(() -> {
                    webSocketListener.connectionResolved(event);
                    setConnectionResolved();
                });
            }

            sendEventToHandlerThread(event);
        }

        @Override
        public synchronized void onClosing(WebSocket webSocket, int code, String reason) {
            if (isShuttingDown()) return;
            StreamChat.getLogger().logD(this, "WebSocket # " + wsId + " Closing : " + code + " / " + reason);
            // this usually happens only when the connection fails for auth reasons
            if (code == NORMAL_CLOSURE_STATUS) {
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
            if (isShuttingDown()) return;
            try {
                StreamChat.getLogger().logI(this, "WebSocket # " + wsId + " Error: " + t.getMessage());
            } catch (Exception e) {
                StreamChat.getLogger().logT(this, e);
            }
            consecutiveFailures++;
            setConnecting(false);
            setHealth(false);
            reconnect(true);
        }
    }

}
