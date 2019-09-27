package com.getstream.sdk.chat.rest.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WsErrorMessage {
    public ErrorResponse getError() {
        return error;
    }

    @SerializedName("error")
    @Expose
    private ErrorResponse error;
}
