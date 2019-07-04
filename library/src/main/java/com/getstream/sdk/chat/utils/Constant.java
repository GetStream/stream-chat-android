package com.getstream.sdk.chat.utils;

public final class Constant {

    public static final String MESSAGE_DELETED = "This message was deleted.";

    public static final int HEALTH_CHECK_INTERVAL = 1000 * 30;
    public static final int DEFAULT_LIMIT = 50;

    // Tags
    public static final String TAG_MESSAGE_REACTION = "TAG_MESSAGE_REACTION";
    public static final String TAG_MESSAGE_MOREACTION = "TAG_MESSAGE_MOREACTION";
    public static final String TAG_MESSAGE_RESEND = "TAG_MESSAGE_RESEND";
    public static final String TAG_MESSAGE_INVALID_COMMAND = "TAG_MESSAGE_INVALID_COMMAND";
    public static final String TAG_MESSAGE_CHECK_DELIVERED = "TAG_MESSAGE_CHECK_DELIVERED";

    public static final String TAG_MOREACTION_EDIT = "TAG_MOREACTION_EDIT";
    public static final String TAG_MOREACTION_DELETE = "TAG_MOREACTION_DELETE";
    public static final String TAG_MOREACTION_REPLY = "TAG_MOREACTION_REPLY";

    public static final String TAG_ACTION_SEND = "TAG_ACTION_SEND";
    public static final String TAG_ACTION_SHUFFLE = "TAG_ACTION_SHUFFLE";
    public static final String TAG_ACTION_CANCEL = "TAG_ACTION_CANCEL";

    public static final String TAG_ATTACH_FILE_PATH = "TAG_ATTACH_FILE_PATH";

    // pagination
    public static final int CHANNEL_LIMIT = 30;
    public static final int CHANNEL_MESSAGE_LIMIT = 25;
    public static final int THREAD_MESSAGE_LIMIT = 25;

    public static final int USER_LIMIT = 30;
    // Channel
    public static final int TYPYING_CLEAN_INTERVAL = 15 * 1000;
    // Permission Code
    public static final int SELECT_PICTURE_REQUEST_CODE = 1001;
    public static final int CAPTURE_IMAGE_REQUEST_CODE = 1002;
    public static final int PERMISSIONS_REQUEST = 1003;
    // Activity Result Code
    public static final int USERSLISTACTIVITY_REQUEST = 1004;
    // File Attach
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 3;

    // Command
    public static final String COMMAND_GIPHY = "giphy";
    public static final String COMMAND_IMGUR = "imgur";
    public static final String COMMAND_BAN = "ban";
    public static final String COMMAND_FLAG = "flag";
}

