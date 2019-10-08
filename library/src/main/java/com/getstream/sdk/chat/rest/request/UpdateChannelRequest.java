package com.getstream.sdk.chat.rest.request;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/*
 * Created by Anton Bevza on 2019-10-04.
 */
@SuppressWarnings("FieldCanBeLocal")
public class UpdateChannelRequest {

    @NotNull
    @Expose
    @SerializedName("data")
    private Map<String, Object> data;

    @Nullable
    @Expose
    @SerializedName("message")
    private UpdateMessage updateMessage;

    public UpdateChannelRequest(@NotNull Map<String, Object> data, @Nullable String updateMessage) {
        this.data = data;
        this.updateMessage = updateMessage != null ? new UpdateMessage(updateMessage) : null;
    }

    private class UpdateMessage {

        @Nullable
        @Expose
        @SerializedName("text")
        private String text;

        UpdateMessage(@Nullable String text) {
            this.text = text;
        }
    }
}
