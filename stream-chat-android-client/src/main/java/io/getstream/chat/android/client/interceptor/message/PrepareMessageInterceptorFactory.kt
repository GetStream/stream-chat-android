package io.getstream.chat.android.client.interceptor.message

import android.content.Context
import android.net.ConnectivityManager
import io.getstream.chat.android.client.network.NetworkStateProvider

public class PrepareMessageInterceptorFactory {

    public fun create(context: Context): PrepareMessageInterceptor {
        val networkStateProvider =
            NetworkStateProvider(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

        return PrepareMessageInterceptorImpl(networkStateProvider)
    }
}
