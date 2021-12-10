package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.images.loadVideoThumbnail
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.AttachmentConstants
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.google.android.material.internal.TextWatcherAdapter
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.isMedia
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.databinding.StreamUiFileAttachmentPreviewBinding
import io.getstream.chat.android.ui.databinding.StreamUiMediaAttachmentPreviewBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerAttachmentContainerBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultCenterContentBinding

/**
 * Default center content of [MessageComposerView]
 */
public class MessageComposerDefaultCenterContent : FrameLayout, MessageComposerChild {
    /**
     * Lambda invoked each time after text was changed
     */
    public var onTextChangedListener: (String) -> Unit = {}

    /**
     * Lambda invoked when clear button was clicked
     */
    public var onClearButtonClickListener: () -> Unit = {}

    /**
     * Callback invoked when attachment is removed
     */
    public var onAttachmentRemovedListener: (Attachment) -> Unit = {}

    private lateinit var binding: StreamUiMessageComposerDefaultCenterContentBinding

    private val attachmentsAdapter: MessageComposerAttachmentsAdapter =
        MessageComposerAttachmentsAdapter { onAttachmentRemovedListener(it) }

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    /**
     * Initial UI rendering and setting up callbacks
     */
    private fun init() {
        binding = StreamUiMessageComposerDefaultCenterContentBinding.inflate(streamThemeInflater, this)
        binding.messageEditText.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                onTextChangedListener(s.toString())
            }
        })
        binding.clearCommandButton.setOnClickListener {
            onClearButtonClickListener()
        }
        binding.attachmentsRecyclerView.adapter = attachmentsAdapter
    }

    /**
     * Re-rendering the UI according to the new state
     */
    override fun renderState(state: MessageInputState) {
        val isClearInputButtonVisible = state.inputValue.isNotEmpty()
        binding.clearCommandButton.isVisible = isClearInputButtonVisible

        binding.messageEditText.apply {
            val currentValue = text.toString()
            val newValue = state.inputValue
            if (newValue != currentValue) {
                setText(state.inputValue)
            }
        }

        attachmentsAdapter.setAttachments(state.attachments)

        binding.selectedAttachmentsContainer.isVisible = state.attachments.isNotEmpty()
    }

    /**
     * Allows to override default attachment previews. Useful when you want to add support for custom attachments previews.
     */
    public fun setAttachmentViewFactory(factory: DefaultMessageComposerAttachmentPreviewFactory) {
        attachmentsAdapter.viewFactory = factory
        attachmentsAdapter.notifyDataSetChanged()
    }
}

internal class MessageComposerAttachmentsAdapter(
    private val onAttachmentRemovedCallback: (Attachment) -> Unit,
) : RecyclerView.Adapter<MessageComposerViewHolder>() {
    private val attachments: MutableList<Attachment> = mutableListOf()

    public var viewFactory: DefaultMessageComposerAttachmentPreviewFactory =
        DefaultMessageComposerAttachmentPreviewFactory()

    internal fun setAttachments(attachments: List<Attachment>) {
        this.attachments.apply {
            clear()
            addAll(attachments)
            notifyDataSetChanged()
        }
    }

    fun removeItem(attachment: Attachment) {
        val position = attachments.indexOf(attachment)
        attachments -= attachment
        notifyItemRemoved(position)
    }

    fun clear() {
        attachments.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageComposerViewHolder {
        return MessageComposerViewHolder(parent, viewFactory, { onAttachmentRemovedCallback(it) })
    }

    override fun onBindViewHolder(holder: MessageComposerViewHolder, position: Int) {
        holder.bindData(attachments[position])
    }

    override fun getItemCount(): Int = attachments.size
}

internal class MessageComposerViewHolder(
    private val parent: ViewGroup,
    private val attachmentViewFactory: MessageComposerAttachmentPreviewFactory,
    private val onAttachmentRemovedCallback: (Attachment) -> Unit,
    private val binding: StreamUiMessageComposerAttachmentContainerBinding =
        StreamUiMessageComposerAttachmentContainerBinding.inflate(parent.streamThemeInflater, parent, false),
) : RecyclerView.ViewHolder(binding.root) {

    fun bindData(attachment: Attachment) {
        val attachmentContainer = binding.root
        val view = attachmentViewFactory.createViewForAttachment(parent, attachment) {
            onAttachmentRemovedCallback(attachment)
        }
        attachmentContainer.removeAllViews()
        attachmentContainer.addView(view)
    }
}

/**
 *
 */
public open class DefaultMessageComposerAttachmentPreviewFactory(
    private val attachmentMaxFileSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE, // TODO: pass from view model
) : MessageComposerAttachmentPreviewFactory {
    public override fun createViewForAttachment(
        parent: ViewGroup,
        attachment: Attachment,
        onAttachmentRemovedCallback: (Attachment) -> Unit,
    ): View {
        val context = parent.context
        return when {
            attachment.isMedia() -> {
                StreamUiFileAttachmentPreviewBinding.inflate(context.streamThemeInflater, parent, false)
                    .apply {
                        ivFileThumb.loadAttachmentThumb(attachment)
                        tvFileSize.text = MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong())
                        if (attachment.fileSize > attachmentMaxFileSize) {
                            tvFileTitle.text = context.getString(R.string.stream_ui_message_input_error_file_size)
                            tvFileTitle.setTextColor(ContextCompat.getColor(context, R.color.stream_ui_accent_red))
                        } else {
                            tvFileTitle.text = attachment.title
                            tvFileTitle.setTextColor(ContextCompat.getColor(context, R.color.stream_ui_black))
                        }
                        tvClose.setOnClickListener { onAttachmentRemovedCallback(attachment) }
                    }.root
            }
            attachment.upload != null -> {
                StreamUiMediaAttachmentPreviewBinding.inflate(context.streamThemeInflater, parent, false)
                    .apply {
                        if (attachment.type == ModelType.attach_video) {

                        } else {

                        }
                        removeButton.setOnClickListener { onAttachmentRemovedCallback(attachment) }
                    }.root
            }
            else -> {
                throw IllegalStateException("DefaultMessageComposerAttachmentPreviewFactory is not able to provide preview for this attachment. If this is a custom attachment please set custom factory for rendering its preview using MessageComposerDefaultCenterContent::setAttachmentViewFactory")
            }
        }
    }
}

/**
 *
 */
public interface MessageComposerAttachmentPreviewFactory {
    public fun createViewForAttachment(
        parent: ViewGroup,
        attachment: Attachment,
        onAttachmentRemovedCallback: (Attachment) -> Unit,
    ): View
}
