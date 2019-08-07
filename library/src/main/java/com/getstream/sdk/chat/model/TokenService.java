package com.getstream.sdk.chat.model;

import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONObject;

public class TokenService {

    public static String devToken(@NonNull String userId) throws Exception {
        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("User ID must be non-null");
        }


        String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"; //  //{"alg": "HS256", "typ": "JWT"}
        JSONObject payloadJson = new JSONObject();

        payloadJson.put("user_id", userId);
        String payload = payloadJson.toString();
        String payloadBase64 = Base64.encodeToString(payload.getBytes("UTF-8"), Base64.NO_WRAP);
        String devSignature = "devtoken";

        String[] a = new String[3];
        a[0] = header;
        a[1] = payloadBase64;
        a[2] = devSignature;
        return TextUtils.join(".", a);
    }

    public static String createGuestToken(@NonNull String userId) throws Exception {
        // some progressing
        return devToken(userId);
    }
}
