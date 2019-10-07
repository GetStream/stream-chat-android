package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BanUserRequest {

    @NotNull
    @Expose
    @SerializedName("target_user_id")
    String targetUserId;

    @Nullable
    @Expose
    @SerializedName("timeout")
    Integer timeout;

    @Nullable
    @Expose
    @SerializedName("reason")
    String reason;

    @Nullable
    @Expose
    @SerializedName("type")
    String channelType;

    @Nullable
    @Expose
    @SerializedName("id")
    String channelId;

    public BanUserRequest(@NotNull String targetUserId, @Nullable Integer timeout,
                          @Nullable String reason, @Nullable String channelType, @Nullable String channelId) {
        this.targetUserId = targetUserId;
        this.timeout = timeout;
        this.reason = reason;
        this.channelType = channelType;
        this.channelId = channelId;
    }
}
