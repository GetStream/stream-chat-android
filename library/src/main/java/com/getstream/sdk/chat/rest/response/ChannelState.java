package com.getstream.sdk.chat.rest.response;

import android.text.TextUtils;
import android.util.Log;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.Watcher;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChannelState{

    private static final String TAG = ChannelState.class.getSimpleName();

    @SerializedName("channel")
    private Channel channel;

    @SerializedName("messages")
    private List<Message> messages;

    @SerializedName("read")
    private List<ChannelUserRead> reads;

    @SerializedName("members")
    private List<Member> members;

    @SerializedName("watchers")
    private List<Watcher> watchers;

    @SerializedName("watcher_count")
    private int watcherCount;

    public ChannelState(Channel channel) {
        this.channel = channel;
        messages = new ArrayList<>();
        reads = new ArrayList<>();
        members = new ArrayList<>();
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    // endregion
    public static void sortUserReads(List<ChannelUserRead> reads) {
        Collections.sort(reads, (ChannelUserRead o1, ChannelUserRead o2) -> o1.getLastRead().compareTo(o2.getLastRead()));
    }

    public List<User> getOtherUsers() {
        List<User> users = new ArrayList<>();
        for (Member m : members) {
            if (!channel.getClient().fromCurrentUser(m)) {
                users.add(m.getUser());
            }
        }
        return users;
    }

    public String getOldestMessageId() {
        Message message = getOldestMessage();
        if (message == null) {
            return null;
        }
        return message.getId();
    }

    // last time the channel had another user online OR got a message from another user
    public Date getLastActive() {
        Message message = getLastMessageFromOtherUser();
        Date lastActive = new Date(0);
        List<User> users = this.getOtherUsers();
        for (User u: users) {
            if (u.getLastActive() != null && u.getLastActive().after(lastActive)) {
                lastActive = u.getLastActive();
            }
        }

        if (lastActive.after(message.getCreatedAt())) {
            return lastActive;
        }

        return message.getCreatedAt();
    }

    public Boolean anyOtherUsersOnline() {
        Boolean online = false;
        List<User> users = this.getOtherUsers();
        for (User u: users) {
            if (u.getOnline()) {
                online = true;
                break;
            }
        }
        return online;
    }

    public String getChannelNameOrMembers() {
        String channelName;

        Log.i(TAG, "Channel name is" + channel.getName() + channel.getCid());
        if (!TextUtils.isEmpty(channel.getName())) {
            channelName = channel.getName();
        } else {
            List<User> users = this.getOtherUsers();
            List<User> top3 = users.subList(0, Math.min(3, users.size()));
            List<String> usernames = new ArrayList<>();
            for (User u : top3) {
                usernames.add(u.getName());
            }

            channelName = TextUtils.join(", ", usernames);
            if (users.size() > 3) {
                channelName += "...";
            }
        }
        return channelName;
    }

    public Channel getChannel() {
        return channel;
    }

    public Message getOldestMessage() {
        if (messages == null || messages.size() == 0) {
            return null;
        }
        return messages.get(0);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<ChannelUserRead> getReads() {
        return reads;
    }

    public List<ChannelUserRead> getLastMessageReads() {
        Message lastMessage = this.getLastMessage();
        List<ChannelUserRead> readLastMessage = new ArrayList<>();
        if (reads == null || lastMessage == null) return readLastMessage;
        for (ChannelUserRead r : reads) {
            if (r.getLastRead().compareTo(lastMessage.getCreatedAt()) > -1) {
                readLastMessage.add(r);
            }
        }

        // sort the reads
        Collections.sort(readLastMessage, (ChannelUserRead o1, ChannelUserRead o2) -> o1.getLastRead().compareTo(o2.getLastRead()));
        return readLastMessage;
    }

    public List<Member> getMembers() {
        return members;
    }

    public Message getLastMessage() {
        Message lastMessage = null;
        List<Message> messages = getMessages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message.getDeletedAt() == null && message.getType().equals(ModelType.message_regular)) {
                lastMessage = message;
                break;
            }
        }
        Message.setStartDay(Arrays.asList(lastMessage), null);

        return lastMessage;
    }

    public Message getLastMessageFromOtherUser() {
        Message lastMessage = null;
        try {
            List<Message> messages = getMessages();
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message message = messages.get(i);
                if (message.getDeletedAt() == null && !channel.getClient().fromCurrentUser(message)) {
                    lastMessage = message;
                    break;
                }
            }
            Message.setStartDay(Arrays.asList(lastMessage), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastMessage;
    }

    public User getLastReader() {
        return null;
//        if (this.reads == null || this.reads.isEmpty()) return null;
//        User lastReadUser = null;
//        try {
//            if (!isSorted && this.reads != null) {
//                Global.sortUserReads(this.reads);
//                isSorted = true;
//            }
//            for (int i = reads.size() - 1; i >= 0; i--) {
//                ChannelUserRead channelUserRead = reads.get(i);
//                if (!channelUserRead.getUser().getId().equals(Global.client.user.getId())) {
//                    lastReadUser = channelUserRead.getUser();
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return lastReadUser;
    }

    private void addOrUpdateMessage(Message newMessage) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getId().equals(newMessage.getId())) {
                messages.set(i, newMessage);
                return;
            }
            if (messages.get(i).getCreatedAt().before(newMessage.getCreatedAt())) {
                messages.add(newMessage);
            }
        }
    }

    public void addMessageSorted(Message message){
        List<Message> diff = new ArrayList<>();
        diff.add(message);
        addMessagesSorted(diff);
    }

    private void addMessagesSorted(List<Message> messages){
        int initialSize = messages.size();
        Log.w(TAG, "initial size" + initialSize);
        Log.w(TAG, "incoming size" + messages.size());

        for (Message m : messages) {
            if(m.getParentId() == null) {
                addOrUpdateMessage(m);
            }
        }
    }

    public void init(ChannelState incoming) {
        //TODO: do an actual init instead of a replacement
        reads = incoming.reads;
        members = incoming.members;

        if (incoming.members != null && incoming.members.size() > 0) {
            for (Member m : incoming.members) {
                //TODO: update user references to client
            }
        }

        if (incoming.messages != null) {
            addMessagesSorted(incoming.messages);
        }
        watcherCount = incoming.watcherCount;

        watchers = incoming.watchers;
        if (incoming.watchers != null && incoming.watchers.size() != 0) {
            // TODO: init watchers
        }

        reads = incoming.reads;
        if (incoming.reads != null && incoming.reads.size() != 0) {
            // TODO: init read state
        }

        members = incoming.members;
        if (incoming.members != null && incoming.members.size() != 0) {
            // TODO: init read state
        }

    }

    public int getCurrentUserUnreadMessageCount() {
        Client client = this.getChannel().getClient();
        String userID = client.getUserId();
        return this.getUnreadMessageCount(userID);
    }

    public int getUnreadMessageCount(String userId) {
        int unreadMessageCount = 0;
        if (this.reads == null || this.reads.isEmpty()) return unreadMessageCount;

        Date lastReadDate = getReadDateOfChannelLastMessage(userId);
        if (lastReadDate == null) return unreadMessageCount;
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (!message.getUser().getId().equals(userId)) continue;
            if (message.getDeletedAt() != null) continue;
            if (message.getCreatedAt().getTime() > lastReadDate.getTime())
                unreadMessageCount++;
        }
        return unreadMessageCount;
    }

    public Date getReadDateOfChannelLastMessage(String userId) {
        if (this.reads == null || this.reads.isEmpty()) return null;
        Date lastReadDate = null;
        sortUserReads(this.reads);

        try {
            for (int i = reads.size() - 1; i >= 0; i--) {
                ChannelUserRead channelUserRead = reads.get(i);
                if (channelUserRead.getUser().getId().equals(userId)) {
                    lastReadDate = channelUserRead.getLastRead();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lastReadDate;
    }

    public void setReadDateOfChannelLastMessage(User user, Date readDate) {
        if (this.reads == null || this.reads.isEmpty()) return;
        boolean isNotSet = true;
        for (ChannelUserRead userLastRead : this.reads) {
            try {
                User user_ = userLastRead.getUser();
                if (user_.getId().equals(user.getId())) {
                    userLastRead.setLastRead(readDate);
                    // Change Order
                    this.reads.remove(userLastRead);
                    this.reads.add(userLastRead);
                    isNotSet = false;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isNotSet) {
            ChannelUserRead channelUserRead = new ChannelUserRead();
            channelUserRead.setUser(user);
            channelUserRead.setLastRead(readDate);
            this.reads.add(channelUserRead);
        }
    }
}

