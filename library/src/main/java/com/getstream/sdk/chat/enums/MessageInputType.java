package com.getstream.sdk.chat.enums;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;

public enum  MessageInputType {
    EDIT_MESSAGE(StreamChat.getContext().getString(R.string.stream_input_type_edit_message)),
    ADD_FILE(StreamChat.getContext().getString(R.string.stream_input_type_add_file)),
    UPLOAD_MEDIA(StreamChat.getContext().getString(R.string.stream_input_type_select_gallery)),
    UPLOAD_FILE(StreamChat.getContext().getString(R.string.stream_input_type_select_file)),
    COMMAND(StreamChat.getContext().getString(R.string.stream_input_type_command)),
    MENTION(StreamChat.getContext().getString(R.string.stream_input_type_auto_mention));

    public final String label;

    MessageInputType(String label) {
        this.label = label;
    }
}
