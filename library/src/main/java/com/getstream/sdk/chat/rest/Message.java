package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.utils.Global;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * A message
 */
public class Message {
    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("text")
    @Expose
    private String text;

    @SerializedName("html")
    @Expose
    private String html;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("user")
    @Expose
    private User user;

    @SerializedName("attachments")
    @Expose
    private List<Attachment> attachments;

    @SerializedName("latest_reactions")
    @Expose
    private List<Reaction> latestReactions;

    @SerializedName("own_reactions")
    @Expose
    private List<Reaction> ownReactions;

    @SerializedName("reply_count")
    @Expose
    private int replyCount;

    @SerializedName("created_at")
    @Expose
    private String created_at;

    @SerializedName("updated_at")
    @Expose
    private String updated_at;

    @SerializedName("deleted_at")
    @Expose
    private String deleted_at;

    @SerializedName("mentioned_users")
    @Expose
    private List<User> mentionedUsers;

    @SerializedName("reaction_counts")
    @Expose
    private Map<String, Integer> reactionCounts;

    @SerializedName("parent_id")
    @Expose
    private String parent_id;

    @SerializedName("command")
    @Expose
    private String command;

    @SerializedName("command_info")
    @Expose
    private Map<String, String> command_info;

    // Additional Params
    private Map<String, Object> extraData;


    private boolean isStartDay = false;
    private boolean isYesterday = false;
    private boolean isToday = false;
    private String created, edited, deleted;
    private String date, time;
    private boolean isDelivered = false;

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getEdited() {
        return edited;
    }

    public void setEdited(String edited) {
        this.edited = edited;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public boolean isYesterday() {
        return isYesterday;
    }

    public void setYesterday(boolean yesterday) {
        isYesterday = yesterday;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public boolean isStartDay() {
        return isStartDay;
    }

    public void setStartDay(boolean startDay) {
        isStartDay = startDay;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public List<Reaction> getLatestReactions() {
        return latestReactions;
    }

    public void setLatestReactions(List<Reaction> latestReactions) {
        this.latestReactions = latestReactions;
    }

    public List<Reaction> getOwnReactions() {
        return ownReactions;
    }

    public void setOwnReactions(List<Reaction> ownReactions) {
        this.ownReactions = ownReactions;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }


    public List<User> getMentionedUsers() {
        return mentionedUsers;
    }

    public void setMentionedUsers(List<User> mentionedUsers) {
        this.mentionedUsers = mentionedUsers;
    }

    public Map<String, Integer> getReactionCounts() {
        return reactionCounts;
    }

    public void setReactionCounts(Map<String, Integer> reactionCounts) {
        this.reactionCounts = reactionCounts;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getCommand_info() {
        return command_info;
    }

    public void setCommand_info(Map<String, String> command_info) {
        this.command_info = command_info;
    }

    public boolean isIncoming() {
        try {
            return !this.getUser().getId().equals(Global.client.user.getId());
        } catch (Exception e) {
        }
        return false;
    }
}
