package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.model.Mute;
import com.getstream.sdk.chat.rest.User;
import com.google.gson.annotations.SerializedName;

public class MuteUserResponse {
    @SerializedName("mute")
    Mute mute;

    @SerializedName("own_user")
    User own_user;

}
