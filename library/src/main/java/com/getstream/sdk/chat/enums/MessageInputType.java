package com.getstream.sdk.chat.enums;

import androidx.annotation.StringRes;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;

public enum MessageInputType {
    EDIT_MESSAGE(R.string.stream_input_type_edit_message),
    ADD_FILE(R.string.stream_input_type_add_file),
    UPLOAD_MEDIA(R.string.stream_input_type_select_gallery),
    UPLOAD_FILE(R.string.stream_input_type_select_file),
    COMMAND(R.string.stream_input_type_command),
    MENTION(R.string.stream_input_type_auto_mention);

    @StringRes
    private final int labelId;

    public String getLabel() {
        return StreamChat.getContext().getString(labelId);
    }

    MessageInputType(@StringRes int labelId) {
        this.labelId = labelId;
    }
}
