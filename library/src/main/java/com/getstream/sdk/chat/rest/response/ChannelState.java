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
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ChannelState {

    private final String TAG = ChannelState.class.getSimpleName();

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
        Collections.sort(reads, (ChannelUserRead o1, ChannelUserRead o2) -> o1.getLast_read().compareTo(o2.getLast_read()));
    }

    public List<User> getOtherUsers() {
        List<User> users = new ArrayList<User>();
        for (Member m : this.members) {
            // TODO: Tommaso
            String currentUserId = "";
            if (!m.getUser().getId().equals(currentUserId)) {
                users.add(m.getUser());
            }
        }
        return users;
    }

    public String getOldestMessageId(){
        Message message = this.getOldestMessage();
        if (message == null) {
            return null;
        }
        return message.getId();
    }

    public Date getLastActive() {
//        List<User> users = this.getOtherUsers();
//        for (User u: users) {
//            // TODO: Tommaso why is this a string?
//            u.getLast_active();
//        }
        return new Date();
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
            List<String> usernames = new ArrayList<String>();
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
        return messages.get(messages.size() - 1);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<ChannelUserRead> getReads() {
        return reads;
    }

    public List<ChannelUserRead> getLastMessageReads() {
        Message lastMessage = this.getLastMessage();
        List<ChannelUserRead> readLastMessage = new ArrayList<ChannelUserRead>();
        for (ChannelUserRead r : reads) {
            // TODO: fix me as soon as we have working date parsing
            //r.getLast_read() > lastMessage.getCreatedAt___OLD()
            if (true) {
                readLastMessage.add(r);
            }
        }

        // sort the reads
        Collections.sort(readLastMessage, (ChannelUserRead o1, ChannelUserRead o2) -> o1.getLast_read().compareTo(o2.getLast_read()));

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
                if (message.getDeletedAt() == null && !message.getUser().isMe()) {

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

    public static Comparator<Message> byDate = (Message a, Message b) -> a.getCreatedAt().compareTo(b.getCreatedAt());

    public void addOrUpdateMessage(Message newMessage){
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getId().equals(newMessage.getId())) {
                messages.set(i, newMessage);
                return;
            }
        }
        messages.add(newMessage);
        return;
    }

    public void addMessageSorted(Message message){
        List<Message> diff = new ArrayList<>();
        diff.add(message);
        addMessagesSorted(diff);
    }

    public void addMessagesSorted(List<Message> messages){
        int initialSize = messages.size();
        for (Message m : messages) {
            if(m.getParentId() == null) {
                addOrUpdateMessage(m);
            }
        }
        if (initialSize != messages.size()) {
            Collections.sort(messages, byDate);
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

        addMessagesSorted(incoming.messages);
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
                    lastReadDate = channelUserRead.getLast_read();
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
                    userLastRead.setLast_read(readDate);
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
            channelUserRead.setLast_read(readDate);
            this.reads.add(channelUserRead);
        }
    }
}

