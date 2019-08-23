package com.getstream.sdk.chat.utils;

public final class Constant {

    public static final String MESSAGE_DELETED = "This message was deleted.";

    public static final int HEALTH_CHECK_INTERVAL = 1000 * 30;
    public static final int DEFAULT_LIMIT = 50;

    // Tags

    public static final String TAG_MESSAGE_RESEND = "TAG_MESSAGE_RESEND";
    public static final String TAG_MESSAGE_INVALID_COMMAND = "TAG_MESSAGE_INVALID_COMMAND";
    public static final String TAG_MESSAGE_CHECK_DELIVERED = "TAG_MESSAGE_CHECK_DELIVERED";


    public static final String TAG_CHANNEL_RESPONSE_ID = "TAG_CHANNEL_RESPONSE_ID";
    // Broad Cast
    public static final String BC_RECONNECT_CHANNEL = "BC_RECONNECT_CHANNEL";
    public static final String BC_CONNECTION_OFF = "BC_CONNECTION_OFF";
    public static final String BC_CONNECTION_ON = "BC_CONNECTION_ON";

    public static final String NO_INTERNET = "No internet connection!";
    public static final int NO_INTERNET_ERROR_CODE = 10001;
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

}

