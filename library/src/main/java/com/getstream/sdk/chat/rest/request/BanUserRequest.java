package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("WeakerAccess")
public class BanUserRequest {

    @NotNull
    @SerializedName("target_user_id")
    String targetUserId;

    @Nullable
    @SerializedName("timeout")
    Integer timeout;

    @Nullable
    @SerializedName("reason")
    String reason;

    @Nullable
    @SerializedName("type")
    String channelType;

    @Nullable
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
