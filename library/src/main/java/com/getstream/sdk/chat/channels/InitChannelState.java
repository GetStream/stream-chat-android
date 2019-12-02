package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Watcher;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InitChannelState {

    private final ComputeLastMessage computeLastMessage;

    public InitChannelState(ComputeLastMessage computeLastMessage) {

        this.computeLastMessage = computeLastMessage;
    }

    public void init(Channel channel, ChannelState incoming) {
        ChannelState currentState = channel.getChannelState();
        currentState.reads = incoming.reads;
        currentState.watcherCount = incoming.watcherCount;

        if (currentState.watcherCount > 1) {
            currentState.lastKnownActiveWatcher = new Date();
        }

        if (incoming.messages != null) {
            addMessagesSorted(currentState, incoming.messages);
            currentState.lastMessage = computeLastMessage.computeLastMessage(channel);
        }

        if (incoming.watchers != null) {
            for (Watcher watcher : incoming.watchers) {
                currentState.addWatcher(watcher);
            }
        }

        if (incoming.members != null) {
            currentState.members = new ArrayList<>(incoming.members);
        }
        // TODO: merge with incoming.reads
    }

    private void addMessagesSorted(ChannelState currentState, List<Message> messages) {
        for (Message m : messages) {
            if (m.getParentId() == null) {
                addOrUpdateMessage(currentState, m);
            }
        }
    }

    private void addOrUpdateMessage(ChannelState currentState, Message newMessage) {
        if (currentState.messages.size() > 0) {
            for (int i = currentState.messages.size() - 1; i >= 0; i--) {
                if (currentState.messages.get(i).getId().equals(newMessage.getId())) {
                    currentState.messages.set(i, newMessage);
                    return;
                }
                if (currentState.messages.get(i).getCreatedAt().before(newMessage.getCreatedAt())) {
                    currentState.messages.add(newMessage);
                    return;
                }
            }
        } else {
            currentState.messages.add(newMessage);
        }
    }
}
