package io.getstream.chat.android.ui.messagepreview

import android.content.Context
import android.content.res.Configuration
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.formatDate
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMessagePreviewItemBinding
import io.getstream.chat.android.ui.utils.extensions.bold
import io.getstream.chat.android.ui.utils.extensions.singletonList

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
        binding.contentRoot.setBackgroundColor(getItemBackgroundColor())

        parseAttrs(attrs)
    }

    private fun getItemBackgroundColor(): Int {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> R.color.stream_ui_alabaster
            Configuration.UI_MODE_NIGHT_YES -> R.color.stream_ui_black
            else -> R.color.stream_ui_alabaster
        }.let { colorRes ->
            ContextCompat.getColor(context, colorRes)
        }
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
    }

    public fun setMessage(message: Message, currentUserMention: String? = null) {
        binding.avatarView.setUserData(message.user)
        binding.senderNameLabel.text = formatChannelName(message)
        binding.messageLabel.text = formatMessagePreview(message, currentUserMention)
        binding.messageTimeLabel.text = dateFormatter.formatDate(message.createdAt ?: message.createdLocallyAt)
    }

    private fun formatChannelName(message: Message): CharSequence {
        val channel = message.channelInfo
        return if (channel?.name != null && channel.memberCount > 2) {
            Html.fromHtml(
                context.getString(
                    R.string.stream_ui_message_sender_title_in_channel,
                    message.user.name,
                    channel.name,
                )
            )
        } else {
            message.user.name.bold()
        }
    }

    private fun formatMessagePreview(message: Message, currentUserMention: String?): CharSequence {
        val attachmentsNames = message.attachments
            .mapNotNull { attachment ->
                attachment.title ?: attachment.name
            }

        if (attachmentsNames.isNotEmpty()) {
            return context.getString(R.string.stream_ui_message_file, attachmentsNames.joinToString())
        }

        if (currentUserMention != null) {
            // bold mentions of the current user
            return message.text.trim().bold(currentUserMention.singletonList())
        }

        return message.text.trim()
    }
}
