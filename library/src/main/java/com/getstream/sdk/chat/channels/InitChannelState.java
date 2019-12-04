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
            currentState.addMessagesSorted(incoming.messages);
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
}
