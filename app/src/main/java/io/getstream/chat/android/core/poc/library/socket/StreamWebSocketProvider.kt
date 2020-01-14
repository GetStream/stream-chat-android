package io.getstream.chat.android.core.poc.library.socket

import com.google.gson.Gson
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.api.ApiClientOptions
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class StreamWebSocketServiceProvider(val options: ApiClientOptions, val apiKey: String) {


    fun provideWebSocketService(
        user: User,
        userToken: String?,
        listener: WSResponseHandler?,
        anonymousAuth: Boolean
    ): StreamWebSocketService {
        val wsUrl = getWsUrl(userToken, user, anonymousAuth)
        //Log.d(TAG, "WebSocket URL : $wsUrl")
        return StreamWebSocketService(wsUrl!!, listener!!)
    }

    fun getWsUrl(
        userToken: String?,
        user: User,
        anonymousAuth: Boolean
    ): String? {
        if (anonymousAuth && userToken != null) {
            //Log.e(TAG, "Can\'t use anonymousAuth with userToken. UserToken will be ignored")
        }
        if (!anonymousAuth && userToken == null) {
            //Log.e(TAG, "userToken must be non-null in non anonymous mode")
            return null
        }
        var json = buildUserDetailJSON(user)
        return try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.name())
            val baseWsUrl: String =
                options.wssURL + "connect?json=" + json + "&api_key=" + apiKey
            if (anonymousAuth) {
                "$baseWsUrl&stream-auth-type=anonymous"
            } else {
                "$baseWsUrl&authorization=$userToken&stream-auth-type=jwt"
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            throw UnsupportedEncodingException("Unable to encode user details json: $json")
        }
    }

    fun buildUserDetailJSON(user: User): String {
        val data: HashMap<String, Any> = HashMap()
        data["user_details"] = user
        data["server_determines_connection_id"] = true
        data["user_id"] = user.id
        return Gson().toJson(data)
    }

    companion object {
        private val TAG: String = StreamWebSocketServiceProvider.javaClass.simpleName
    }
}
