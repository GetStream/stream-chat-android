package io.getstream.chat.docs.cookbook.ui

import android.content.Context
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.docs.R

/**
 * @see <a href="https://github.com/GetStream/stream-chat-android/wiki/UI-Cookbook#ui-customisation">Message List View</a>
 */
class MessageListView {
    fun customiseMessageListViewProgrammatically(context: Context) {
        TransformStyle.messageInputStyleTransformer = StyleTransformer { viewStyle ->
            viewStyle.copy(
                messageInputTextColor = ContextCompat.getColor(context, R.color.stream_ui_white)
            )
        }
    }
}
