package com.getstream.sdk.chat.utils;


import android.text.TextUtils;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.ModelType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import androidx.annotation.Nullable;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.ChannelUserRead;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;

import static com.getstream.sdk.chat.enums.Dates.TODAY;
import static com.getstream.sdk.chat.enums.Dates.YESTERDAY;

public class LlcMigrationUtils {

    private static Map<String, String> reactionTypes;

    public static String getInitials(Channel channel) {



        String name = (String) channel.getExtraData().get("name");
        if (name == null) {
            return "";
        }
        String[] names = name.split(" ");
        String firstName = names[0];
        String lastName = null;
        try {
            lastName = names[1];
        } catch (Exception e) {
        }

        if (!TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName))
            return firstName.substring(0, 1).toUpperCase();
        if (TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName))
            return lastName.substring(0, 1).toUpperCase();

        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName))
            return firstName.substring(0, 1).toUpperCase() + lastName.substring(0, 1).toUpperCase();
        return null;
    }

    public static Map<String, String> getReactionTypes() {
        if (reactionTypes == null) {
            reactionTypes = new HashMap<String, String>() {
                {
                    put("like", "\uD83D\uDC4D");
                    put("love", "\u2764\uFE0F");
                    put("haha", "\uD83D\uDE02");
                    put("wow", "\uD83D\uDE32");
                    put("sad", " \uD83D\uDE41");
                    put("angry", "\uD83D\uDE21");
                }
            };
        }
        return reactionTypes;
    }

    public static int getIcon(String mimeType) {
        int fileTyineRes = 0;
        if (mimeType == null) {
            fileTyineRes = R.drawable.stream_ic_file;
            return fileTyineRes;
        }

        switch (mimeType) {
            case ModelType.attach_mime_pdf:
                fileTyineRes = R.drawable.stream_ic_file_pdf;
                break;
            case ModelType.attach_mime_csv:
                fileTyineRes = R.drawable.stream_ic_file_csv;
                break;
            case ModelType.attach_mime_tar:
                fileTyineRes = R.drawable.stream_ic_file_tar;
                break;
            case ModelType.attach_mime_zip:
                fileTyineRes = R.drawable.stream_ic_file_zip;
                break;
            case ModelType.attach_mime_doc:
            case ModelType.attach_mime_docx:
            case ModelType.attach_mime_txt:
                fileTyineRes = R.drawable.stream_ic_file_doc;
                break;
            case ModelType.attach_mime_xlsx:
                fileTyineRes = R.drawable.stream_ic_file_xls;
                break;
            case ModelType.attach_mime_ppt:
                fileTyineRes = R.drawable.stream_ic_file_ppt;
                break;
            case ModelType.attach_mime_mov:
            case ModelType.attach_mime_mp4:
                fileTyineRes = R.drawable.stream_ic_file_mov;
                break;
            case ModelType.attach_mime_m4a:
            case ModelType.attach_mime_mp3:
                fileTyineRes = R.drawable.stream_ic_file_mp3;
                break;
            default:
                if (mimeType.contains("audio")) {
                    fileTyineRes = R.drawable.stream_ic_file_mp3;
                } else if (mimeType.contains("video")) {
                    fileTyineRes = R.drawable.stream_ic_file_mov;
                }
                break;
        }
        return fileTyineRes;
    }

    public static boolean isFromCurrentUser(ChatEvent event){
        User user = event.getUser();
        User currentUser = StreamChat.getInstance().getCurrentUser();
        if(user == null || currentUser == null) return false;
        return user.getId().equals(currentUser.getId());
    }

    public static Map<String, ChannelUserRead> getReadsByUser(Channel channel) {
        Map<String, ChannelUserRead> result = new HashMap<>();
        for (ChannelUserRead r : channel.read) result.put(r.getUserId(), r);
        return result;
    }

    @Nullable
    public static String getOldestMessageId(Channel channel) {
        Message oldestMessage = getOldestMessage(channel.getMessages());
        if (oldestMessage == null) {
            return null;
        } else {
            return oldestMessage.getId();
        }
    }

    @Nullable
    public static String getOldestMessageId(List<Message> messages) {
        Message message = getOldestMessage(messages);
        if (message == null) return null;
        else return message.getId();
    }

    @Nullable
    private static Message getOldestMessage(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }

        return messages.get(0);
    }

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
