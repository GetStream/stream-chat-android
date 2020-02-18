package com.getstream.sdk.chat.utils;

//import com.getstream.sdk.chat.StreamChat;
//import com.getstream.sdk.chat.rest.Message;

import com.getstream.sdk.chat.model.ModelType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import androidx.annotation.Nullable;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Message;

import static com.getstream.sdk.chat.enums.Dates.TODAY;
import static com.getstream.sdk.chat.enums.Dates.YESTERDAY;

public class DataUtils {

    public static int getUnreadMessageCount(String userId, Channel channel) {
        int unreadMessageCount = 0;
        List<io.getstream.chat.android.client.models.ChannelUserRead> read = channel.read;
        if (read == null || read.isEmpty()) return unreadMessageCount;

        Date lastReadDate = getReadDateOfChannelLastMessage(userId, channel);
        if (lastReadDate == null) return unreadMessageCount;

        List<io.getstream.chat.android.client.models.Message> messages = channel.messages;

        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message.getUser().getId().equals(userId)) continue;
            if (message.getDeletedAt() != null) continue;
            if (message.getCreatedAt().getTime() > lastReadDate.getTime())
                unreadMessageCount++;
        }
        return unreadMessageCount;
    }

    public static Date getReadDateOfChannelLastMessage(String userId, Channel channel) {
        List<io.getstream.chat.android.client.models.ChannelUserRead> read = channel.read;
        if (read == null || read.isEmpty()) return null;
        Date lastReadDate = null;
        try {
            for (int i = read.size() - 1; i >= 0; i--) {
                io.getstream.chat.android.client.models.ChannelUserRead channelUserRead = read.get(i);
                if (channelUserRead.getUser().getId().equals(userId)) {
                    lastReadDate = channelUserRead.getLastRead();
                    break;
                }
            }
        } catch (Exception e) {
            ChatLogger.Companion.getInstance().logE(e, "getReadDateOfChannelLastMessage");
        }

        return lastReadDate;
    }

    @Nullable
    public static Message computeLastMessage(Channel channel) {
        Message lastMessage = null;
        List<Message> messages = channel.getMessages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message.getDeletedAt() == null && message.getType().equals(ModelType.message_regular)) {
                lastMessage = message;
                break;
            }
        }
        setStartDay(Collections.singletonList(lastMessage), null);

        return lastMessage;
    }

    public static void setStartDay(List<Message> messages, @Nullable Message preMessage0) {
        if (messages == null) return;
        if (messages.size() == 0) return;

        Message preMessage = (preMessage0 != null) ? preMessage0 : messages.get(0);
        setFormattedDate(preMessage);
        int startIndex = (preMessage0 != null) ? 0 : 1;
        for (int i = startIndex; i < messages.size(); i++) {
            if (i != startIndex) {
                preMessage = messages.get(i - 1);
            }

            Message message = messages.get(i);
            setFormattedDate(message);
            message.setStartDay(!message.getDate().equals(preMessage.getDate()));
        }
    }

    private static void setFormattedDate(Message message) {
        if (message == null || message.getDate() != null) return;
        Utils.messageDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(message.getCreatedAt().getTime());

        Calendar now = Calendar.getInstance();

        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            message.setToday(true);
            message.setDate(TODAY.getLabel());
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            message.setYesterday(true);
            message.setDate(YESTERDAY.getLabel());
        } else if (now.get(Calendar.WEEK_OF_YEAR) == smsTime.get(Calendar.WEEK_OF_YEAR)) {
            DateFormat dayName = new SimpleDateFormat("EEEE");
            message.setDate(dayName.format(message.getCreatedAt()));
        } else {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG);
            message.setDate(dateFormat.format(message.getCreatedAt()));
        }
        DateFormat timeFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        message.setTime(timeFormat.format(message.getCreatedAt()));
    }
}
