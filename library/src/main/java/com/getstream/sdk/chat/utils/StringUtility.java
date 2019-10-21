package com.getstream.sdk.chat.utils;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

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
            text = "_" + Constant.MESSAGE_DELETED + "_";
            return text;
        }
        if (message.getMentionedUsers() != null && !message.getMentionedUsers().isEmpty()) {
            for (User mentionedUser : message.getMentionedUsers()) {
                String userName = mentionedUser.getName();
                text = text.replace("@" + userName, "**" + "@" + userName + "**");
            }
        }
        // Markdown for newline
        text = text.replaceAll("<br/>  <br/>  \n", "\n");
        return text.replaceAll("\n", "<br/>  <br/>  \n");
    }



    public static String getSaltString(String s) {
        String s_ = s.replaceAll("\\s+","");
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        int minIndex = Math.min(5, s_.length() - 1);
        salt.append(s_.substring(0,minIndex));
        salt.append("-");
        Random rnd = new Random();
        while (salt.length() < 18) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    public static String urlEncode(String value, String errorValue) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Throwable t) {
            Log.d(TAG, t.getMessage());
            return errorValue;
        }
    }
}
