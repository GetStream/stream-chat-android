package com.getstream.sdk.chat.model;

import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;

import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.storage.converter.DateConverter;
import com.getstream.sdk.chat.storage.converter.ExtraDataConverter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Map;

public class Reaction {

    @ColumnInfo(name = "message_id")
    private String messageId;

    @SerializedName("user")
    @Expose
    private User user;

    @ColumnInfo(name = "user_id")
    private String userID;

    @SerializedName("type")
    @Expose
    private String type;

    @ColumnInfo(name = "created_at")
    @TypeConverters({DateConverter.class})
    private Date createdAt;

    @SerializedName("score")
    @Expose
    private Integer score;

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }

    @TypeConverters(ExtraDataConverter.class)
    private Map<String, Object> extraData;

    public Reaction() {
        this.setSyncStatus(Sync.SYNCED);
    }

    public Reaction(String messageId, User user, String type, Map<String, Object> extraData) {
        this.messageId = messageId;
        this.user = user;
        this.userID = user.getId();
        this.type = type;
        this.extraData = extraData;
    }

    private Integer syncStatus;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public @Sync.Status Integer getSyncStatus() {
        return syncStatus;
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public void setSyncStatus(@Sync.Status Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getMessageId() {
        return messageId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

}
