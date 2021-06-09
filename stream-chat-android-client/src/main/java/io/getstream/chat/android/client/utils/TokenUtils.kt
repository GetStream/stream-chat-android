package io.getstream.chat.android.client.utils

import android.util.Base64
import io.getstream.chat.android.client.logger.ChatLogger
import org.json.JSONObject
import java.lang.Exception
import java.nio.charset.StandardCharsets

internal object TokenUtils {

    fun getUserId(token: String): String = try {
        JSONObject(
            String(
                Base64.decode(
                    (token.split(".")[1]).toByteArray(StandardCharsets.UTF_8),
                    Base64.NO_WRAP
                )
            )
        ).optString("user_id")
    } catch (e: Exception) {
        ChatLogger.get("TokenUtils").logE("Unable to obtain userId from JWT Token Payload", e)
        ""
    }
}
