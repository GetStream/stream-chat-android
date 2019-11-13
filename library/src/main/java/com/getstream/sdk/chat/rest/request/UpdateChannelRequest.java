package com.getstream.sdk.chat.rest.request;


import com.getstream.sdk.chat.rest.Message;
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
    private Message updateMessage;

    public UpdateChannelRequest(@NotNull Map<String, Object> data, @Nullable Message updateMessage) {
        // remove reserved fields for server api call
        data.remove("members");
        this.data = data;
        this.updateMessage = updateMessage;
    }

    @NotNull
    public Map<String, Object> getData() {
        return data;
    }

    @Nullable
    public Message getUpdateMessage() {
        return updateMessage;
    }
}
