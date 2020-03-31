package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.model.Mute;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.getstream.chat.android.client.models.User;

public class MuteUserResponse {
    @SerializedName("mute")
    @Expose
    Mute mute;

    @SerializedName("own_user")
    @Expose
    User own_user;

}
