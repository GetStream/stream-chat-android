package com.getstream.sdk.chat.rest.controller;

import android.support.annotation.NonNull;
import android.util.Log;

import com.getstream.sdk.chat.rest.apimodel.request.AddDeviceRequest;
import com.getstream.sdk.chat.rest.apimodel.request.ChannelDetailRequest;
import com.getstream.sdk.chat.rest.apimodel.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.apimodel.request.PaginationRequest;
import com.getstream.sdk.chat.rest.apimodel.request.ReactionRequest;
import com.getstream.sdk.chat.rest.apimodel.request.SendActionRequest;
import com.getstream.sdk.chat.rest.apimodel.request.SendEventRequest;
import com.getstream.sdk.chat.rest.apimodel.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.apimodel.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.apimodel.response.AddDevicesResponse;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.rest.apimodel.response.EventResponse;
import com.getstream.sdk.chat.rest.apimodel.response.FileSendResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetChannelsResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetDevicesResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetUsersResponse;
import com.getstream.sdk.chat.rest.apimodel.response.MessageResponse;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.Utils;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestController {

    private static final String TAG = RestController.class.getSimpleName();

    APIService mService;

    public RestController() {
        mService = RetrofitClient.getAuthrizedClient().create(APIService.class);
        Log.d(TAG, mService.toString());
    }

    public void getChannels(@NonNull JSONObject payload, final GetChannelsCallback callback, final ErrCallback errCallback) {

        mService.getChannels(Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), payload).enqueue(new Callback<GetChannelsResponse>() {
            @Override
            public void onResponse(Call<GetChannelsResponse> call, Response<GetChannelsResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<GetChannelsResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void channelDetailWithID(@NonNull String channelId, @NonNull ChannelDetailRequest channelDetailRequest, final ChannelDetailCallback channelDetailCallback, final ErrCallback errCallback) {

        mService.chatDetail(channelId, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), channelDetailRequest).enqueue(new Callback<ChannelResponse>() {
            @Override
            public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                if (response.isSuccessful()) {
                    channelDetailCallback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<ChannelResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void pagination(@NonNull String channelId, @NonNull PaginationRequest request, final ChannelDetailCallback channelDetailCallback, final ErrCallback errCallback) {

        mService.pagination(channelId, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), request).enqueue(new Callback<ChannelResponse>() {
            @Override
            public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                if (response.isSuccessful()) {
                    channelDetailCallback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<ChannelResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }


    // region Message

    /**
     * sendMessage - Send a message to this channel
     *
     * @param {object} message The Message object
     * @return {object} The Server Response
     */
    public void sendMessage(@NonNull String channelId, @NonNull SendMessageRequest sendMessageRequest, final SendMessageCallback sendMessageCallback, final ErrCallback errCallback) {

        mService.sendMessage(channelId, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), sendMessageRequest).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    sendMessageCallback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    /**
     * updateMessage - Update the given message
     *
     * @param {object} message object, id needs to be specified
     * @return {object} Response that includes the message
     */
    public void updateMessage(@NonNull String messageId, @NonNull UpdateMessageRequest request, final SendMessageCallback sendMessageCallback, final ErrCallback errCallback) {

        mService.updateMessage(messageId, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    sendMessageCallback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    /**
     * deleteMessage - Delete the given message
     *
     * @param {string} messageID the message id needs to be specified
     * @return {object} Response that includes the message
     */
    public void deleteMessage(@NonNull String messageId, final SendMessageCallback sendMessageCallback, final ErrCallback errCallback) {

        mService.deleteMessage(messageId, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID()).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    sendMessageCallback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    /**
     * markRead - Send the mark read event for this user, only works if the `read_events` setting is enabled
     *
     * @return {Promise} Description
     */
    public void readMark(@NonNull String channelId, MarkReadRequest readRequest, final EventCallback eventCallback, final ErrCallback errCallback) {

        mService.readMark(channelId, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), readRequest).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful()) {
                    eventCallback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }
    // endregion

    // region Thread

    /**
     * getReplies - List the message replies for a parent message
     *
     * @param {type} parent_id The message parent id, ie the top of the thread
     * @param {type} options   Pagination params, ie {limit:10, idlte: 10}
     * @return {type} A channelResponse with a list of messages
     */
    public void getReplies(@NonNull String parentId, final GetRepliesCallback callback, final ErrCallback errCallback) {

        mService.getReplies(parentId, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID()).enqueue(new Callback<GetRepliesResponse>() {
            @Override
            public void onResponse(Call<GetRepliesResponse> call, Response<GetRepliesResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<GetRepliesResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }
    // endregion

    // region Reaction

    /**
     * sendReaction - Send a reaction about a message
     *
     * @param {string} messageID the message id
     * @param {object} reaction the reaction object for instance {type: 'love'}
     * @param {string} user_id the id of the user (used only for server side request) default null
     * @return {object} The Server Response
     */
    public void sendReaction(@NonNull String messageId, @NonNull ReactionRequest reactionRequest, final SendMessageCallback sendMessageCallback, final ErrCallback errCallback) {

        mService.sendReaction(messageId, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), reactionRequest).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    sendMessageCallback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
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
    public void deleteReaction(@NonNull String messageId, @NonNull String reactionType, final SendMessageCallback sendMessageCallback, final ErrCallback errCallback) {

        mService.deleteReaction(messageId, reactionType, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID()).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    sendMessageCallback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // endregion

    // region Event

    /**
     * sendEvent - Send an event on this channel
     *
     * @param {object} event for example {type: 'message.read'}
     * @return {object} The Server Response
     */
    public void sendEvent(@NonNull String channelId, @NonNull SendEventRequest eventRequest, final EventCallback callback, final ErrCallback errCallback) {

        mService.sendEvent(channelId, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), eventRequest).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // endregion

    // region File
    public void sendImage(@NonNull String channelId, MultipartBody.Part part, final SendFileCallback callback, final ErrCallback errCallback) {

        mService.sendImage(channelId, part, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID()).enqueue(new Callback<FileSendResponse>() {
            @Override
            public void onResponse(Call<FileSendResponse> call, Response<FileSendResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Send File:" + response.body().toString());
                    callback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void sendFile(@NonNull String channelId, MultipartBody.Part part, final SendFileCallback callback, final ErrCallback errCallback) {

        mService.sendFile(channelId, part, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID()).enqueue(new Callback<FileSendResponse>() {
            @Override
            public void onResponse(Call<FileSendResponse> call, Response<FileSendResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Send File:" + response.body().toString());
                    callback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // endregion
    public void sendAction(@NonNull String messageId, SendActionRequest request, final SendMessageCallback callback, final ErrCallback errCallback) {

        mService.sendAction(messageId, Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Send File:" + response.body().toString());
                    callback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // region User
    public void getUsers(@NonNull JSONObject payload, final GetUsersCallback callback, final ErrCallback errCallback) {

        mService.getUsers(Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), payload).enqueue(new Callback<GetUsersResponse>() {
            @Override
            public void onResponse(Call<GetUsersResponse> call, Response<GetUsersResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<GetUsersResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }
    // endregion

    // region Device

    public void addDevice(final AddDeviceRequest request, final AddDeviceCallback callback, final ErrCallback errCallback) {

        mService.addDevices(Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), request).enqueue(new Callback<AddDevicesResponse>() {
            @Override
            public void onResponse(Call<AddDevicesResponse> call, Response<AddDevicesResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<AddDevicesResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void getDevices(@NonNull Map<String, String> payload, final GetDevicesCallback callback, final ErrCallback errCallback) {
        mService.getDevices(Global.streamChat.getApiKey(), Global.streamChat.getUser().getId(), Global.streamChat.getClientID(), payload).enqueue(new Callback<GetDevicesResponse>() {
            @Override
            public void onResponse(Call<GetDevicesResponse> call, Response<GetDevicesResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    handleError(response.errorBody(), response.code(), errCallback);
                }
            }

            @Override
            public void onFailure(Call<GetDevicesResponse> call, Throwable t) {
                errCallback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }


    // endregion

    private void handleError(ResponseBody errBody, int errCode, ErrCallback errCallback) {
        if (errBody == null) {
            errCallback.onError("No channelResponse from server", -1);
        }

        String err = "";
        try {
            JSONObject jsonObject = new JSONObject(Utils.readInputStream(errBody.byteStream()));
            err = jsonObject.getString("message");
        } catch (Exception exception) {
            exception.printStackTrace();
            err = "Error " + String.valueOf(errCode);
        }
        errCallback.onError(err, errCode);
    }


    // region Interface
    public interface GetChannelsCallback {
        void onSuccess(GetChannelsResponse response);
    }

    public interface GetUsersCallback {
        void onSuccess(GetUsersResponse response);
    }

    public interface AddDeviceCallback {
        void onSuccess(AddDevicesResponse response);
    }

    public interface GetDevicesCallback {
        void onSuccess(GetDevicesResponse response);
    }

    public interface ChannelDetailCallback {
        void onSuccess(ChannelResponse response);
    }

    public interface EventCallback {
        void onSuccess(EventResponse response);
    }

    public interface SendMessageCallback {
        void onSuccess(MessageResponse response);
    }

    public interface SendFileCallback {
        void onSuccess(FileSendResponse response);
    }

    public interface GetRepliesCallback {
        void onSuccess(GetRepliesResponse response);
    }

    public interface ErrCallback {
        void onError(String errMsg, int errCode); // errCode = -1 means that it's failed to connect API.
    }
    // endregion
}
