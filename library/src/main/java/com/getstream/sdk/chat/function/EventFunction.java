package com.getstream.sdk.chat.function;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.channel.Channel;
import com.getstream.sdk.chat.model.channel.Event;
import com.getstream.sdk.chat.model.message.Message;
import com.getstream.sdk.chat.rest.apimodel.request.SendEventRequest;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.rest.apimodel.response.EventResponse;
import com.getstream.sdk.chat.rest.controller.RestController;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EventFunction {

    private final String TAG = EventFunction.class.getSimpleName();

    public interface EventHandler {
        void handleEvent(Event event);

        void handleReconnection(boolean disconnect);
    }

    private EventHandler eventHandler;

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    private Channel channel;

    public void setChannel(@Nullable Channel channel) {
        this.channel = channel;
    }

    // region Send
    public void sendEvent(@NonNull String type) {
        // Check Event Allow
        if (this.channel == null) return;
        if (this.channel.getConfig() == null) return;
        if (!isEventAllow(type)) return;

        final Map<String, Object> event = new HashMap<>();
        event.put("type", type);
        SendEventRequest request = new SendEventRequest(event);
        RestController.EventCallback callback = (EventResponse response) -> {
            Log.d(TAG, "Event Send!");
        };

        Global.mRestController.sendEvent(channel.getId(), request, callback, (String errMsg, int errCode) -> {
            Log.d(TAG, "Send Event Failed!");
        });
    }

    private boolean isEventAllow(String type) {

        switch (type) {
            case Event.health_check:
                break;
            case Event.message_new:
                break;
            case Event.message_updated:
                break;
            case Event.message_deleted:
                break;
            case Event.typing_start:
            case Event.typing_stop:
                return channel.getConfig().isTyping_events();
            case Event.message_read:
                return channel.getConfig().isRead_events();
            case Event.user_updated:
                break;
            case Event.user_presence_changed:
                break;
            case Event.user_watching_start:
                break;
            case Event.user_watching_stop:
                break;
            case Event.reaction_new:
                break;
            case Event.reaction_deleted:
                break;
            default:
                break;
        }
        return true;
    }
    // endregion

    // region Receive
    public void handleReceiveEvent(Event event) {
        if (event.getType().equals(Event.health_check)) return;
        String channelId = null;
        try {
            String[] array = event.getCid().split(":");
            channelId = array[1];
        } catch (Exception e) {
        }

        if (channelId == null) return;
        Log.d(TAG, "channelId : " + channelId);

        ChannelResponse channel_ = null;
        for (ChannelResponse channel1 : Global.channels) {
            if (channel1.getChannel().getId().equals(channelId)) {
                channel_ = channel1;
                break;
            }
        }
        if (channel_ == null) {
            Log.d(TAG, "No channel : ");
            return;
        }
        if (this.channel != null) {
            if (this.channel.getId().equals(channelId) && this.eventHandler != null) {
                this.eventHandler.handleEvent(event);
                return;
            }
        }
        handlerReceiveEvent(channel_, event);
    }

    public void handleReconnect(boolean disconnect) {
        if (this.eventHandler != null)
            this.eventHandler.handleReconnection(disconnect);
    }

    private void handlerReceiveEvent(ChannelResponse channelResponse, Event event) {

        switch (event.getType()) {
            case Event.health_check:
                break;
            case Event.message_new:
            case Event.message_updated:
            case Event.message_deleted:
                handleMessageEvent(channelResponse, event);
                break;
            case Event.typing_start:
                break;
            case Event.typing_stop:
                break;
            case Event.message_read:
                readMessage(channelResponse, event);
                break;
            case Event.user_updated:
                break;
            case Event.user_presence_changed:
                break;
            case Event.user_watching_start:
                break;
            case Event.user_watching_stop:
                break;
            case Event.reaction_new:
            case Event.reaction_deleted:
                handleReactionEvent(channelResponse, event);
                break;
            case Event.notification_invited:
            case Event.notification_invite_accepted:
                handleInvite(channelResponse, event);
                break;
            case Event.channel_updated:
            case Event.channel_deleted:
                handleChannelEvent(channelResponse, event);
            default:
                break;
        }
    }

    public void handleReactionEvent(ChannelResponse channelResponse, Event event) {
        Message message = event.getMessage();
        if (message == null) return;

        if (!message.getType().equals(ModelType.message_regular)) return;
        updateMessage(channelResponse, message);
    }
    // endregion

    // region Handle Message Event
    public void handleMessageEvent(ChannelResponse channelResponse, Event event) {
        Message message = event.getMessage();
        if (message == null) return;

        if (!message.getType().equals(ModelType.message_regular)) return;

        switch (event.getType()) {
            case Event.message_new:
                newMessage(channelResponse, message);
                break;
            case Event.message_updated:
            case Event.message_deleted:
                for (int i = 0; i < channelResponse.getMessages().size(); i++) {
                    if (message.getId().equals(channelResponse.getMessages().get(i).getId())) {
                        // Deleted Message
                        if (event.getType().equals(Event.message_deleted))
                            message.setText(Constant.MESSAGE_DELETED);

                        channelResponse.getMessages().set(i, message);
                        break;
                    }
                }
                break;
            default:
                break;
        }
    }


    public void newMessage(ChannelResponse channelResponse, Message message) {
        Log.d(TAG, "new Message");
        Global.setStartDay(Arrays.asList(message), channelResponse.getLastMessage());
        channelResponse.getMessages().add(message);
        Global.channels.remove(channelResponse);
        Global.channels.add(0, channelResponse);
    }

    public void updateMessage(ChannelResponse channelResponse, Message message) {
        for (int i = 0; i < channelResponse.getMessages().size(); i++) {
            if (message.getId().equals(channelResponse.getMessages().get(i).getId())) {
                channelResponse.getMessages().set(i, message);
                break;
            }
        }
    }

    public void readMessage(ChannelResponse channelResponse, Event event) {
        channelResponse.setReadDateOfChannelLastMessage(event.getUser(), event.getCreated_at());
        channelResponse.getChannel().setLastMessageDate(event.getCreated_at());
    }
    // endregion

    // region Handle Invite
    public void handleInvite(ChannelResponse channelResponse, Event event) {

    }
    // endregion

    // region Handle Channel Event
    public void handleChannelEvent(ChannelResponse channelResponse, Event event) {
        switch (event.getType()) {
            case Event.channel_deleted:
                Global.deleteChannelResponse(channelResponse);
                break;
            case Event.channel_updated:
                break;
            default:
                break;
        }
    }

    // endregion
}
