package com.getstream.sdk.chat.model.channel;

import com.getstream.sdk.chat.model.Command;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Config {
    @SerializedName("created_at")
    @Expose
    private String created_at;

    @SerializedName("updated_at")
    @Expose
    private String updated_at;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("typing_events")
    @Expose
    private boolean typing_events;

    @SerializedName("read_events")
    @Expose
    private boolean read_events;

    @SerializedName("connect_events")
    @Expose
    private boolean connect_events;

    @SerializedName("search")
    @Expose
    private boolean search;

    @SerializedName("reactions")
    @Expose
    private boolean reactions;

    @SerializedName("replies")
    @Expose
    private boolean replies;

    @SerializedName("mutes")
    @Expose
    private boolean mutes;

    @SerializedName("infinite")
    @Expose
    private String infinite;

    @SerializedName("max_message_length")
    @Expose
    private int max_message_length;

    @SerializedName("automod")
    @Expose
    private String automod;

    @SerializedName("commands")
    @Expose
    private List<Command>commands;


    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public String getName() {
        return name;
    }

    public boolean isTyping_events() {
        return typing_events;
    }

    public boolean isRead_events() {
        return read_events;
    }

    public boolean isConnect_events() {
        return connect_events;
    }

    public boolean isSearch() {
        return search;
    }

    public boolean isReactions() {
        return reactions;
    }

    public boolean isReplies() {
        return replies;
    }

    public boolean isMutes() {
        return mutes;
    }

    public String getInfinite() {
        return infinite;
    }

    public int getMax_message_length() {
        return max_message_length;
    }

    public String getAutomod() {
        return automod;
    }

    public List<Command> getCommands() {
        return commands;
    }
}
