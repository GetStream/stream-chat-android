package com.getstream.sdk.chat.model;

import androidx.room.TypeConverters;

import com.getstream.sdk.chat.CommandListConverter;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * A config
 */

public class Config {
    @SerializedName("created_at")
    private Date created_at;

    @SerializedName("updated_at")
    private Date updated_at;

    public void setName(String name) {
        this.name = name;
    }

    @SerializedName("name")
    private String name;

    @SerializedName("typing_events")
    private boolean typingEvents;


    public void setConnect_events(boolean connect_events) {
        this.connect_events = connect_events;
    }

    @SerializedName("read_events")
    private boolean readEvents;

    @SerializedName("connect_events")
    private boolean connect_events;

    @SerializedName("search")
    private boolean search;

    @SerializedName("reactions")
    private boolean reactions;

    public void setInfinite(String infinite) {
        this.infinite = infinite;
    }

    public void setMax_message_length(int max_message_length) {
        this.max_message_length = max_message_length;
    }

    @SerializedName("replies")
    private boolean replies;

    @SerializedName("mutes")
    private boolean mutes;

    @SerializedName("infinite")
    private String infinite;

    @SerializedName("max_message_length")
    private int max_message_length;

    public void setAutomod(String automod) {
        this.automod = automod;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    @SerializedName("automod")
    private String automod;

    @SerializedName("commands")
    @TypeConverters(CommandListConverter.class)
    private List<Command>commands;

    public Date getCreatedAt() {
        return created_at;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public String getName() {
        return name;
    }




    public boolean isReadEvents() {
        return readEvents;
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

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCreated_at() {
       return this.created_at;
    }

    public String getUpdated_at() {
        return this.updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public void setReadEvents(boolean readEvents) {
        this.readEvents = readEvents;
    }

    public void setTypingEvents(boolean typingEvents) {
        this.typingEvents = typingEvents;
    }

    public boolean isTypingEvents() {
        return typingEvents;
    }

    public boolean isConnect_events() {
        return connect_events;
    }

    public boolean isSearch() {
        return search;
    }

    public void setSearch(boolean search) {
        this.search = search;
    }

    public boolean isReactions() {
        return reactions;
    }

    public void setReactions(boolean reactions) {
        this.reactions = reactions;
    }

    public boolean isReplies() {
        return replies;
    }

    public void setReplies(boolean replies) {
        this.replies = replies;
    }

    public boolean isMutes() {
        return mutes;
    }

    public void setMutes(boolean mutes) {
        this.mutes = mutes;
    }
}
