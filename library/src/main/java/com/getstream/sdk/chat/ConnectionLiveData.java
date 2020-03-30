package com.getstream.sdk.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.lifecycle.LiveData;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.logger.TaggedLogger;

public class ConnectionLiveData extends LiveData<ConnectionLiveData.ConnectionModel> {

    private TaggedLogger logger = ChatLogger.Companion.get("ConnectionLiveData");
    private Context context;

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager == null) {
                    logger.logE("Can\'t get access to ConnectivityManager");
                    return;
                }
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    switch (activeNetwork.getType()) {
                        case ConnectivityManager.TYPE_WIFI:
                            postValue(new ConnectionModel(ConnectivityManager.TYPE_WIFI, true));
                            break;
                        case ConnectivityManager.TYPE_MOBILE:
                            postValue(new ConnectionModel(ConnectivityManager.TYPE_MOBILE, true));
                            break;
                    }
                } else {
                    postValue(new ConnectionModel(0, false));
                }
            }
        }
    };

    public ConnectionLiveData(Context context) {
        this.context = context;
    }

    @Override
    protected void onActive() {
        super.onActive();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        context.unregisterReceiver(networkReceiver);
    }

    public class ConnectionModel {

        private int type;
        private boolean isConnected;

        public ConnectionModel(int type, boolean isConnected) {
            this.type = type;
            this.isConnected = isConnected;
        }

        public int getType() {
            return type;
        }

        public boolean getIsConnected() {
            return isConnected;
        }
    }

}
