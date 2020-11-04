package com.getstream.sdk.chat.utils;


import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.AttachmentMetaData;
import com.getstream.sdk.chat.model.ModelType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.ChannelUserRead;
import io.getstream.chat.android.client.models.Member;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.livedata.ChatDomain;

public class LlcMigrationUtils {

    private static Map<String, String> reactionTypes;

    @NonNull
    public static String getInitials(User user) {
        return PrimitivesKt.initials((String) user.getExtraData().get("name"));
    }

    @Nullable
    public static String getName(Channel channel) {
        if (channel.getExtraData().containsKey("name")) {
            return (String) channel.getExtraData().get("name");
        } else {
            return "";
        }
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

    public static boolean readLastMessage(Channel channel) {
        User currentUser = getCurrentUser();
        String currentUserId = currentUser.getId();
        Date myReadDate = getReadDateOfChannelLastMessage(currentUserId, channel);
        Message lastMessage = computeLastMessage(channel);
        if (myReadDate == null) {
            return false;
        } else if (lastMessage == null) {
            return true;
        } else {
            Date lastMessageDate = lastMessage.getCreatedAt() != null ? lastMessage.getCreatedAt() : lastMessage.getCreatedLocallyAt();
            boolean shouldBeMarkedRead = myReadDate.getTime() >= (lastMessageDate != null ? lastMessageDate.getTime() : 0);
            return shouldBeMarkedRead;
        }
    }

    public static List<ChannelUserRead> getLastMessageReads(Channel channel) {
        Message lastMessage = computeLastMessage(channel);
        List<ChannelUserRead> readLastMessage = new ArrayList<>();
        List<ChannelUserRead> reads = channel.getRead();
        if (lastMessage == null || lastMessage.getCreatedAt() == null) return readLastMessage;

        User currentUser = getCurrentUser();
        String currentUserId = currentUser.getId();

        for (ChannelUserRead r : reads) {
            if (r.getUserId().equals(currentUserId) || r.getLastRead() == null)
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
        for (Member member : members) {
            if (member.getUser().getId() != ChatDomain.instance().getCurrentUser().getId()) {
                if (member.getUser().getLastActive() != null) {
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

    public static boolean lastMessagesAreTheSame(Channel a, Channel b) {
        Message oldLastMessage = computeLastMessage(a);
        Message newLastMessage = computeLastMessage(b);
        if (oldLastMessage == null && newLastMessage == null) {
            return true;
        } else if (oldLastMessage == null || newLastMessage == null) {
            return false;
        } else {
            return oldLastMessage.equals(newLastMessage);
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

    public static List<User> getOtherUsers(List<Member> members) {

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

    @NonNull
    public static String getInitials(Channel channel) {
        return PrimitivesKt.initials((String) channel.getExtraData().get("name"));
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
        int fileTyineRes = R.drawable.stream_ic_file;
        if (mimeType == null) {
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

    public static boolean isFromCurrentUser(String userId) {
        User currentUser = getCurrentUser();
        if (userId == null || currentUser == null) return false;
        return userId.equals(currentUser.getId());
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
        return lastMessage;
    }

    public static User getCurrentUser() {
        return ChatDomain.instance().getCurrentUser();
    }
}
