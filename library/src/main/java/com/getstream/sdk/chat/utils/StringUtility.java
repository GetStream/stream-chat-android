package com.getstream.sdk.chat.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.getstream.sdk.chat.Chat;
import com.getstream.sdk.chat.R;

import java.text.DecimalFormat;

import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;

public class StringUtility {

    private static final String TAG = StringUtility.class.getSimpleName();

    public static String stringFromNumbers(int... numbers) {
        StringBuilder sNumbers = new StringBuilder();
        for (int number : numbers)
            sNumbers.append(number);
        return sNumbers.toString();
    }

    public static boolean isValidImageUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) return false;
        return !url.contains("svg");
    }

    public static boolean isEmoji(String message) {
        return message.matches("(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|" +
                "[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|" +
                "[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|" +
                "[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|" +
                "[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|" +
                "[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|" +
                "[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|" +
                "[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|" +
                "[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|" +
                "[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|" +
                "[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)+");
    }

    public static String getDeletedOrMentionedText(Message message) {
        if (message == null) return null;
        // Trimming New Lines
        String text = message.getText().replaceAll("^[\r\n]+|[\r\n]+$", "");

        if (message.getDeletedAt() != null) {
            text = "_" + Chat.getInstance().getStrings().get(R.string.stream_delete_message) + "_";
            return text;
        }
        if (message.getMentionedUsers() != null && !message.getMentionedUsers().isEmpty()) {
            for (User mentionedUser : message.getMentionedUsers()) {
                String userName = mentionedUser.getExtraValue("name", "");
                int index = text.indexOf(userName);
                if (index > 1 && text.charAt(index - 2) != ' ')
                    text = text.replace("@" + userName, " **" + "@" + userName + "**");
                else
                    text = text.replace("@" + userName, "**" + "@" + userName + "**");
            }
        }
        // Markdown for newline
        return text.replaceAll("\n", "  \n");
    }

    @SuppressLint("DefaultLocale")
    public static String convertVideoLength(long videoLength) {
        long hours = videoLength / 3600;
        long minutes = (videoLength % 3600) / 60;
        long seconds = videoLength % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @SuppressLint("DefaultLocale")
    public static String convertFileSizeByteCount(long bytes) {
        int unit = 1000;
        if (bytes <= 0) return 0 + " B";
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = String.valueOf(("KMGTPE").charAt(exp - 1));
        DecimalFormat df = new DecimalFormat("###.##");
        return df.format(bytes / Math.pow(unit, exp)) + " " + pre + "B";
    }

    public static String convertMentionedText(String text, String userName) {
        if (text.substring(text.length() - 1).equals("@"))
            return text + userName;

        String[] names = text.split("@");
        String last = names[names.length - 1];
        return text.substring(0, text.length() - last.length()) + userName;
    }

    public static boolean isEmptyTextMessage(String text){
        if (TextUtils.isEmpty(text))
            return true;
        String s = text.replaceAll("\\s+", "");
        return TextUtils.isEmpty(s);
    }

    @Nullable
    public static String getChannelIdFromCid(@NonNull String cid) {
        String[] array = cid.split(":");
        if (array.length > 1) {
            return array[1];
        }
        return null;
    }

    @Nullable
    public static String getChannelTypeFromCid(@NonNull String cid) {
        String[] array = cid.split(":");
        if (array.length > 1) {
            return array[0];
        }
        return null;
    }
}
