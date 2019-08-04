package com.getstream.sdk.chat.utils;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.getstream.sdk.chat.component.Component;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.BaseURL;
import com.getstream.sdk.chat.rest.core.StreamChat;
import com.getstream.sdk.chat.enums.Location;
import com.getstream.sdk.chat.model.SelectAttachmentModel;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Global {

    private static final String TAG = Global.class.getSimpleName();

    public static BaseURL baseURL = new BaseURL(Location.US_EAST);
    public static SelectAttachmentModel selectAttachmentModel;
    public static StreamChat client;
    public static Component component;
//    public static EventFunction eventFunction;


    public static boolean noConnection = false;

//    public static List<ChannelResponse> channels = new ArrayList<>();
    public static List<User> typingUsers = new ArrayList<>();

    // region Set Date and Time
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

    public static final Locale locale = new Locale("en", "US", "POSIX");
    public static final DateFormat messageDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", locale);

    private static void setFormattedDate(Message message) {
        if (message == null || message.getDate() != null) return;
        messageDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String sendDate = message.getCreated_at();

        Date date = null;
        try {
            date = messageDateFormat.parse(sendDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(date.getTime());

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEEE";

        DateFormat timeFormat = new SimpleDateFormat(timeFormatString, locale);
        DateFormat dateFormat1 = new SimpleDateFormat(dateTimeFormatString, locale);
        DateFormat dateFormat2 = new SimpleDateFormat("MMMM dd yyyy", locale);

//        Log.d(TAG, "Date: " + message.getCreated_at());
//        Log.d(TAG, "Calendar.DATE: " + now.get(Calendar.DATE));
//        Log.d(TAG, "Calendar.WEEK_OF_YEAR: " + now.get(Calendar.WEEK_OF_YEAR));
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            message.setToday(true);
            message.setDate("Today");
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            message.setYesterday(true);
            message.setDate("Yesterday");
        } else if (now.get(Calendar.WEEK_OF_YEAR) == smsTime.get(Calendar.WEEK_OF_YEAR)) {
            message.setDate(dateFormat1.format(date));
        } else {
            message.setDate(dateFormat2.format(date));
        }
        message.setTime(timeFormat.format(date));
        message.setCreated(dateFormat2.format(date));
    }


    public static String convertDateToString(Date date) {
        messageDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String timeStr = messageDateFormat.format(date);
        return timeStr;
    }

    public static boolean isCommandMessage(Message message) {
        return message.getText().startsWith("/");
    }

    // Passed Time
    public static String differentTime(String dateStr) {
        if (TextUtils.isEmpty(dateStr)) return null;
        Date lastActiveDate = null;
        try {
            lastActiveDate = messageDateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        Date dateTwo = new Date();
        long timeDiff = Math.abs(lastActiveDate.getTime() - dateTwo.getTime()) / 1000;
        String timeElapsed = TimeElapsed(timeDiff);
        String differTime = "";
        if (timeElapsed.contains("Just now"))
            differTime = "Active: " + timeElapsed;
        else
            differTime = "Active: " + timeElapsed + " ago";

        return differTime;
    }

    public static String TimeElapsed(long seconds) {
        String elapsed;
        if (seconds < 60) {
            elapsed = "Just now";
        } else if (seconds < 60 * 60) {
            int minutes = (int) (seconds / 60);
            elapsed = String.valueOf(minutes) + " " + ((minutes > 1) ? "mins" : "min");
        } else if (seconds < 24 * 60 * 60) {
            int hours = (int) (seconds / (60 * 60));
            elapsed = String.valueOf(hours) + " " + ((hours > 1) ? "hours" : "hour");
        } else {
            int days = (int) (seconds / (24 * 60 * 60));
            elapsed = String.valueOf(days) + " " + ((days > 1) ? "days" : "day");
        }
        return elapsed;
    }
    // endregion

    // region Attachment
    public static List<Attachment> attachments = new ArrayList<>();

    public static List<Attachment> Search_Dir(File dir) {
        String pdfPattern = ".pdf";
        String pptPattern = ".ppt";
        String csvPattern = ".csv";
        String docPattern = ".doc";
        String docxPattern = ".docx";
        String txtPattern = ".txt";
//        String xlsPattern = ".xls";
        String xlsxPattern = ".xlsx";

        String zipPattern = ".zip";
        String tarPattern = ".tar";

        String movPattern = ".mov";
//        String mp4Pattern = ".mp4";
        String mp3Pattern = ".mp3";

        File FileList[] = dir.listFiles();
        if (FileList != null) {
            for (int i = 0; i < FileList.length; i++) {
                if (FileList[i].isDirectory()) {
                    Search_Dir(FileList[i]);
                } else {
                    Attachment attachment = new Attachment();

                    if (FileList[i].getName().endsWith(pdfPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_pdf);
                    } else if (FileList[i].getName().endsWith(pptPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_ppt);
                    } else if (FileList[i].getName().endsWith(csvPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_csv);
                    }/*else if (FileList[i].getName().endsWith(xlsPattern)) {
                        attachment.setMime_type(ModelType.attach_mine_xls);
                    }*/ else if (FileList[i].getName().endsWith(xlsxPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_xlsx);
                    } else if (FileList[i].getName().endsWith(docPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_doc);
                    } else if (FileList[i].getName().endsWith(docxPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_docx);
                    } else if (FileList[i].getName().endsWith(txtPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_txt);
                    } else if (FileList[i].getName().endsWith(zipPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_zip);
                    } else if (FileList[i].getName().endsWith(tarPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_tar);
                    } else if (FileList[i].getName().endsWith(movPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_mov);
                    }/*else if (FileList[i].getName().endsWith(mp4Pattern)) {
                        attachment.setMime_type(ModelType.attach_mime_mp4);
                    }*/ else if (FileList[i].getName().endsWith(mp3Pattern)) {
                        attachment.setMime_type(ModelType.attach_mime_mp3);
                    }

                    if (attachment.getMime_type() != null) {
                        attachment.setType(ModelType.attach_file);
                        attachment.setTitle(FileList[i].getName());
                        attachment.config.setFilePath(FileList[i].getPath());
                        long size = FileList[i].length();
                        attachment.setFile_size((int) size);
                        attachments.add(attachment);
                    }
                }
            }
        }
        return attachments;
    }

    public static ArrayList<Attachment> getAllShownImagesPath(Activity context) {
        String[] columns = {MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.VideoColumns.DURATION,
        };
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        final String orderBy = MediaStore.Files.FileColumns.DATE_ADDED;
        Uri queryUri = MediaStore.Files.getContentUri("external");

        @SuppressWarnings("deprecation")
        Cursor imagecursor = context.managedQuery(queryUri,
                columns,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        int image_column_index = imagecursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
        int count = imagecursor.getCount();

        ArrayList<Attachment> attachments = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Attachment attachment = new Attachment();
            imagecursor.moveToPosition(i);
            int id = imagecursor.getInt(image_column_index);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            int type = imagecursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
            int t = imagecursor.getInt(type);
            attachment.config.setFilePath(imagecursor.getString(dataColumnIndex));

            if (t == Constant.MEDIA_TYPE_IMAGE) {
                attachment.setType(ModelType.attach_image);
            } else if (t == Constant.MEDIA_TYPE_VIDEO) {
                float videolengh = imagecursor.getLong(imagecursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
                attachment.setType(ModelType.attach_file);
                attachment.setMime_type(ModelType.attach_mime_mp4);
                attachment.config.setVideoLengh((int) (videolengh / 1000));
                attachments.add(attachment);
            }
            attachments.add(attachment);
        }

        return attachments;
    }
    // endregion

    // region Channel
    public static ChannelResponse getChannelResponseById(String id) {
        ChannelResponse response_ = null;
        for (ChannelResponse response : client.channels) {
            if (id.equals(response.getChannel().getId())) {
                response_ = response;
                break;
            }
        }
        return response_;
    }

    public static ChannelResponse getPrivateChannel(User user) {
        String channelId1 = client.user.getId() + "-" + user.getId(); // Created by
        String channelId2 = user.getId() + "-" + client.user.getId(); // Invited by
        ChannelResponse channelResponse = null;
        for (ChannelResponse response : client.channels) {
            if (response.getChannel().getId().equals(channelId1) || response.getChannel().getId().equals(channelId2)) {
                channelResponse = response;
                break;
            }
        }
        return channelResponse;
    }

    public static User getOpponentUser(ChannelResponse channelResponse) {
        if (channelResponse.getMembers() == null || channelResponse.getMembers().isEmpty())
            return null;
        if (channelResponse.getMembers().size() > 2) return null;
        User opponent = null;
        try {
            for (Member member : channelResponse.getMembers()) {
                if (!member.getUser().getId().equals(client.user.getId())) {
                    opponent = member.getUser();
                    break;
                }
            }
        } catch (Exception e) {
        }
        return opponent;
    }

    public static List<String> getMentionedUserIDs(ChannelResponse response, String text) {
        if (TextUtils.isEmpty(text)) return null;

        List<String> mentionedUserIDs = new ArrayList<>();
        if (response.getMembers() != null && !response.getMembers().isEmpty()) {
            for (Member member : response.getMembers()) {
                String userName = member.getUser().getName();
                if (text.contains("@" + userName)) {
                    mentionedUserIDs.add(member.getUser().getId());
                }
            }
        }
        return mentionedUserIDs;
    }
    // endregion

    // region Message

    public static void setEphemeralMessage(String channelId, Message message) {
        List<Message> messages = client.ephemeralMessage.get(channelId);
        if (messages == null) messages = new ArrayList<>();

        boolean isContain = false;
        for (Message message1 : messages) {
            if (message1.getId().equals(message.getId())) {
                messages.remove(message1);
                isContain = true;
                break;
            }
        }
        if (!isContain)
            messages.add(message);

        client.ephemeralMessage.put(channelId, messages);
    }

    public static List<Message> getEphemeralMessages(String channelId, String parentId) {
        List<Message> ephemeralMessages = client.ephemeralMessage.get(channelId);
        if (ephemeralMessages == null) return null;

        List<Message> messages = new ArrayList<>();
        if (parentId == null) {
            for (Message message : ephemeralMessages) {
                if (message.getParent_id() == null)
                    messages.add(message);
            }
        } else {
            for (Message message : ephemeralMessages) {
                if (message.getParent_id() == null) continue;
                if (message.getParent_id().equals(parentId))
                    messages.add(message);
            }
        }
        return messages;
    }

    public static void removeEphemeralMessage(String channelId, String messageId) {
        Log.d(TAG, "remove MessageId: " + messageId);
        List<Message> messages = client.ephemeralMessage.get(channelId);
        for (Message message : messages) {
            if (message.getId().equals(messageId)) {
                Log.d(TAG, "Message Removed!");
                messages.remove(message);
                break;
            }
        }
    }

    public static String getMentionedText(Message message) {
        if (message == null) return null;
        String text = message.getText();
        if (message.getMentionedUsers() != null && !message.getMentionedUsers().isEmpty()) {
            for (User mentionedUser : message.getMentionedUsers()) {
                String userName = mentionedUser.getName();
                text = text.replace("@" + userName, "**" + "@" + userName + "**");
            }
        }
        return text;
    }

    // endregion
    public static void sortUserReads(List<ChannelUserRead> reads) {
        Collections.sort(reads, (ChannelUserRead o1, ChannelUserRead o2) -> {
            return o1.getLast_read().compareTo(o2.getLast_read());
        });
    }

    public static List<User> getReadUsers(ChannelResponse response, Message message) {
        if (response.getReads() == null || response.getReads().isEmpty()) return null;
        List<User> users = new ArrayList<>();

        for (int i = response.getReads().size() - 1; i >= 0; i--) {
            ChannelUserRead read = response.getReads().get(i);
            if (readMessage(read.getLast_read(), message.getCreated_at())) {
                if (!users.contains(read.getUser()) && !read.getUser().getId().equals(client.user.getId()))
                    users.add(read.getUser());
            }
        }
        return users;
    }
    public static boolean readMessage(String lastReadMessageDate, String channelLastMesage) {
        if (lastReadMessageDate == null) return true;

        Global.messageDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateUserRead, dateChannelMessage;

        try {
            dateUserRead = Global.messageDateFormat.parse(lastReadMessageDate);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        try {
            dateChannelMessage = Global.messageDateFormat.parse(channelLastMesage);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (dateUserRead.equals(dateChannelMessage) || dateUserRead.after(dateChannelMessage)) {
            return true;
        }
        return false;
    }
    // ONLY_FOR_DEBUG
    public static boolean checkMesageGapState = false;
}
