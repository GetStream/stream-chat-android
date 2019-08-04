package com.getstream.sdk.chat.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * A config
 */

public class Config {
    @SerializedName("created_at")
    private String created_at;

    @SerializedName("updated_at")
    private String updated_at;

    @SerializedName("name")
    private String name;

    @SerializedName("typing_events")
    private boolean typing_events;

    @SerializedName("read_events")
    private boolean read_events;

    @SerializedName("connect_events")
    private boolean connect_events;

    @SerializedName("search")
    private boolean search;

    @SerializedName("reactions")
    private boolean reactions;

    @SerializedName("replies")
    private boolean replies;

    @SerializedName("mutes")
    private boolean mutes;

    @SerializedName("infinite")
    private String infinite;

    @SerializedName("max_message_length")
    private int max_message_length;

    @SerializedName("automod")
    private String automod;

    @SerializedName("commands")
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
