package io.getstream.chat.android.ui.messagepreview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.databinding.StreamUiMessagePreviewItemBinding
import io.getstream.chat.android.ui.utils.DateFormatter
import io.getstream.chat.android.ui.utils.formatDate

public class MessagePreviewView : FrameLayout {

    private val binding = StreamUiMessagePreviewItemBinding.inflate(LayoutInflater.from(context), this, true)

    private var _dateFormatter: DateFormatter? = null

    /**
     * The formatter used to display the time/date for the message.
     * If not set explicitly, a default implementation will be used.
     */
    public var dateFormatter: DateFormatter
        set(value) {
            _dateFormatter = value
        }
        get() {
            if (_dateFormatter == null) {
                _dateFormatter = DateFormatter.from(context)
            }
            return _dateFormatter!!
        }

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
        binding.messageTimeLabel.text = dateFormatter.formatDate(message.createdAt ?: message.createdLocallyAt)
    }
}
