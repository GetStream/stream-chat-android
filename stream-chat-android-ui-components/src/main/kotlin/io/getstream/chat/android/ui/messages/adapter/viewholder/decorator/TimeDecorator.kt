package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import android.widget.TextView
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrThrow

internal class TimeDecorator(private val dateFormatter: DateFormatter) : BaseDecorator() {

    private fun setupTime(textView: TextView, data: MessageListItem.MessageItem) {
        textView.text = dateFormatter.formatTime(data.message.getCreatedAtOrThrow())
    }
}
