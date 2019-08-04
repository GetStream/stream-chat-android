package com.getstream.sdk.chat.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.StreamChat;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.SendFileCallback;
import com.getstream.sdk.chat.rest.interfaces.SendMessageCallback;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.FileSendResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * A channel
 */
public class Channel {

    @SerializedName("id")
    private String id;
    @SerializedName("cid")
    private String cid;
    @SerializedName("type")
    private String type;
    @SerializedName("last_message_at")
    private String lastMessageDate;
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

    private Map<String, Object> extraData;

    // region Getter & Setter
    public String getId() {
        return id;
    }

    public String getCid() {
        return cid;
    }

    public String getType() {
        return type;
    }

    public String getLastMessageDate() {
        return lastMessageDate;
    }

    public User getCreatedByUser() {
        return createdByUser;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public Config getConfig() {
        return config;
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

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLastMessageDate(String lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }
    // endregion

    // region Constructor

    StreamChat client;
    ChannelResponse channelResponse;
    static final String TAG = "Channel";

    /**
     * constructor - Create a channel
     *
     * @param type  the type of channel
     * @param id  the id of the chat
     * @return Returns a new uninitialized channel
     */
    public Channel(String type, String id) {
        this.type = type;
        this.id = id;
        this.extraData = new HashMap<>();
    }

    /**
     * constructor - Create a channel
     *
     * @param type  the type of channel
     * @param id  the id of the chat
     * @param extraData any additional custom params
     *
     * @return Returns a new uninitialized channel
     */
    public Channel(String type, String id, HashMap<String, Object> extraData) {
        this.type = type;
        this.id = id;

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
    }

    // endregion

    public ChannelResponse getChannelResponse() {
        return channelResponse;
    }

    public void setChannelResponse(ChannelResponse channelResponse) {
        this.channelResponse = channelResponse;
    }

    public StreamChat getClient() {
        return client;
    }

    public void setClient(StreamChat client) {
        this.client = client;
    }

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
                            SendMessageCallback callback) {
        List<String> mentionedUserIDs = Global.getMentionedUserIDs(channelResponse, text);
        SendMessageRequest request = new SendMessageRequest(text, attachments, parentId, false, mentionedUserIDs);
        client.sendMessage(this.id, request, new SendMessageCallback() {
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
                              SendMessageCallback callback) {
        if (message == null) return;
        List<String> mentionedUserIDs = Global.getMentionedUserIDs(channelResponse, text);
        message.setText(text);
        UpdateMessageRequest request = new UpdateMessageRequest(message, attachments, mentionedUserIDs);

        client.updateMessage(message.getId(), request, new SendMessageCallback() {
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
                              SendMessageCallback callback) {
        client.deleteMessage(message.getId(), new SendMessageCallback() {
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
            client.sendImage(this.channelResponse.getChannel().getId(), part, new SendFileCallback() {
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
            client.sendFile(this.channelResponse.getChannel().getId(), part, new SendFileCallback() {
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
    public void sendReaction(String mesageId, String type, SendMessageCallback callback){
        ReactionRequest request = new ReactionRequest(type);
        client.sendReaction(mesageId, request, new SendMessageCallback() {
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
    public void deleteReaction(String mesageId, String type, SendMessageCallback callback){
        client.deleteReaction(mesageId, type, new SendMessageCallback() {
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
    Date lastKeyStroke;
    Date lastTypingEvent;
    boolean isTyping = false;

    /**
     * keystroke - First of the typing.start and typing.stop events based on the users keystrokes.
     * Call this on every keystroke
     */
    public void keystroke(){
        if (!getConfig().isTyping_events()) return;
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
            sendEvent(Event.typing_start, new EventCallback() {
                @Override
                public void onSuccess(EventResponse response) {

                }

                @Override
                public void onError(String errMsg, int errCode) {

                }
            });
        }
    }

    /**
     * stopTyping - Sets last typing to null and sends the typing.stop event
     */
    public void stopTyping(){
        if (!getConfig().isTyping_events()) return;
        this.lastTypingEvent = null;
        this.isTyping = false;
        sendEvent(Event.typing_stop, new EventCallback() {
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
    public void sendEvent(String eventType, final EventCallback callback){
        final Map<String, Object> event = new HashMap<>();
        event.put("type", eventType);
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
}