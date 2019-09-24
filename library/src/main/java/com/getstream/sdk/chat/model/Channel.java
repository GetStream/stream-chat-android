package com.getstream.sdk.chat.model;

import android.text.TextUtils;
import android.util.Log;

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

import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.enums.MessageStatus;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ChatChannelEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.FlagCallback;
import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.SendFileCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.FileSendResponse;
import com.getstream.sdk.chat.rest.response.FlagResponse;
import com.getstream.sdk.chat.rest.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.storage.converter.DateConverter;
import com.getstream.sdk.chat.storage.converter.ExtraDataConverter;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Utils;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A channel
 */
@Entity(tableName = "stream_channel", foreignKeys = @ForeignKey(entity = User.class,
        parentColumns = "id",
        childColumns = "created_by_user_id"), indices = {
        @Index(value = {"created_by_user_id"})})
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
public class Channel {
    private static final String TAG = Channel.class.getSimpleName();

    @PrimaryKey
    @NonNull
    @SerializedName("cid")
    private String cid;

    @NonNull
    @SerializedName("id")
    private String id;
    @NonNull
    @SerializedName("type")
    private String type;
    @SerializedName("last_message_at")
    @TypeConverters(DateConverter.class)
    private Date lastMessageDate;

    public @Sync.Status Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(@Sync.Status Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    private Integer syncStatus;


    @Embedded(prefix = "state_")
    private ChannelState lastState;
    @SerializedName("created_at")
    @TypeConverters(DateConverter.class)
    private Date createdAt;
    @SerializedName("updated_at")
    @TypeConverters(DateConverter.class)
    private Date updatedAt;
    @SerializedName("created_by")
    @Ignore
    private User createdByUser;
    @ColumnInfo(name = "created_by_user_id")
    private String createdByUserID;
    @SerializedName("frozen")
    private boolean frozen;
    @SerializedName("config")
    @Embedded(prefix = "config_")
    private Config config;
    @SerializedName("name")
    private String name;
    @SerializedName("image")
    private String image;
    @TypeConverters(ExtraDataConverter.class)
    private HashMap<String, Object> extraData;
    @Ignore
    private Map<String, String> reactionTypes;
    @Ignore
    private List<ChatChannelEventHandler> eventSubscribers;
    @Ignore
    private Map<Number, ChatChannelEventHandler> eventSubscribersBy;
    @Ignore
    private int subscribersSeq;
    @Ignore
    private Client client;
    @Ignore
    private ChannelState channelState;

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
        this.cid = String.format("%s:%s", type, id);
        this.client = client;
        this.setSyncStatus(Sync.SYNCED);

        if (extraData == null) {
            this.extraData = new HashMap<>();
        } else {
            this.extraData = new HashMap<>(extraData);
        }

        // since name and image are very common fields, we are going to promote them as
        Object image = this.extraData.remove("image");
        if (image != null) {
            this.image = image.toString();
        }

        Object name = this.extraData.remove("name");
        if (name != null) {
            this.name = name.toString();
        }
        this.extraData.remove("id");
        eventSubscribers = new ArrayList<>();
        eventSubscribersBy = new HashMap<>();
        channelState = new ChannelState(this);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date d) {
        this.createdAt = d;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setEventSubscribers(List<ChatChannelEventHandler> eventSubscribers) {
        this.eventSubscribers = eventSubscribers;
    }

    public void setEventSubscribersBy(Map<Number, ChatChannelEventHandler> eventSubscribersBy) {
        this.eventSubscribersBy = eventSubscribersBy;
    }

    public void setSubscribersSeq(int subscribersSeq) {
        this.subscribersSeq = subscribersSeq;
    }

    // region Getter & Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Channel copy() {
        Channel clone = new Channel(client, type, id);
        if (lastMessageDate != null) {
            clone.lastMessageDate = new Date(lastMessageDate.getTime());
        }
        if (createdAt != null) {
            clone.createdAt = new Date(createdAt.getTime());
        }
        if (updatedAt != null) {
            clone.updatedAt = new Date(updatedAt.getTime());
        }
        if (channelState != null) {
            clone.channelState = channelState.copy();
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        // we compare based on the CID
        Channel otherChannel = (Channel) obj;
        return TextUtils.equals(this.getCid(), otherChannel.getCid());
    }

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

    public final synchronized int addEventHandler(ChatChannelEventHandler handler) {
        int id = ++subscribersSeq;
        if (eventSubscribers == null) {
            eventSubscribers = new ArrayList<>();
            eventSubscribersBy = new HashMap<>();
        }
        eventSubscribers.add(handler);
        eventSubscribersBy.put(id, handler);
        return id;
    }

    public final synchronized void removeEventHandler(Number handlerId) {
        ChatChannelEventHandler handler = eventSubscribersBy.remove(handlerId);
        eventSubscribers.remove(handler);
    }

    public final synchronized void handleChannelEvent(Event event) {
        if (eventSubscribers == null || eventSubscribers.isEmpty()) return;
        for (ChatChannelEventHandler handler : eventSubscribers) {
            handler.dispatchEvent(event);
        }
    }

    public void mergeWithState(ChannelState state) {
        name = state.getChannel().name;
        image = state.getChannel().image;
        channelState.init(state);
        config = state.getChannel().config;
        lastMessageDate = state.getChannel().lastMessageDate;
        extraData = state.getChannel().extraData;
        createdAt = state.getChannel().createdAt;
        updatedAt = state.getChannel().updatedAt;
    }

    /**
     * query - Query the API, get messages, members or other channel fields
     *
     */
    public void query(@NonNull ChannelQueryRequest request, QueryChannelCallback callback) {
        Channel channel = this;
        client.onSetUserCompleted(
                new ClientConnectionCallback() {
                    @Override
                    public void onSuccess(User user) {
                        client.getApiService().queryChannel(channel.type, channel.id, client.getApiKey(), client.getUserId(), client.getClientID(), request).enqueue(new Callback<ChannelState>() {
                            @Override
                            public void onResponse(Call<ChannelState> call, Response<ChannelState> response) {
                                Log.i(TAG, "channel query: incoming watchers " + response.body().getWatchers().size());
                                mergeWithState(response.body());

                                if (channel.config == null)
                                    channel.config = response.body().getChannel().getConfig();
                                if (channel.channelState == null)
                                    channel.channelState = response.body();

                                client.addChannelConfig(type, channel.config);
                                client.addToActiveChannels(channel);
                                Log.i(TAG, "channel query: merged watchers " + channel.getChannelState().getWatchers().size());
                                // offline storage

                                getClient().storage().insertMessagesForChannel(channel, response.body().getMessages());

                                callback.onSuccess(response.body());
                            }

                            @Override
                            public void onFailure(Call<ChannelState> call, Throwable t) {
                                callback.onError(t.getLocalizedMessage(), -1);
                            }
                        });
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        callback.onError(errMsg, errCode);
                    }
                }
        );
    }

    /**
     * getReplies - List the message replies for a parent message
     *
     * @return {object} Returns a getReplies response
     */
    public void getReplies(@NonNull String parentId, int limit, String firstMessageId, final GetRepliesCallback callback) {
        client.getReplies(parentId, limit, firstMessageId, new GetRepliesCallback() {
            @Override
            public void onSuccess(GetRepliesResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    public ChannelState getChannelState() {
        return channelState;
    }

    public void setChannelState(ChannelState channelState) {
        this.channelState = channelState;
    }

    // TODO: move this somewhere else
    public String getInitials() {
        String name = this.name;
        if (name == null) {
            this.name = "";
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

    // region Message
    public void sendMessage(Message message,
                            MessageCallback callback) {
        List<String> mentionedUserIDs = Utils.getMentionedUserIDs(channelState, message.getText());
        SendMessageRequest request;
        if (message.getId() != null) {
            request = new SendMessageRequest(message.getId(), message.getText(), message.getAttachments(), message.getParentId(), false, mentionedUserIDs);
        } else {
            request = new SendMessageRequest(message.getText(), message.getAttachments(), message.getParentId(), false, mentionedUserIDs);
        }

        client.sendMessage(this, request, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                if (callback != null)
                    callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                if (callback != null)
                    callback.onError(errMsg, errCode);
            }
        });
    }

    public void updateMessage(@NonNull Message message,
                              MessageCallback callback) {
        List<String> mentionedUserIDs = Utils.getMentionedUserIDs(channelState, message.getText());

        UpdateMessageRequest request = new UpdateMessageRequest(message, mentionedUserIDs);

        client.updateMessage(message.getId(), request, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    public void deleteMessage(@NonNull Message message,
                              MessageCallback callback) {
        client.deleteMessage(message.getId(), new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    public void sendImage(Attachment attachment,
                         SendFileCallback fileCallback) {
        File file = new File(attachment.config.getFilePath());
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
        client.sendImage(this, part, new SendFileCallback() {
            @Override
            public void onSuccess(FileSendResponse response) {
                fileCallback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                fileCallback.onError(errMsg, errCode);
            }
        });
    }

    public void sendFile(Attachment attachment,
                         SendFileCallback fileCallback) {
        File file = new File(attachment.config.getFilePath());

        RequestBody fileReqBody = RequestBody.create(MediaType.parse(attachment.getMime_type()), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
        client.sendFile(this, part, new SendFileCallback() {
            @Override
            public void onSuccess(FileSendResponse response) {
                fileCallback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                fileCallback.onError(errMsg, errCode);
            }
        });
    }
    // endregion

    /**
     * sendReaction - Send a reaction about a message
     *
     * @param {string} messageID the message id
     * @param {object} reaction the reaction object for instance {type: 'love'}
     * @param {string} user_id the id of the user (used only for server side request) default null
     * @return {object} The Server Response
     */
    public void sendReaction(String mesageId, String type, MessageCallback callback) {
        ReactionRequest request = new ReactionRequest(type);
        client.sendReaction(mesageId, request, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    /**
     * deleteReaction - Delete a reaction by user and type
     *
     * @param {string} messageID the id of the message from which te remove the reaction
     * @param {string} reactionType the type of reaction that should be removed
     * @param {string} user_id the id of the user (used only for server side request) default null
     * @return {object} The Server Response
     */
    public void deleteReaction(String mesageId, String type, MessageCallback callback) {
        client.deleteReaction(mesageId, type, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    public void sendAction(@NonNull String messageId,
                           @NonNull SendActionRequest request,
                           MessageCallback callback) {

        client.sendAction(messageId, request, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    public void flagMessage(String messageId, FlagCallback callback) {
        client.flagMessage(messageId, new FlagCallback() {
            @Override
            public void onSuccess(FlagResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    public void unFlagMessage(String messageId, FlagCallback callback) {
        client.unFlagMessage(messageId, new FlagCallback() {
            @Override
            public void onSuccess(FlagResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    public void handleChannelUpdated(Channel channel, Event event) {
        name = channel.name;
        image = channel.image;
        extraData = channel.extraData;
        getClient().storage().insertChannel(channel);
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
        message.setStatus(MessageStatus.RECEIVED);
        if (!message.getType().equals(ModelType.message_reply)){
            channelState.addMessageSorted(message);
        }
        if (getLastMessageDate() != null && getLastMessageDate().before(message.getCreatedAt())) {
            setLastMessageDate(message.getCreatedAt());
        }
        getClient().storage().insertMessageForChannel(this, message);
    }

    public void handleMessageUpdatedOrDeleted(Event event) {
        Message message = event.getMessage();
        for (int i = 0; i < channelState.getMessages().size(); i++) {
            if (message.getId().equals(channelState.getMessages().get(i).getId())) {
                if (event.getType().equals(EventType.MESSAGE_DELETED))
                    message.setText(Constant.MESSAGE_DELETED);
                channelState.getMessages().set(i, message);
                getClient().storage().insertMessageForChannel(this, message);
                break;
            }
        }
        if (event.getWatcherCount() != null) {
            channelState.setWatcherCount(event.getWatcherCount().intValue());
        }
    }

    public void handleReadEvent(Event event) {
        channelState.setReadDateOfChannelLastMessage(event.getUser(), event.getCreatedAt());
        channelState.getChannel().setLastMessageDate(event.getCreatedAt());
    }

    /**
     * sendEvent - Send an event on this channel
     *
     * @param eventType event for example {type: 'message.read'}
     * @return The Server Response
     */
    // TODO: check this function
    public void sendEvent(EventType eventType, final EventCallback callback) {
        final Map<String, Object> event = new HashMap<>();
        event.put("type", eventType.label);
        SendEventRequest request = new SendEventRequest(event);

        client.sendEvent(this, request, new EventCallback() {
            @Override
            public void onSuccess(EventResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    /**
     * markRead - marks the channel read for current user, only works if the `read_events` setting is enabled
     */
    public void markRead() {
        client.markRead(this, new MarkReadRequest(null), new EventCallback() {
            @Override
            public void onSuccess(EventResponse response) {
                Log.i(TAG, "mark read successful");
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "mark read failed with error " + errMsg);
            }
        });
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
}