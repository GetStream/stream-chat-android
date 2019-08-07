package com.getstream.sdk.chat.rest;

import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.utils.Global;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
    private String updatedAt;

    @SerializedName("deleted_at")
    @Expose
    private String deletedAt;

    @SerializedName("mentioned_users")
    @Expose
    private List<User> mentionedUsers;

    @SerializedName("reaction_counts")
    @Expose
    private Map<String, Integer> reactionCounts;

    @SerializedName("parent_id")
    @Expose
    private String parentId;

    @SerializedName("command")
    @Expose
    private String command;

    @SerializedName("command_info")
    @Expose
    private Map<String, String> commandInfo;

    // Additional Params
    private Map<String, Object> extraData;


    private boolean isStartDay = false;
    private boolean isYesterday = false;
    private boolean isToday = false;
    private String created, edited, deleted;
    private String date, time;
    private boolean isDelivered = false;

    // region Set Date and Time
    public static void setStartDay(List<Message> messages, @Nullable Message preMessage0) {
        if (messages == null) return;
        if (messages.size() == 0) return;

        Message preMessage = (preMessage0 != null) ? preMessage0 : messages.get(0);
        setFormattedDate(preMessage);
        int startIndex = (preMessage0 != null) ? 0 : 1;
        for (int i = startIndex; i < messages.size(); i++) {
            if (i != startIndex) {
                preMessage = messages.get(i - 1);
            }

            Message message = messages.get(i);
            setFormattedDate(message);
            message.setStartDay(!message.getDate().equals(preMessage.getDate()));
        }
    }

    private static void setFormattedDate(Message message) {
        if (message == null || message.getDate() != null) return;
        Global.messageDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String sendDate = message.getCreatedAt();

        Date date = null;
        try {
            date = Global.messageDateFormat.parse(sendDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(date.getTime());

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEEE";

        DateFormat timeFormat = new SimpleDateFormat(timeFormatString, Global.locale);
        DateFormat dateFormat1 = new SimpleDateFormat(dateTimeFormatString, Global.locale);
        DateFormat dateFormat2 = new SimpleDateFormat("MMMM dd yyyy", Global.locale);

        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            message.setToday(true);
            message.setDate("Today");
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            message.setYesterday(true);
            message.setDate("Yesterday");
        } else if (now.get(Calendar.WEEK_OF_YEAR) == smsTime.get(Calendar.WEEK_OF_YEAR)) {
            message.setDate(dateFormat1.format(date));
        } else {
            message.setDate(dateFormat2.format(date));
        }
        message.setTime(timeFormat.format(date));
        message.setCreated(dateFormat2.format(date));
    }

    public static String convertDateToString(Date date) {
        Global.messageDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String timeStr = Global.messageDateFormat.format(date);
        return timeStr;
    }

    public static boolean isCommandMessage(Message message) {
        return message.getText().startsWith("/");
    }

    // Passed Time
    public static String differentTime(String dateStr) {
        if (TextUtils.isEmpty(dateStr)) return null;
        Date lastActiveDate = null;
        try {
            lastActiveDate = Global.messageDateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        Date dateTwo = new Date();
        long timeDiff = Math.abs(lastActiveDate.getTime() - dateTwo.getTime()) / 1000;
        String timeElapsed = TimeElapsed(timeDiff);
        String differTime = "";
        if (timeElapsed.contains("Just now"))
            differTime = "Active: " + timeElapsed;
        else
            differTime = "Active: " + timeElapsed + " ago";

        return differTime;
    }

    public static String TimeElapsed(long seconds) {
        String elapsed;
        if (seconds < 60) {
            elapsed = "Just now";
        } else if (seconds < 60 * 60) {
            int minutes = (int) (seconds / 60);
            elapsed = String.valueOf(minutes) + " " + ((minutes > 1) ? "mins" : "min");
        } else if (seconds < 24 * 60 * 60) {
            int hours = (int) (seconds / (60 * 60));
            elapsed = String.valueOf(hours) + " " + ((hours > 1) ? "hours" : "hour");
        } else {
            int days = (int) (seconds / (24 * 60 * 60));
            elapsed = String.valueOf(days) + " " + ((days > 1) ? "days" : "day");
        }
        return elapsed;
    }

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

    public String getCreatedAt() {
        return created_at;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParent_id(String parentId) {
        this.parentId = parentId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getCommandInfo() {
        return commandInfo;
    }

    public void setCommandInfo(Map<String, String> commandInfo) {
        this.commandInfo = commandInfo;
    }

    public boolean isIncoming() {
        return !this.getUser().getId().equals(StreamChat.getInstance().getUserId());
    }
}
