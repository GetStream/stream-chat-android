package com.getstream.sdk.chat.rest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;
import androidx.room.TypeConverters;

import com.getstream.sdk.chat.interfaces.UserEntity;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.rest.adapter.MessageGsonAdapter;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.storage.converter.AttachmentListConverter;
import com.getstream.sdk.chat.storage.converter.CommandInfoConverter;
import com.getstream.sdk.chat.storage.converter.DateConverter;
import com.getstream.sdk.chat.storage.converter.ExtraDataConverter;
import com.getstream.sdk.chat.storage.converter.ReactionCountConverter;
import com.getstream.sdk.chat.storage.converter.ReactionListConverter;
import com.getstream.sdk.chat.storage.converter.UserListConverter;
import com.getstream.sdk.chat.utils.Utils;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import static com.getstream.sdk.chat.enums.Dates.TODAY;
import static com.getstream.sdk.chat.enums.Dates.YESTERDAY;

/**
 * A message
 */

@Entity(tableName = "stream_message",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "user_id"),
                @ForeignKey(entity = Channel.class,
                        parentColumns = "cid",
                        childColumns = "cid")},
        indices = {
                @Index(value = {"user_id"}), @Index(value = {"cid", "created_at"})
        }
)

@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@JsonAdapter(MessageGsonAdapter.class)
public class Message implements UserEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @NonNull
    private String cid;

    @SerializedName("text")
    private String text;

    @SerializedName("html")
    private String html;

    @SerializedName("type")
    private String type;

    private Integer syncStatus;

    @Ignore
    @SerializedName("user")
    private User user;

    @Ignore
    @SerializedName("channel")
    private Channel channel;

    @ColumnInfo(name = "user_id")
    private String userID;

    @TypeConverters(AttachmentListConverter.class)
    private List<Attachment> attachments;

    @TypeConverters(ReactionListConverter.class)
    private List<Reaction> latestReactions;

    @TypeConverters(ReactionListConverter.class)
    private List<Reaction> ownReactions;

    @SerializedName("reply_count")
    private int replyCount;

    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    @TypeConverters({DateConverter.class})
    private Date createdAt;

    @SerializedName("updated_at")
    @TypeConverters({DateConverter.class})
    private Date updatedAt;

    @TypeConverters({DateConverter.class})
    private Date deletedAt;

    @SerializedName("mentioned_users")
    @TypeConverters(UserListConverter.class)
    private List<User> mentionedUsers;

    @Ignore
    private List<String> mentionedUsersId;

    @TypeConverters(ReactionCountConverter.class)
    private Map<String, Integer> reactionCounts;

    private String parentId;

    private String command;

    @TypeConverters(CommandInfoConverter.class)
    private Map<String, String> commandInfo;

    @NonNull
    @TypeConverters(ExtraDataConverter.class)
    private HashMap<String, Object> extraData;

    private boolean isStartDay = false;
    private boolean isYesterday = false;
    private boolean isToday = false;
    private String date, time;

    public Message() {
        this.extraData = new HashMap<>();
        this.setSyncStatus(Sync.IN_MEMORY);
        this.setType("regular");
    }

    public Message(String text) {
        this();
        setText(text);
    }

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
        Utils.messageDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(message.getCreatedAt().getTime());

        Calendar now = Calendar.getInstance();

        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            message.setToday(true);
            message.setDate(TODAY.getLabel());
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            message.setYesterday(true);
            message.setDate(YESTERDAY.getLabel());
        } else if (now.get(Calendar.WEEK_OF_YEAR) == smsTime.get(Calendar.WEEK_OF_YEAR)) {
            DateFormat dayName = new SimpleDateFormat("EEEE");
            message.setDate(dayName.format(message.getCreatedAt()));
        } else {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG);
            message.setDate(dateFormat.format(message.getCreatedAt()));
        }
        DateFormat timeFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        message.setTime(timeFormat.format(message.getCreatedAt()));
    }

    public static String convertDateToString(Date date) {
        Utils.messageDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String timeStr = Utils.messageDateFormat.format(date);
        return timeStr;
    }

    public static boolean isCommandMessage(Message message) {
        return message.getText().startsWith("/");
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Message otherMessage = (Message) obj;
        if (!Objects.equals(id, otherMessage.getId())) {
            return false;
        }

        if (!Objects.equals(updatedAt, otherMessage.updatedAt)) {
            return false;
        }

        if (!Objects.equals(deletedAt, otherMessage.deletedAt)) {
            return false;
        }
        return replyCount == otherMessage.replyCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Message copy() {
        Message clone = new Message();
        clone.id = id;
        clone.text = text;
        clone.html = html;
        clone.type = type;
        clone.user = user;
        clone.attachments = attachments;
        clone.latestReactions = latestReactions;
        clone.ownReactions = ownReactions;
        clone.reactionCounts = reactionCounts;
        clone.replyCount = replyCount;
        clone.createdAt = new Date(createdAt.getTime());
        if (updatedAt != null) {
            clone.updatedAt = new Date(updatedAt.getTime());
        }
        if (deletedAt != null) {
            clone.deletedAt = new Date(deletedAt.getTime());
        }
        if (!extraData.isEmpty()) {
            clone.extraData = new HashMap<>(extraData);
        }
        clone.mentionedUsers = mentionedUsers;
        clone.parentId = parentId;
        clone.command = command;
        clone.commandInfo = commandInfo;
        clone.syncStatus = syncStatus;
        return clone;
    }

    public void preStorage() {
        this.userID = this.getUser().getId();
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @TypeConverters(AttachmentListConverter.class)
    public List<Attachment> getAttachments() {
        if (attachments == null) {
            return new ArrayList<Attachment>();
        }
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

    public List<String> getMentionedUsersId() {
        return mentionedUsersId;
    }

    public void setMentionedUsersId(List<String> mentionedUsersId) {
        this.mentionedUsersId = mentionedUsersId;
    }

    public Map<String, Integer> getReactionCounts() {
        return reactionCounts;
    }

    public void setReactionCounts(Map<String, Integer> reactionCounts) {
        this.reactionCounts = reactionCounts;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
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

    @Override
    public String getUserId() {
        if (user == null) {
            return null;
        }
        return user.getId();
    }

    public HashMap<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(HashMap<String, Object> extraData) {
        this.extraData = new HashMap<>(extraData);
        this.extraData.remove("id");
    }


    @NonNull
    public String getCid() {
        return cid;
    }

    public void setCid(@NonNull String cid) {
        this.cid = cid;
    }

    public @Sync.Status
    Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(@Sync.Status Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    public boolean hasAttachments() {
        return getAttachments() != null && !getAttachments().isEmpty();
    }
}