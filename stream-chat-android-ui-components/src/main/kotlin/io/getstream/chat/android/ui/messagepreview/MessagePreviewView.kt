package io.getstream.chat.android.ui.messagepreview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.databinding.StreamMessagePreviewItemBinding

public class MessagePreviewView : FrameLayout {

    private val binding = StreamMessagePreviewItemBinding.inflate(LayoutInflater.from(context), this, true)

    public constructor(context: Context) : super(context) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        parseAttrs(attrs)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
    }

    public fun setMessage(message: Message) {
        binding.avatarView.setUserData(message.user)
        binding.channelNameLabel.text = message.user.name
        binding.messageLabel.text = message.text
        binding.messageTimeLabel.text = DateFormatter.formatAsTimeOrDate(message.createdAt ?: message.createdLocallyAt)
    }
}
