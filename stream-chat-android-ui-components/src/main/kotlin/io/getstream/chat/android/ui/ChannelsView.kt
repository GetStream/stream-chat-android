package io.getstream.chat.android.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.name

public class ChannelsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    public fun setChannels(channels: List<Channel>) {
        text = channels.joinToString(separator = "\n") { it.name }
    }
}
