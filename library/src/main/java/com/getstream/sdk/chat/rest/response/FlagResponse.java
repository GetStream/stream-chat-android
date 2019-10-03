package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.model.Flag;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FlagResponse {
    @SerializedName("flag")
    @Expose
    Flag flag;

    public Flag getFlag() {
        return flag;
    }
}
