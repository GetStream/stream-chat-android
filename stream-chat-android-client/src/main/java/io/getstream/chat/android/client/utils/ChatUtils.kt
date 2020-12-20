package io.getstream.chat.android.client.utils

import android.util.Base64
import java.nio.charset.StandardCharsets

public object ChatUtils {
    @JvmStatic
    public fun devToken(userId: String): String {
        require(userId.isNotEmpty()) { "User id must not be empty" }
        val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" //  {"alg": "HS256", "typ": "JWT"}
        val devSignature = "devtoken"
        val a = arrayOfNulls<String>(3)
        val payload = "{\"user_id\":\"$userId\"}"
        val payloadBase64 = Base64.encodeToString(payload.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        a[0] = header
        a[1] = payloadBase64
        a[2] = devSignature
        return a.joinToString(".")
    }
}
