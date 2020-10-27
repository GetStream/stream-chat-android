package com.getstream.sdk.chat.enums

import androidx.annotation.StringRes
import com.getstream.sdk.chat.Chat
import com.getstream.sdk.chat.ChatUX
import com.getstream.sdk.chat.R

internal enum class MessageInputType(@StringRes internal val labelId: Int) {
    EDIT_MESSAGE(R.string.stream_input_type_edit_message),
    ADD_FILE(R.string.stream_input_type_add_file),
    UPLOAD_MEDIA(R.string.stream_input_type_select_gallery),
    UPLOAD_FILE(R.string.stream_input_type_select_file),
    COMMAND(R.string.stream_input_type_command),
    MENTION(R.string.stream_input_type_auto_mention);
}

internal val MessageInputType.label: String
    get() = ChatUX.instance().strings.get(labelId)
