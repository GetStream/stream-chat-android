package com.getstream.sdk.chat.utils;


import android.text.TextUtils;

import com.getstream.sdk.chat.Chat;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.AttachmentMetaData;
import com.getstream.sdk.chat.model.ModelType;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import androidx.annotation.Nullable;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.models.*;
import io.getstream.chat.android.livedata.ChatDomain;

import static com.getstream.sdk.chat.enums.Dates.TODAY;
import static com.getstream.sdk.chat.enums.Dates.YESTERDAY;

public class LlcMigrationUtils {

    private static Map<String, String> reactionTypes;

    public static String getInitials(User user) {

        String name = (String) user.getExtraData().get("name");

        if (name == null) {
            name = "";
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

    @Nullable
    public static String getName(Channel channel) {
        if (channel.getExtraData().containsKey("name")) {
            return (String) channel.getExtraData().get("name");
        } else {
            return "";
        }
    }

    public static List<Attachment> getAttachments(List<AttachmentMetaData> attachments) {
        List<Attachment> result = new ArrayList<>();
        for (AttachmentMetaData attachment : attachments)
            result.add(attachment.attachment);
        return result;
    }

    public static List<AttachmentMetaData> getMetaAttachments(Message message) {
        return getMetaAttachments(message.getAttachments());
    }

    public static List<AttachmentMetaData> getMetaAttachments(List<Attachment> attachments) {
        List<AttachmentMetaData> result = new ArrayList<>();
        for (Attachment attachment : attachments) {
            result.add(new AttachmentMetaData(attachment));
        }
        return result;
    }


    public static String getFileSizeHumanized(Attachment attachment) {
        int size = attachment.getFileSize();
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String getImage(Channel channel) {

        if (!channel.getExtraData().containsKey("image")) {
            return null;
        } else {
            Object image = channel.getExtraData().get("image");
            if (image instanceof String) {
                return (String) image;
            }
            return null;
        }

    }

    public static boolean readLastMessage(Channel channel) {
        User currentUser = getCurrentUser();
        String currentUserId = currentUser.getId();
        Date myReadDate = getReadDateOfChannelLastMessage(currentUserId, channel);
        Message lastMessage = computeLastMessage(channel);
        if (myReadDate == null) {
            return false;
        } else if (lastMessage == null) {
            return true;
        } else return myReadDate.getTime() > lastMessage.getCreatedAt().getTime();
    }

    public static List<ChannelUserRead> getLastMessageReads(Channel channel) {
        Message lastMessage = computeLastMessage(channel);
        List<ChannelUserRead> readLastMessage = new ArrayList<>();
        List<ChannelUserRead> reads = channel.getRead();
        if (reads == null || lastMessage == null) return readLastMessage;

        User currentUser = getCurrentUser();
        String currentUserId = currentUser.getId();

        for (ChannelUserRead r : reads) {
            if (r.getUserId().equals(currentUserId))
                continue;
            if (r.getLastRead().compareTo(lastMessage.getCreatedAt()) > -1) {
                readLastMessage.add(r);
            }
        }

        // sort the reads
        Collections.sort(readLastMessage, (ChannelUserRead o1, ChannelUserRead o2) -> o1.getLastRead().compareTo(o2.getLastRead()));
        return readLastMessage;
    }

    public static Date getLastActive(List<Member> members) {
        Date lastActive = new Date();
        for (Member member: members) {
            if (member.getUser().getId() != ChatDomain.instance().getCurrentUser().getId()) {
                if (member.getUser().getLastActive()!= null) {
                    lastActive = member.getUser().getLastActive();
                }

            }
        }

        //TODO: return the right date
        return lastActive;

    }

    public static String getChannelNameOrMembers(Channel channel) {
        String channelName;

        String name = getName(channel);

        if (!TextUtils.isEmpty(name)) {
            channelName = name;
        } else {
            List<User> users = getOtherUsers(channel.getMembers());
            List<User> top3 = users.subList(0, Math.min(3, users.size()));
            List<String> usernames = new ArrayList<>();
            for (User u : top3) {
                if (u == null) continue;
                usernames.add(u.getExtraValue("name", ""));
            }

            channelName = TextUtils.join(", ", usernames);
            if (users.size() > 3) {
                channelName += "...";
            }
        }

        if (channelName == null) return "";
        else return channelName;
    }

    public static boolean equalsName(Channel a, Channel b) {
        String nameA = getName(a);
        String nameB = getName(b);

        return nameA.equals(nameB);
    }

    public static boolean equalsLastMessageDate(Channel a, Channel b) {
        Date lastA = a.getLastMessageAt();
        Date lastB = b.getLastMessageAt();
        if (lastA == null && lastB != null) return false;
        if (lastA != null && lastB == null) return false;
        if (lastA == null) return true;

        return lastA.equals(lastB);
    }

    public static boolean currentUserRead(Channel oldCh, Channel newCh) {
        User currentUser = getCurrentUser();
        String id = currentUser.getId();

        ChannelUserRead oldRead = getRead(oldCh, id);
        ChannelUserRead newRead = getRead(newCh, id);

        if (oldRead == null && newRead == null) {
            return false;
        } else if (oldRead == null) {
            return false;
        } else if (newRead == null) {
            return false;
        } else {
            Date newDate = newRead.getLastRead();
            Date oldDate = oldRead.getLastRead();
            return newDate.after(oldDate);
        }
    }

    public static ChannelUserRead getRead(Channel channel, String userId) {
        int idx = indexOfRead(channel, userId);
        if (idx == -1) return null;
        else return channel.getRead().get(idx);
    }

    public static boolean equalsUserReads(Channel chA, Channel chB) {

        List<ChannelUserRead> a = LlcMigrationUtils.getLastMessageReads(chA);
        List<ChannelUserRead> b = LlcMigrationUtils.getLastMessageReads(chB);

        if (a.size() != b.size()) return false;
        if (a.size() == 0 && b.size() == 0) return true;

        for (int i = 0; i < a.size(); i++) {
            ChannelUserRead readA = a.get(i);
            ChannelUserRead readB = b.get(i);

            boolean dates = readA.getLastRead().equals(readB.getLastRead());
            if (!dates) return false;
            boolean users = readA.getUser().getId().equals(readB.getUser().getId());
            if (!users) return false;

        }
        return true;
    }

    public static boolean lastMessagesAreTheSame(Channel a, Channel b) {
        Message oldLastMessage = computeLastMessage(a);
        Message newLastMessage = computeLastMessage(b);
        if (oldLastMessage != null &&
                newLastMessage != null &&
                newLastMessage.getUpdatedAt() != null &&
                oldLastMessage.getUpdatedAt() != null &&
                oldLastMessage.getUpdatedAt().getTime() < newLastMessage.getUpdatedAt().getTime()) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean equalsLastReaders(Channel a, Channel b) {
        User oldLastReader = getLastReader(a);
        User newLastReader = getLastReader(b);

        if (oldLastReader == null && newLastReader == null) return true;
        if (oldLastReader == null) return false;
        if (newLastReader == null) return false;
        return oldLastReader.getId().equals(newLastReader.getId());
    }

    public static User getLastReader(Channel channel) {
        List<ChannelUserRead> read = channel.getRead();
        if (read == null || read.isEmpty()) return null;
        User lastReadUser = null;
        for (int i = read.size() - 1; i >= 0; i--) {

            User currentUser = getCurrentUser();

            ChannelUserRead channelUserRead = read.get(i);

            if (currentUser != null) {
                String id = currentUser.getId();
                String readUserId = channelUserRead.getUser().getId();

                if (!id.equals(readUserId)) {
                    lastReadUser = channelUserRead.getUser();
                    break;
                }
            }
        }
        return lastReadUser;
    }

    public static void updateReadState(Channel channel, User user, Date date) {

        int indexOfRead = indexOfRead(channel, user.getId());
        ChannelUserRead read = new ChannelUserRead(user, date);

        if (indexOfRead == -1) {
            channel.getRead().add(read);
        } else {
            channel.getRead().set(indexOfRead, read);
        }
    }

    public static int indexOfRead(Channel channel, String userId) {
        for (int i = 0; i < channel.getRead().size(); i++)
            if (channel.getRead().get(i).getUser().getId().equals(userId))
                return i;

        return -1;
    }

    public static boolean equalsUserLists(List<User> a, List<User> b) {
        if (a.size() != b.size()) return false;
        if (a.size() == 0 && b.size() == 0) return true;

        for (int i = 0; i < a.size(); i++) {
            User userA = a.get(i);
            User userB = b.get(i);

            if (!userA.getId().equals(userB.getId())) {
                return false;
            }
        }
        return true;
    }

    public static List<User> getOtherUsers( List<Member> members) {

        List<User> result = new ArrayList<>();


        for (Member m : members) {
            String memberId = m.getUserId();
            boolean isFromCurrentUser = isFromCurrentUser(memberId);
            if (!isFromCurrentUser) {
                User user = m.getUser();
                if (user != null) result.add(user);
            }
        }

        return result;
    }

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

    public static int getIcon(Attachment attachment) {
        return getIcon(attachment.getMimeType());
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

    public static boolean isFromCurrentUser(ChatEvent event) {
        User user = event.getUser();
        User currentUser = getCurrentUser();
        if (user == null || currentUser == null) return false;
        return user.getId().equals(currentUser.getId());
    }

    public static boolean isFromCurrentUser(String userId) {
        User currentUser = getCurrentUser();
        if (userId == null || currentUser == null) return false;
        return userId.equals(currentUser.getId());
    }

    public static Map<String, ChannelUserRead> getReadsByUser(Channel channel) {
        Map<String, ChannelUserRead> result = new HashMap<>();
        for (ChannelUserRead r : channel.getRead()) result.put(r.getUserId(), r);
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
    public static Message getOldestMessage(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }

        return messages.get(0);
    }

    public static int getUnreadMessageCount(String userId, Channel channel) {
        int unreadMessageCount = 0;
        List<ChannelUserRead> read = channel.getRead();
        if (read == null || read.isEmpty()) return unreadMessageCount;

        Date lastReadDate = getReadDateOfChannelLastMessage(userId, channel);
        if (lastReadDate == null) return unreadMessageCount;

        List<io.getstream.chat.android.client.models.Message> messages = channel.getMessages();

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
        List<ChannelUserRead> read = channel.getRead();
        if (read == null || read.isEmpty()) return null;
        Date lastReadDate = null;
        try {
            for (int i = read.size() - 1; i >= 0; i--) {
                ChannelUserRead channelUserRead = read.get(i);
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

    public static int indexOf(List<Message> messages, Message message) {
        String id = message.getId();
        for (int i = 0; i < messages.size(); i++)
            if (id.equals(messages.get(i).getId())) return i;
        return -1;
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
    
    public static User getCurrentUser(){
        return ChatDomain.instance().getCurrentUser();
    }
}
