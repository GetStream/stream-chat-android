package com.getstream.sdk.chat.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

public class ConnectionChecker {
    private static final String TAG = ConnectionChecker.class.getSimpleName();
    private static ConnectivityManager connectivityManager;
    private static Handler connectionCheckHandler = new Handler();
    private static Context context;

    public static void startConnectionCheck(Context context) {
        ConnectionChecker.startConnectionCheckRepeatingTask(context);
    }

    private static Runnable runnableConnectionCheck = new Runnable() {
        @Override
        public void run() {
            try {
                isConnection(context.getApplicationContext());
            } finally {
                connectionCheckHandler.postDelayed(runnableConnectionCheck, 1000 * 3);
            }
        }
    };

    private static void startConnectionCheckRepeatingTask(Context context) {
        stopConnectionCheckRepeatingTask();
        ConnectionChecker.context = context.getApplicationContext();
        connectivityManager = (ConnectivityManager) ConnectionChecker.context.getSystemService(ConnectionChecker.context.CONNECTIVITY_SERVICE);
        runnableConnectionCheck.run();
    }

    public static void stopConnectionCheckRepeatingTask() {
        connectionCheckHandler.removeCallbacks(runnableConnectionCheck);
    }

    private static void isConnection(Context context) {
        if (connectivityManager == null)
            connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean noConnection = !(activeNetwork != null && activeNetwork.isConnectedOrConnecting());

        if (noConnection != Global.noConnection) {
            Intent broadcast = new Intent();
            broadcast.addCategory(Intent.CATEGORY_DEFAULT);
            Log.d(TAG,"No connection: " + noConnection + ": Global No connection: " + Global.noConnection);
            if (noConnection) {
                broadcast.setAction(Constant.BC_CONNECTION_OFF);
            } else {
                broadcast.setAction(Constant.BC_CONNECTION_ON);
            }
            context.sendBroadcast(broadcast);
        }
    }
}
