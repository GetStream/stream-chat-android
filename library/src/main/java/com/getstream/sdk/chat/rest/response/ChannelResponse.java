package com.getstream.sdk.chat.rest.response;

import android.text.TextUtils;

import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.Global;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class ChannelResponse {

    private final String TAG = ChannelResponse.class.getSimpleName();

    @SerializedName("channel")
    private Channel channel;

    @SerializedName("messages")
    private List<Message> messages;

    @SerializedName("read")
    private List<ChannelUserRead> reads;

    @SerializedName("members")
    private List<Member> members;

    private boolean isSorted = false;

    public Channel getChannel() {
        return channel;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<ChannelUserRead> getReads() {
        return reads;
    }

    public List<Member> getMembers() {
        return members;
    }

    public Message getLastMessage() {
        Message lastMessage = null;
        try {
            List<Message> messages = getMessages();
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message message = messages.get(i);
                if (TextUtils.isEmpty(message.getDeleted_at()) && message.getType().equals(ModelType.message_regular)) {
                    lastMessage = message;
                    break;
                }
            }
            Global.setStartDay(Arrays.asList(lastMessage), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastMessage;
    }

    public Message getOpponentLastMessage() {
        Message lastMessage = null;
        try {
            List<Message> messages = getMessages();
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message message = messages.get(i);
                if (TextUtils.isEmpty(message.getDeleted_at()) && !message.getUser().isMe()) {
                    lastMessage = message;
                    break;
                }
            }
            Global.setStartDay(Arrays.asList(lastMessage), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastMessage;
    }

    public User getLastReadUser() {
        if (this.reads == null || this.reads.isEmpty()) return null;
        User lastReadUser = null;
        try {
            if (!isSorted && this.reads != null) {
                Global.sortUserReads(this.reads);
                isSorted = true;
            }
            for (int i = reads.size() - 1; i >= 0; i--) {
                ChannelUserRead channelUserRead = reads.get(i);
                if (!channelUserRead.getUser().getId().equals(Global.client.user.getId())) {
                    lastReadUser = channelUserRead.getUser();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastReadUser;
    }

    public int getUnreadMessageCount() {
        int unreadMessageCount = 0;
        if (this.reads == null || this.reads.isEmpty()) return unreadMessageCount;

        String lastReadDate = getReadDateOfChannelLastMessage(true);
        if (TextUtils.isEmpty(lastReadDate)) return unreadMessageCount;
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (!message.isIncoming()) continue;
            if (!TextUtils.isEmpty(message.getDeleted_at())) continue;
            if (!Global.readMessage(lastReadDate, message.getCreated_at()))
                unreadMessageCount++;
        }
        return unreadMessageCount;
    }

    public String getReadDateOfChannelLastMessage(boolean isMyRead) {
        if (this.reads == null || this.reads.isEmpty()) return null;
        String lastReadDate = null;
        if (!isSorted) {
            Global.sortUserReads(this.reads);
            isSorted = true;
        }

        try {
            for (int i = reads.size() - 1; i >= 0; i--) {
                ChannelUserRead channelUserRead = reads.get(i);
                if (isMyRead) {
                    if (channelUserRead.getUser().getId().equals(Global.client.user.getId())) {
                        lastReadDate = channelUserRead.getLast_read();
                        break;
                    }
                } else {
                    if (!channelUserRead.getUser().getId().equals(Global.client.user.getId())) {
                        lastReadDate = channelUserRead.getLast_read();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lastReadDate;
    }

    public void setReadDateOfChannelLastMessage(User user, String readDate) {
        if (this.reads == null || this.reads.isEmpty()) return;
        boolean isSet = false;
        for (ChannelUserRead userLastRead : this.reads) {
            try {
                User user_ = userLastRead.getUser();
                if (user_.getId().equals(user.getId())) {

                    userLastRead.setLast_read(readDate);
                    // Change Order
                    this.reads.remove(userLastRead);
                    this.reads.add(userLastRead);
                    isSet = true;
                    break;
                }
            } catch (Exception e) {
            }
        }
        if (!isSet) {
            ChannelUserRead channelUserRead = new ChannelUserRead();
            channelUserRead.setUser(user);
            channelUserRead.setLast_read(readDate);
            this.reads.add(channelUserRead);
        }
    }
}

