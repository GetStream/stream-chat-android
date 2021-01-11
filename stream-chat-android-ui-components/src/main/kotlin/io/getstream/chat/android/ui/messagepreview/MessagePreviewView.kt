package io.getstream.chat.android.ui.messagepreview

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.widget.FrameLayout
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.utils.formatDate
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMessagePreviewItemBinding
import io.getstream.chat.android.ui.utils.extensions.bold
import io.getstream.chat.android.ui.utils.extensions.singletonList

public class MessagePreviewView : FrameLayout {

    private val binding = StreamUiMessagePreviewItemBinding.inflate(context.inflater, this, true)

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
        val fileAttachmentsNames = message.attachments
            .filter { it.type == ModelType.attach_file }
            .mapNotNull { attachment ->
                attachment.title ?: attachment.name
            }

        if (fileAttachmentsNames.isNotEmpty()) {
            return context.getString(R.string.stream_ui_message_file, fileAttachmentsNames.joinToString())
        }

        if (currentUserMention != null) {
            // bold mentions of the current user
            return message.text.trim().bold(currentUserMention.singletonList(), ignoreCase = true)
        }

        return message.text.trim()
    }
}
