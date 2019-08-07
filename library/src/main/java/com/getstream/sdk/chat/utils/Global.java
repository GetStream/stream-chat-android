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
import com.getstream.sdk.chat.model.SelectAttachmentModel;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Global {

    private static final String TAG = Global.class.getSimpleName();

    public static SelectAttachmentModel selectAttachmentModel;
    public static Component component;

    public static List<User> typingUsers = new ArrayList<>();

    public static final Locale locale = new Locale("en", "US", "POSIX");
    public static final DateFormat messageDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", locale);


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
    public static User getOpponentUser(ChannelState channelState) {
        return null;
//        if (channelState.getMembers() == null || channelState.getMembers().isEmpty())
//            return null;
//        if (channelState.getMembers().size() > 2) return null;
//        User opponent = null;
//        try {
//            for (Member member : channelState.getMembers()) {
//                if (!member.getUser().getId().equals(client.user.getId())) {
//                    opponent = member.getUser();
//                    break;
//                }
//            }
//        } catch (Exception e) {
//        }
//        return opponent;
    }

    public static List<String> getMentionedUserIDs(ChannelState response, String text) {
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
//        List<Message> messages = client.ephemeralMessage.get(channelId);
//        if (messages == null) messages = new ArrayList<>();
//
//        boolean isContain = false;
//        for (Message message1 : messages) {
//            if (message1.getId().equals(message.getId())) {
//                messages.remove(message1);
//                isContain = true;
//                break;
//            }
//        }
//        if (!isContain)
//            messages.add(message);
//
//        client.ephemeralMessage.put(channelId, messages);
    }

    public static List<Message> getEphemeralMessages(String channelId, String parentId) {
        return new ArrayList<>();
//        List<Message> ephemeralMessages = client.ephemeralMessage.get(channelId);
//        if (ephemeralMessages == null) return null;
//
//        List<Message> messages = new ArrayList<>();
//        if (parentId == null) {
//            for (Message message : ephemeralMessages) {
//                if (message.getParent_id() == null)
//                    messages.add(message);
//            }
//        } else {
//            for (Message message : ephemeralMessages) {
//                if (message.getParent_id() == null) continue;
//                if (message.getParent_id().equals(parentId))
//                    messages.add(message);
//            }
//        }
//        return messages;
    }

    public static void removeEphemeralMessage(String channelId, String messageId) {
//        Log.d(TAG, "remove MessageId: " + messageId);
//        List<Message> messages = client.ephemeralMessage.get(channelId);
//        for (Message message : messages) {
//            if (message.getId().equals(messageId)) {
//                Log.d(TAG, "Message Removed!");
//                messages.remove(message);
//                break;
//            }
//        }
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

    public static List<User> getReadUsers(ChannelState response, Message message) {
        return null;
//        if (response.getReads() == null || response.getReads().isEmpty()) return null;
//        List<User> users = new ArrayList<>();
//
//        for (int i = response.getReads().size() - 1; i >= 0; i--) {
//            ChannelUserRead read = response.getReads().get(i);
//            if (readMessage(read.getLast_read(), message.getCreatedAt())) {
//                if (!users.contains(read.getUser()) && !read.getUser().getId().equals(client.user.getId()))
//                    users.add(read.getUser());
//            }
//        }
//        return users;
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
