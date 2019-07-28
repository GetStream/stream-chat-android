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

    public static void connectionCheck(Context context) {
        ConnectionChecker.startConnectionCheckRepeatingTask(context.getApplicationContext());
    }

    private static Runnable runnableConnectionCheck = new Runnable() {
        @Override
        public void run() {
            try {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                boolean noConnection = !(activeNetwork != null && activeNetwork.isConnectedOrConnecting());
                if (noConnection && noConnection != Global.noConnection) {
                    Global.noConnection = noConnection;
                    Intent broadcast = new Intent();
                    broadcast.addCategory(Intent.CATEGORY_DEFAULT);

                    if (Global.noConnection) {
                        broadcast.setAction(Constant.BC_CONNECTION_OFF);
                    } else {
                        broadcast.setAction(Constant.BC_CONNECTION_ON);
                    }

                    context.sendBroadcast(broadcast);
                    Log.d(TAG, "Connection: " + !Global.noConnection);
                }


//                if (Global.noConnection) {
//                    Global.channels = new ArrayList<>();
//                    if (Global.streamChat != null) Global.streamChat.setClientID(null);
//                }
            } finally {
                connectionCheckHandler.postDelayed(runnableConnectionCheck, 1000 * 3);
            }
        }
    };

    private static void startConnectionCheckRepeatingTask(Context context) {
        ConnectionChecker.context = context;
        connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        runnableConnectionCheck.run();
    }

    public static void stopConnectionCheckRepeatingTask() {
        connectionCheckHandler.removeCallbacks(runnableConnectionCheck);
    }
}
