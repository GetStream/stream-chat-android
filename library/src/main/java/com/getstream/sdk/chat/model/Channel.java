package com.getstream.sdk.chat.model;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ChatChannelEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.SendFileCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.FileSendResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
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
public class Channel {
    private static final String TAG = Channel.class.getSimpleName();

    @SerializedName("id")
    private String id;
    @SerializedName("type")
    private String type;
    @SerializedName("last_message_at")
    private Date lastMessageDate;

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    @SerializedName("created_at")
    private Date createdAt;
    @SerializedName("updated_at")
    private Date updatedAt;
    @SerializedName("created_by")
    private User createdByUser;
    @SerializedName("frozen")
    private boolean frozen;
    @SerializedName("config")
    private Config config;
    @SerializedName("name")
    private String name;
    @SerializedName("image")
    private String image;

    private Date lastKeyStroke;
    private Date lastTypingEvent;
    boolean isTyping = false;
    private HashMap<String, Object> extraData;
    private Map<String, String>reactionTypes;

    // region Getter & Setter
    public String getId() {
        return id;
    }

    public String getCid() {
        return getType() + ":" + getId();
    }

    public String getType() {
        return type;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public User getCreatedByUser() {
        return createdByUser;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public Config getConfig() {
        return client.getChannelConfig(type);
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Channel copy() {
        Channel clone = new Channel(client, type, id);
        clone.name = name;
        clone.lastMessageDate = new Date(lastMessageDate.getTime());
        // TODO: add all fields here
        // TODO: copy
        clone.channelState = channelState;
        clone.channelState.setChannel(clone);
        return clone;
    }

    public Map<String, String> getReactionTypes() {
        if (reactionTypes == null){
            return new HashMap<String, String>() {
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

    private List<ChatChannelEventHandler> eventSubscribers;
    private Map<Number, ChatChannelEventHandler> eventSubscribersBy;
    private int subscribersSeq;

    public HashMap<String, Object> getExtraData() {
        return extraData;
    }
    // endregion

    // region Constructor

    public void setClient(Client client) {
        this.client = client;
    }

    private Client client;
    private ChannelState channelState;

    // this constructor is here for GSON to play fair
//    public Channel() {
//        this(null, "", "", new HashMap<>());
//    }

    /**
     * constructor - Create a channel
     *
     * @param client  the chat client
     * @param type  the type of channel
     * @param id  the id of the chat
     * @return Returns a new uninitialized channel
     */
    public Channel(Client client, String type, String id) {
        this(client, type, id, new HashMap<>());
    }

    public Client getClient() {
        return this.client;
    }

    /**
     * constructor - Create a channel
     *
     * @param client  the chat client
     * @param type  the type of channel
     * @param id  the id of the chat
     * @param extraData any additional custom params
     *
     * @return Returns a new uninitialized channel
     */
    public Channel(Client client, String type, String id, HashMap<String, Object> extraData) {
        this.type = type;
        this.id = id;
        this.client = client;

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

    // endregion

    public final synchronized int addEventHandler(ChatChannelEventHandler handler) {
        int id = ++subscribersSeq;
        if (eventSubscribers == null){
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

    public final synchronized void handleChannelEvent(Event event){
        if (eventSubscribers == null || eventSubscribers.isEmpty()) return;
        for (ChatChannelEventHandler handler: eventSubscribers) {
            handler.dispatchEvent(event);
        }
    }

    public void mergeWithState(ChannelState state){
        name = state.getChannel().name;
        image = state.getChannel().image;
        channelState.init(state);
        config = state.getChannel().config;
    }

    /**
     * query - Query the API, get messages, members or other channel fields
     *
     * @param {object} options The query options
     *
     * @return {object} Returns a query response
     */
    public void query(@NonNull ChannelQueryRequest request, QueryChannelCallback callback) {
        Channel channel = this;
        client.onSetUserCompleted(
                new ClientConnectionCallback() {
                    @Override
                    public void onSuccess(User user) {
                        client.getApiService().queryChannel(channel.id, client.getApiKey(), client.getUserId(), client.getClientID(), request).enqueue(new Callback<ChannelState>() {
                            @Override
                            public void onResponse(Call<ChannelState> call, Response<ChannelState> response) {
                                Log.i(TAG, "channel query: incoming watchers " + response.body().getWatchers().size());
                                mergeWithState(response.body());
                                client.addChannelConfig(type, channel.config);
                                client.addToActiveChannels(channel);
                                Log.i(TAG, "channel query: merged watchers " + channel.getChannelState().getWatchers().size());
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
     * query - Query the API, get messages, members or other channel fields
     *
     * @return {object} Returns a query response
     */
    public void query(QueryChannelCallback callback) {
        query(new ChannelQueryRequest().withData(this.extraData), callback);
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

        if (!StringUtility.isNullOrEmpty(firstName) && StringUtility.isNullOrEmpty(lastName))
            return firstName.substring(0, 1).toUpperCase();
        if (StringUtility.isNullOrEmpty(firstName) && !StringUtility.isNullOrEmpty(lastName))
            return lastName.substring(0, 1).toUpperCase();

        if (!StringUtility.isNullOrEmpty(firstName) && !StringUtility.isNullOrEmpty(lastName))
            return firstName.substring(0, 1).toUpperCase() + lastName.substring(0, 1).toUpperCase();
        return null;
    }

    // region Message
    public void sendMessage(@Nullable String text,
                            @Nullable List<Attachment> attachments,
                            @Nullable String parentId,
                            MessageCallback callback) {
        List<String> mentionedUserIDs = Global.getMentionedUserIDs(channelState, text);
        SendMessageRequest request = new SendMessageRequest(text, attachments, parentId, false, mentionedUserIDs);
        client.sendMessage(this.id, request, new MessageCallback() {
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
    // region Message
    public void sendMessage(Message message,
                            MessageCallback callback) {
        List<String> mentionedUserIDs = Global.getMentionedUserIDs(channelState, message.getText());
        SendMessageRequest request;
        if (message.getId() != null) {
            request = new SendMessageRequest(message.getId(), message.getText(), message.getAttachments(), message.getParentId(), false, mentionedUserIDs);
        } else {
            request = new SendMessageRequest(message.getText(), message.getAttachments(), message.getParentId(), false, mentionedUserIDs);
        }

        client.sendMessage(this.id, request, new MessageCallback() {
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

    public void updateMessage(String text,
                              @NonNull Message message,
                              @Nullable List<Attachment> attachments,
                              MessageCallback callback) {
        List<String> mentionedUserIDs = Global.getMentionedUserIDs(channelState, text);
        message.setText(text);
        UpdateMessageRequest request = new UpdateMessageRequest(message, attachments, mentionedUserIDs);

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

    public void sendFile(Attachment attachment, boolean isImage,
                          SendFileCallback fileCallback) {
        File file = new File(attachment.config.getFilePath());

        if (isImage) {
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
            client.sendImage(this.channelState.getChannel().getId(), part, new SendFileCallback() {
                @Override
                public void onSuccess(FileSendResponse response) {
                    fileCallback.onSuccess(response);
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    fileCallback.onError(errMsg, errCode);
                }
            });
        } else {
            RequestBody fileReqBody = RequestBody.create(MediaType.parse(attachment.getMime_type()), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
            client.sendFile(this.channelState.getChannel().getId(), part, new SendFileCallback() {
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
    }
    // endregion

    /**
     * sendReaction - Send a reaction about a message
     *
     * @param {string} messageID the message id
     * @param {object} reaction the reaction object for instance {type: 'love'}
     * @param {string} user_id the id of the user (used only for server side request) default null
     *
     * @return {object} The Server Response
     */
    public void sendReaction(String mesageId, String type, MessageCallback callback){
        ReactionRequest request = new ReactionRequest(type);
        client.sendReaction(mesageId, request, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg,errCode);
            }
        });
    }

    /**
     * deleteReaction - Delete a reaction by user and type
     *
     * @param {string} messageID the id of the message from which te remove the reaction
     * @param {string} reactionType the type of reaction that should be removed
     * @param {string} user_id the id of the user (used only for server side request) default null
     *
     * @return {object} The Server Response
     */
    public void deleteReaction(String mesageId, String type, MessageCallback callback){
        client.deleteReaction(mesageId, type, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg,errCode);
            }
        });
    }

    /**
     * keystroke - First of the typing.start and typing.stop events based on the users keystrokes.
     * Call this on every keystroke
     */
    public void keystroke(){
        if (!getConfig().istypingEvents()) return;
        Date now = new Date();
        long diff;
        if (this.lastKeyStroke == null)
            diff = 2001;
        else
            diff = now.getTime() - this.lastKeyStroke.getTime();


        this.lastKeyStroke = now;
        this.isTyping = true;
        // send a typing.start every 2 seconds
        if (diff > 2000) {
            this.lastTypingEvent = new Date();
            sendEvent(EventType.TYPING_START, new EventCallback() {
                @Override
                public void onSuccess(EventResponse response) {

                }

                @Override
                public void onError(String errMsg, int errCode) {

                }
            });
        }
    }

    public void handleChannelUpdated(Event event){
        name = event.getChannel().name;
        image = event.getChannel().image;
        extraData = event.getChannel().extraData;
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
        channelState.addMessageSorted(event.getMessage());
        if (getLastMessageDate().before(message.getCreatedAt())) {
            setLastMessageDate(message.getCreatedAt());
        }
    }

    public void handleMessageUpdatedOrDeleted(Event event) {
        Message message = event.getMessage();
        for (int i = 0; i < channelState.getMessages().size(); i++) {
            if (message.getId().equals(channelState.getMessages().get(i).getId())) {
                if (event.getType().equals(EventType.MESSAGE_DELETED))
                    message.setText(Constant.MESSAGE_DELETED);
                channelState.getMessages().set(i, message);
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
     * stopTyping - Sets last typing to null and sends the typing.stop event
     */
    public void stopTyping(){
        if (!getConfig().istypingEvents()) return;
        this.lastTypingEvent = null;
        this.isTyping = false;
        sendEvent(EventType.TYPING_STOP, new EventCallback() {
            @Override
            public void onSuccess(EventResponse response) {

            }

            @Override
            public void onError(String errMsg, int errCode) {

            }
        });
    }

    /**
     * Clean - Cleans the channel state and fires stop typing if needed
     */
    public void clean() {
        if (this.lastKeyStroke != null) {
            Date now = new Date();
            long diff = now.getTime() - this.lastKeyStroke.getTime();
            if (diff > 1000 && this.isTyping) {
                this.stopTyping();
            }
        }
    }

    /**
     * sendEvent - Send an event on this channel
     *
     * @param eventType event for example {type: 'message.read'}
     *
     * @return The Server Response
     */
    // TODO: check this function
    public void sendEvent(EventType eventType, final EventCallback callback){
        final Map<String, Object> event = new HashMap<>();
        event.put("type", eventType.label);
        SendEventRequest request = new SendEventRequest(event);

        client.sendEvent(this.id, request, new EventCallback() {
            @Override
            public void onSuccess(EventResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg,errCode);
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
}