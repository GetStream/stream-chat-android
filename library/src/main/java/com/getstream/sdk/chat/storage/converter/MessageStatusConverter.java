package com.getstream.sdk.chat.storage.converter;

import androidx.room.TypeConverter;

import com.getstream.sdk.chat.enums.MessageStatus;


public class MessageStatusConverter {
    @TypeConverter
    public static MessageStatus fromString(String value) {
        return value == null ? null : MessageStatus.valueOf(value);
    }

    @TypeConverter
    public static String statusToString(MessageStatus value) {
        return value == null ? null : value.label;
    }
}