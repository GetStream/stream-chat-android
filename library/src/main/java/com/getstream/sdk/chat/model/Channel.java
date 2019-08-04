package com.getstream.sdk.chat.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.getstream.sdk.chat.interfaces.MessageSendListener;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.StreamChat;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.FileSendResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.google.gson.annotations.SerializedName;

import java.io.File;
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
     * @param client the chat client
     * @param type  the type of channel
     * @param id  the id of the chat
     * @return Returns a new uninitialized channel
     */
    public Channel(StreamChat client, String type, String id) {
        this.client = client;
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
        this.client = client;
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
                            final MessageSendListener sendListener) {
        List<String> mentionedUserIDs = Global.getMentionedUserIDs(channelResponse, text);
        SendMessageRequest request = new SendMessageRequest(text, attachments, parentId, false, mentionedUserIDs);
        client.sendMessage(this.id, request, new StreamChat.SendMessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                sendListener.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                sendListener.onFailed(errMsg, errCode);
            }
        });
    }

    public void updateMessage(String text,
                              @NonNull Message message,
                              @Nullable List<Attachment> attachments,
                              final MessageSendListener sendListener) {
        if (message == null) return;
        List<String> mentionedUserIDs = Global.getMentionedUserIDs(channelResponse, text);
        message.setText(text);
        UpdateMessageRequest request = new UpdateMessageRequest(message, attachments, mentionedUserIDs);

        client.updateMessage(message.getId(), request, new StreamChat.SendMessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                sendListener.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                sendListener.onFailed(errMsg, errCode);
            }
        });
    }

    public void deleteMessage(@NonNull Message message,
                              final MessageSendListener sendListener) {
        client.deleteMessage(message.getId(), new StreamChat.SendMessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                sendListener.onSuccess(response);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                sendListener.onFailed(errMsg, errCode);
            }
        });
    }

    public void sendFile(Attachment attachment, boolean isImage,
                          StreamChat.SendFileCallback fileCallback) {
        File file = new File(attachment.config.getFilePath());

        if (isImage) {
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
            client.sendImage(this.channelResponse.getChannel().getId(), part, new StreamChat.SendFileCallback() {
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
            client.sendFile(this.channelResponse.getChannel().getId(), part, new StreamChat.SendFileCallback() {
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

    public void sendReaction(String mesageId, String type, StreamChat.SendMessageCallback callback){
        ReactionRequest request = new ReactionRequest(type);
        client.sendReaction(mesageId, request, new StreamChat.SendMessageCallback() {
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
    public void deleteReaction(String mesageId, String type, StreamChat.SendMessageCallback callback){
        client.deleteReaction(mesageId, type, new StreamChat.SendMessageCallback() {
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
}