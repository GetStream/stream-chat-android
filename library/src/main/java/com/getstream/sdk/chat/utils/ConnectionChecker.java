package com.getstream.sdk.chat.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

public class ConnectionChecker {
    private static final String TAG = ConnectionChecker.class.getSimpleName();
    private static ConnectivityManager connectivityManager;
    private static Handler connectionCheckHandler = new Handler();
    private static Runnable runnableConnectionCheck = new Runnable() {
        @Override
        public void run() {
            try {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                Global.noConnection = !(activeNetwork != null && activeNetwork.isConnectedOrConnecting());
                Log.d(TAG, "Connection: " + !Global.noConnection);
                if (Global.noConnection) {
                    Global.channels = new ArrayList<>();
                    if (Global.streamChat != null) Global.streamChat.setClientID(null);
                }
            } finally {
                connectionCheckHandler.postDelayed(runnableConnectionCheck, 1000 * 3);
            }
        }
    };

    public static void startConnectionCheckRepeatingTask(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        runnableConnectionCheck.run();
    }

    public static void stopConnectionCheckRepeatingTask() {
        connectionCheckHandler.removeCallbacks(runnableConnectionCheck);
    }
}
