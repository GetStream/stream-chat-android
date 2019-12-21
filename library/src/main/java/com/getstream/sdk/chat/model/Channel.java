package com.getstream.sdk.chat.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;
import androidx.room.TypeConverters;

import com.getstream.sdk.chat.EventSubscriberRegistry;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.adapter.ChannelGsonAdapter;
import com.getstream.sdk.chat.rest.core.ChatChannelEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.FlagCallback;
import com.getstream.sdk.chat.rest.interfaces.GetReactionsCallback;
import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryWatchCallback;
import com.getstream.sdk.chat.rest.interfaces.UploadFileCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.request.ChannelWatchRequest;
import com.getstream.sdk.chat.rest.request.HideChannelRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.storage.converter.DateConverter;
import com.getstream.sdk.chat.storage.converter.ExtraDataConverter;
import com.getstream.sdk.chat.utils.Utils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A channel
 */
@Entity(tableName = "stream_channel",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "created_by_user_id"),
        indices = {
                @Index(value = {"created_by_user_id"})
        }
)
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@JsonAdapter(ChannelGsonAdapter.class)
public class Channel {
    private static final String TAG = Channel.class.getSimpleName();

    @PrimaryKey
    @NonNull
    @SerializedName("cid")
    @Expose
    private String cid;

    @SerializedName("id")
    @Expose
    private String id;

    @NonNull
    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("last_message_at")
    @Expose
    @TypeConverters(DateConverter.class)
    private Date lastMessageDate;

    public @Sync.Status
    Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(@Sync.Status Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    private Integer syncStatus;

    public Date getLastKeystrokeAt() {
        return lastKeystrokeAt;
    }

    public void setLastKeystrokeAt(Date lastKeystrokeAt) {
        this.lastKeystrokeAt = lastKeystrokeAt;
    }

    @Ignore
    private Date lastKeystrokeAt;

    public Date getLastStartTypingEvent() {
        return lastStartTypingEvent;
    }

    public void setLastStartTypingEvent(Date lastStartTypingEvent) {
        this.lastStartTypingEvent = lastStartTypingEvent;
    }

    @Ignore
    private Date lastStartTypingEvent;

    @Embedded(prefix = "state_")
    private ChannelState lastState;

    @SerializedName("created_at")
    @Expose
    @TypeConverters(DateConverter.class)
    private Date createdAt;

    @SerializedName("deleted_at")
    @Expose
    @Nullable
    @TypeConverters(DateConverter.class)
    private Date deletedAt;

    @SerializedName("updated_at")
    @Expose
    @TypeConverters(DateConverter.class)
    private Date updatedAt;

    @SerializedName("created_by")
    @Expose
    @Ignore
    private User createdByUser;

    @ColumnInfo(name = "created_by_user_id")
    private String createdByUserID;

    @SerializedName("frozen")
    @Expose
    private boolean frozen;

    @SerializedName("config")
    @Expose
    @Embedded(prefix = "config_")
    private Config config;

    @NonNull
    @TypeConverters(ExtraDataConverter.class)
    private HashMap<String, Object> extraData;

    @Ignore
    private Map<String, String> reactionTypes;

    @Ignore
    private EventSubscriberRegistry<ChatChannelEventHandler> subRegistery;

    @Ignore
    private Client client;

    @Ignore
    private ChannelState channelState;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Ignore
    private boolean initialized;

    // this constructor is here for GSON to play fair
    public Channel() {
        this(null, "", "", new HashMap<>());
    }

    /**
     * constructor - Create a channel
     *
     * @param client the chat client
     * @param type   the type of channel
     * @param id     the id of the chat
     * @return Returns a new uninitialized channel
     */
    public Channel(Client client, String type, String id) {
        this(client, type, id, new HashMap<>());
    }

    /**
     * constructor - Create a channel
     *
     * @param client    the chat client
     * @param type      the type of channel
     * @param id        the id of the chat
     * @param extraData any additional custom params
     * @return Returns a new uninitialized channel
     */
    public Channel(Client client, String type, String id, HashMap<String, Object> extraData) {
        this.type = type;
        this.id = id;
        this.cid = String.format("%1$s:%2$s", type, id);
        this.client = client;
        this.setSyncStatus(Sync.SYNCED);
        this.createdAt = new Date();

        if (extraData == null) {
            this.extraData = new HashMap<>();
        } else {
            this.extraData = new HashMap<>(extraData);
        }

        subRegistery = new EventSubscriberRegistry<>();
        channelState = new ChannelState(this);
        initialized = false;
    }

    public Channel(Client client, String type, HashMap<String, Object> extraData, List<String> members) {
        this(client, type, null, extraData);
        this.extraData.put("members", members);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date d) {
        this.createdAt = d;
    }

    @Nullable
    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(@Nullable Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null; //if field DeletedAt is specified the channel was deleted
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }


    // region Getter & Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.cid = String.format("%1$s:%2$s", type, id);
    }

    public String getCid() {
        return this.cid;
    }

    public void setCid(String id) {
        this.cid = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public User getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getName() {
        Object name = extraData.get("name");
        if (name instanceof String) {
            return (String) name;
        }
        return null;
    }

    public void setName(String name) {
        extraData.put("name", name);
    }

    public String getImage() {
        Object image = extraData.get("image");
        if (image instanceof String) {
            return (String) image;
        }
        return null;
    }

    public void setImage(String image) {
        extraData.put("image", image);
    }

    public Channel copy() {
        Channel clone = new Channel(client, type, id);
        if (lastMessageDate != null) {
            clone.lastMessageDate = new Date(lastMessageDate.getTime());
        }
        if (createdAt != null) {
            clone.createdAt = new Date(createdAt.getTime());
        }
        if (deletedAt != null) {
            clone.deletedAt = new Date(deletedAt.getTime());
        }
        if (updatedAt != null) {
            clone.updatedAt = new Date(updatedAt.getTime());
        }
        if (channelState != null) {
            clone.channelState = channelState.copy();
        }
        if (!extraData.isEmpty()) {
            clone.extraData = new HashMap<>(extraData);
        }
        return clone;
    }
    // endregion

    // region Constructor

    public Map<String, String> getReactionTypes() {
        if (reactionTypes == null) {
            reactionTypes = new HashMap<String, String>() {
                {
                    put("like", "\uD83D\uDC4D");
                    put("love", "\u2764\uFE0F");
                    put("haha", "\uD83D\uDE02");
                    put("wow", "\uD83D\uDE32");
                    put("sad", " \uD83D\uDE41");
                    put("angry", "\uD83D\uDE21");
                }
            };
        }
        return reactionTypes;
    }

    public void setReactionTypes(Map<String, String> reactionTypes) {
        this.reactionTypes = reactionTypes;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Channel channel = (Channel) obj;
        // we compare based on the CID
        return Objects.equals(cid, channel.cid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cid);
    }

    @NotNull
    public HashMap<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(HashMap<String, Object> extraData) {
        this.extraData = extraData;
    }

    public void preStorage() {
        this.lastState = this.channelState;
        this.createdByUserID = this.createdByUser.getId();
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    // endregion

    public final int addEventHandler(ChatChannelEventHandler handler) {
        Integer subID = subRegistery.addSubscription(handler);
        return subID;
    }

    public final void removeEventHandler(Integer subID) {
        subRegistery.removeSubscription(subID);
    }

    public final void handleChannelEvent(Event event) {
        for (ChatChannelEventHandler handler : subRegistery.getSubscribers()) {
            handler.dispatchEvent(event);
        }
    }

    public void mergeWithState(ChannelState state) {
        channelState.init(state);
        config = state.getChannel().config;
        lastMessageDate = state.getChannel().lastMessageDate;
        extraData = state.getChannel().extraData;
        createdAt = state.getChannel().createdAt;
        updatedAt = state.getChannel().updatedAt;
        deletedAt = state.getChannel().deletedAt;
    }

    /**
     * watch - Loads the initial channel state and watches for changes
     */
    public void watch(@NonNull ChannelWatchRequest request,
                      @NonNull QueryWatchCallback callback) {
        query(request, new QueryChannelCallback() {
            @Override
            public void onSuccess(ChannelState response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    /**
     * Query the API, get messages, members or other channel fields
     *
     * @param request  request options
     * @param callback the result callback
     */
    public void query(@NonNull ChannelQueryRequest request,
                      @NonNull QueryChannelCallback callback) {
        client.queryChannel(this, request, callback);
    }

    /**
     * Only for Test
     * Query the API, get messages, members or other channel fields
     *
     * @param callback the result callback
     */
    public void query(@NonNull QueryChannelCallback callback) {
        client.queryChannel(this, new ChannelQueryRequest().withData(this.extraData).withWatch(), callback);
    }

    /**
     * getReplies - List the message replies for a parent message
     *
     * @return {object} Returns a getReplies response
     */
    public void getReplies(@NonNull String parentId,
                           int limit,
                           @NonNull String firstMessageId,
                           @NonNull GetRepliesCallback callback) {
        client.getReplies(parentId, limit, firstMessageId, callback);
    }

    public ChannelState getChannelState() {
        return channelState;
    }

    public void setChannelState(ChannelState channelState) {
        this.channelState = channelState;
    }

    // TODO: move this somewhere else
    public String getInitials() {
        String name = this.getName();
        if (name == null) {
            return "";
        }
        String[] names = name.split(" ");
        String firstName = names[0];
        String lastName = null;
        try {
            lastName = names[1];
        } catch (Exception e) {
        }

        if (!TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName))
            return firstName.substring(0, 1).toUpperCase();
        if (TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName))
            return lastName.substring(0, 1).toUpperCase();

        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName))
            return firstName.substring(0, 1).toUpperCase() + lastName.substring(0, 1).toUpperCase();
        return null;
    }

    /**
     * countUnread - Count the number of unread messages mentioning the current user
     *
     * @return {int} Unread mentions count
     */
    public int countUnreadMentions() {
        Date lastRead = channelState.getReadDateOfChannelLastMessage(client.getUserId());
        int count = 0;
        for (Message m : this.channelState.getMessages()) {
            if (client.getUser().getId().equals(m.getUserId())) {
                continue;
            }
            if (lastRead == null) {
                count++;
                continue;
            }
            if (m.getCreatedAt().getTime() > lastRead.getTime()) {
                if (m.getMentionedUsers().indexOf(client.getUser()) != -1) {
                    count++;
                }
            }
        }
        return count;
    }

    // region Message
    public void sendMessage(@NonNull Message message,
                            @NonNull MessageCallback callback) {
        List<String> mentionedUserIDs = Utils.getMentionedUserIDs(channelState, message.getText());
        if (mentionedUserIDs != null && !mentionedUserIDs.isEmpty())
            message.setMentionedUsersId(mentionedUserIDs);
        client.sendMessage(this, message, callback);
    }

    public void updateMessage(@NonNull Message message,
                              MessageCallback callback) {
        List<String> mentionedUserIDs = Utils.getMentionedUserIDs(channelState, message.getText());
        if (mentionedUserIDs != null && !mentionedUserIDs.isEmpty())
            message.setMentionedUsersId(mentionedUserIDs);
        client.updateMessage(message, callback);
    }

    public void deleteMessage(@NonNull Message message,
                              MessageCallback callback) {
        client.deleteMessage(message.getId(), callback);
    }

    public void sendImage(@NotNull String filePath,
                          @NotNull String mimeType,
                          @NotNull UploadFileCallback fileCallback) {
        File file = new File(filePath);

        client.getUploadStorage().sendFile(this, file, mimeType, fileCallback);
    }

    public void sendFile(@NotNull String filePath,
                         @NotNull String mimeType,
                         @NotNull UploadFileCallback fileCallback) {
        File file = new File(filePath);

        client.getUploadStorage().sendFile(this, file, mimeType, fileCallback);
    }

    /**
     * Delete a file with a given URL.
     *
     * @param url      the file URL
     * @param callback the result callback
     */
    public void deleteFile(@NotNull String url, @NotNull CompletableCallback callback) {
        client.getUploadStorage().deleteFile(this, url, callback);
    }

    /**
     * Delete a image with a given URL.
     *
     * @param url      the image URL
     * @param callback the result callback
     */
    public void deleteImage(@NotNull String url, @NotNull CompletableCallback callback) {
        client.getUploadStorage().deleteImage(this, url, callback);
    }

    // endregion

    /**
     * sendReaction - Send a reaction about a message
     *
     * @param reaction {Reaction} the reaction object
     * @param callback {MessageCallback} the request callback
     */
    public void sendReaction(@NotNull Reaction reaction,
                             @NotNull MessageCallback callback) {
        ReactionRequest r = new ReactionRequest(reaction);
        client.sendReaction(r, callback);
    }

    /**
     * sendReaction - Send a reaction about a message
     *
     * @param messageID {string} the message id
     * @param type      {string} the type of reaction (ie. like)
     * @param extraData {Map<String, Object>} reaction extra data
     * @param callback  {MessageCallback} the request callback
     */
    public void sendReaction(@NotNull String messageID,
                             @NotNull String type,
                             Map<String, Object> extraData,
                             @NotNull MessageCallback callback) {
        Reaction reaction = new Reaction();
        reaction.setMessageId(messageID);
        reaction.setType(type);
        reaction.setExtraData(extraData);
        ReactionRequest r = new ReactionRequest(reaction);
        client.sendReaction(r, callback);
    }

    /**
     * list the reactions, supports pagination
     *
     * @param messageId  the message id
     * @param pagination pagination options
     * @param callback   the result callback
     */
    public void getReactions(@NotNull String messageId,
                             @NotNull PaginationOptions pagination,
                             @NotNull GetReactionsCallback callback) {
        client.getReactions(messageId, pagination, callback);
    }

    /**
     * list of reactions (10 most recent reactions)
     *
     * @param messageId the message id
     * @param callback  the result callback
     */
    public void getReactions(@NotNull String messageId,
                             @NotNull GetReactionsCallback callback) {
        client.getReactions(messageId, callback);
    }

    /**
     * deleteReaction - Delete a reaction by user and type
     *
     * @param messageId {string} the message id
     * @param type      {string} the type of reaction that should be removed
     * @param callback  {MessageCallback} the request callback
     */
    public void deleteReaction(@NonNull String messageId,
                               @NonNull String type,
                               @NonNull MessageCallback callback) {
        client.deleteReaction(messageId, type, callback);
    }

    public void sendAction(@NonNull String messageId,
                           @NonNull SendActionRequest request,
                           @NotNull MessageCallback callback) {
        client.sendAction(messageId, request, callback);
    }

    public void flagMessage(@NotNull String messageId,
                            @NotNull FlagCallback callback) {
        client.flagMessage(messageId, callback);
    }

    public void unFlagMessage(@NotNull String messageId,
                              @NotNull FlagCallback callback) {
        client.unFlagMessage(messageId, callback);
    }

    /**
     * bans a user from this channel
     *
     * @param targetUserId the ID of the user to ban
     * @param reason       the reason the ban was created
     * @param timeout      the timeout in minutes until the ban is automatically expired
     * @param callback     the result callback
     */
    public void banUser(@NotNull String targetUserId, @Nullable String reason, @Nullable Integer timeout,
                        @NotNull CompletableCallback callback) {
        client.banUser(targetUserId, this, reason, timeout, callback);
    }

    /**
     * removes the ban for a user on this channel
     *
     * @param targetUserId the ID of the user to remove the ban
     * @param callback     the result callback
     */
    public void unBanUser(@NotNull String targetUserId, @NotNull CompletableCallback callback) {
        client.unBanUser(targetUserId, this, callback);
    }

    /**
     * adds members with given user IDs to this channel
     *
     * @param members  list of user IDs to add as members
     * @param callback the result callback
     */
    public void addMembers(@NotNull List<String> members, @NotNull ChannelCallback callback) {
        client.addMembers(this, members, callback);
    }

    /**
     * remove members with given user IDs from this channel
     *
     * @param members  list of user IDs to remove from the member list
     * @param callback the result callback
     */
    public void removeMembers(@NotNull List<String> members, @NotNull ChannelCallback callback) {
        client.removeMembers(this, members, callback);
    }

    /**
     * Accept an invite to this channel
     *
     * @param message  message object allowing you to show a system message in the Channel
     * @param callback the result callback
     */
    public void acceptInvite(@Nullable String message, @NotNull ChannelCallback callback) {
        client.acceptInvite(this, message, callback);
    }

    /**
     * Accept an invite to this channel
     *
     * @param callback the result callback
     */
    public void acceptInvite(@NotNull ChannelCallback callback) {
        client.acceptInvite(this, null, callback);
    }

    /**
     * Reject an invite to this channel
     *
     * @param callback the result callback
     */
    public void rejectInvite(@NotNull ChannelCallback callback) {
        client.rejectInvite(this, callback);
    }

    public void handleChannelUpdated(Channel channel) {
        extraData = channel.extraData;
        updatedAt = channel.updatedAt;
        getClient().getStorage().insertChannel(channel);
    }

    public void handleChannelDeleted(Channel channel) {
        deletedAt = channel.deletedAt;
        getClient().getStorage().deleteChannel(channel);
    }

    public void handleWatcherStart(Event event) {
        channelState.addWatcher(new Watcher(event.getUser(), event.getCreatedAt()));
        channelState.setWatcherCount(event.getWatcherCount().intValue());
    }

    public void handleWatcherStop(Event event) {
        channelState.removeWatcher(new Watcher(event.getUser(), event.getCreatedAt()));
        channelState.setWatcherCount(event.getWatcherCount().intValue());
    }

    public void handleNewMessage(Event event) {
        Message message = event.getMessage();
        Message.setStartDay(Arrays.asList(message), channelState.getLastMessage());
        if (!message.getType().equals(ModelType.message_reply) && TextUtils.isEmpty(message.getParentId())) {
            channelState.addMessageSorted(message);
        }
        if (getLastMessageDate() != null && getLastMessageDate().before(message.getCreatedAt())) {
            setLastMessageDate(message.getCreatedAt());
        }
        getClient().getStorage().insertMessageForChannel(this, message);
    }

    public void handleMessageUpdatedOrDeleted(Event event) {
        Message message = event.getMessage();
        for (int i = 0; i < channelState.getMessages().size(); i++) {
            if (message.getId().equals(channelState.getMessages().get(i).getId())) {
                channelState.getMessages().set(i, message);
                // Check updatedMessage is Last or not
                if (i == channelState.getMessages().size() - 1)
                    channelState.setLastMessage(message);

                getClient().getStorage().insertMessageForChannel(this, message);
                break;
            }
        }
        if (event.getWatcherCount() != null) {
            channelState.setWatcherCount(event.getWatcherCount().intValue());
        }
    }

    public void handleReadEvent(Event event) {
        channelState.setReadDateOfChannelLastMessage(event.getUser(), event.getCreatedAt());
    }

    public void handleMemberAdded(@NotNull Member member) {
        channelState.addOrUpdateMember(member);
    }

    public void handleMemberUpdated(@NotNull Member member) {
        channelState.addOrUpdateMember(member);
    }

    public void handelMemberRemoved(@NotNull User user) {
        channelState.removeMemberById(user.getId());
    }

    /**
     * Sends a start typing event if it's been more than 3 seconds since the last start typing event was sent
     */
    public synchronized void keystroke(EventCallback callback) {
        Date now = new Date();
        lastKeystrokeAt = now;
        if (lastStartTypingEvent == null || (now.getTime() - lastStartTypingEvent.getTime() > 3000)) {
            lastStartTypingEvent = now;
            this.sendEvent(EventType.TYPING_START, callback);
        }
    }

    /**
     * Sends the stop typing event
     */
    public synchronized void stopTyping(EventCallback callback) {
        lastStartTypingEvent = null;
        this.sendEvent(EventType.TYPING_STOP, callback);
    }

    /**
     * sendEvent - Send an event on this channel
     *
     * @param eventType event for example {type: 'message.read'}
     * @return The Server Response
     */
    // TODO: check this function
    public void sendEvent(@NotNull EventType eventType,
                          @NotNull EventCallback callback) {
        final Map<String, Object> event = new HashMap<>();
        event.put("type", eventType.label);
        SendEventRequest request = new SendEventRequest(event);
        client.sendEvent(this, request, callback);
    }

    /**
     * markRead - marks the channel read for current user, only works if the `read_events` setting is enabled
     *
     * @param callback the result callback
     */
    public void markRead(@NotNull EventCallback callback) {
        client.markRead(this, new MarkReadRequest(null), callback);
    }

    /**
     * hides the channel from queryChannels for the user until a message is added
     *
     * @param request the request options for the hide channel request
     * @param callback the result callback
     */
    public void hide(HideChannelRequest request, @NotNull CompletableCallback callback) {
        client.hideChannel(this, request, callback);
    }

    /**
     * hides the channel from queryChannels for the user until a message is added
     *
     * @param callback the result callback
     */
    public void show(@NotNull CompletableCallback callback) {
        client.showChannel(this, callback);
    }

    /**
     * edit the channel's custom properties.
     *
     * @param updateMessage message allowing you to show a system message in the Channel that something changed
     * @param callback      the result callback
     */
    public void update(@Nullable Message updateMessage, @NotNull ChannelCallback callback) {
        client.updateChannel(this, updateMessage, callback);
    }

    /**
     * edit the channel's custom properties.
     *
     * @param callback the result callback
     */
    public void update(@NotNull ChannelCallback callback) {
        client.updateChannel(this, null, callback);
    }

    /**
     * removes the channel. Messages are permanently removed.
     *
     * @param callback the result callback
     */
    public void delete(@NotNull ChannelCallback callback) {
        client.deleteChannel(this, callback);
    }

    /**
     * stops watching the channel for events.
     *
     * @param callback the result callback
     */
    public void stopWatching(@NotNull CompletableCallback callback) {
        client.stopWatchingChannel(this, callback);
    }

    public ChannelState getLastState() {
        return lastState;
    }

    public void setLastState(ChannelState lastState) {
        this.lastState = lastState;
    }

    public String getCreatedByUserID() {
        return createdByUserID;
    }

    public void setCreatedByUserID(String createdByUserID) {
        this.createdByUserID = createdByUserID;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + '\'' + "," +
                "name='" + getName() + '\'' +
                '}';
    }
}