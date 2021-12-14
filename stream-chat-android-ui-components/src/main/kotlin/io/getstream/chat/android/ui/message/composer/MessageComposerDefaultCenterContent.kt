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
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.google.android.material.internal.TextWatcherAdapter
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.common.state.ValidationError
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

    /**
     * Default attachment preview factories. To
     */
    private var attachmentPreviewFactories: List<MessageComposerAttachmentPreviewFactory> =
        listOf(
            MessageComposerImageAttachmentPreviewFactory(),
            MessageComposerFileAttachmentPreviewFactory(),
        )

    /**
     * Adapter used to render attachments previews list
     */
    private val attachmentsAdapter: MessageComposerAttachmentsAdapter by lazy {
        MessageComposerAttachmentsAdapter { onAttachmentRemovedListener(it) }
    }

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
        attachmentsAdapter.viewFactories = this.attachmentPreviewFactories
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
        renderValidationErrors(state.validationErrors)
    }

    /**
     * Displays first of the validation errors received or clears error if there are no errors
     */
    private fun renderValidationErrors(validationErrors: List<ValidationError>) {
        if (validationErrors.isEmpty()) {
            binding.messageEditText.error = null
        } else {
            val errorMessage = when (val validationError = validationErrors.first()) {
                is ValidationError.MessageLengthExceeded -> {
                    context.getString(
                        R.string.stream_ui_message_composer_error_message_length,
                        validationError.maxMessageLength
                    )
                }
                is ValidationError.AttachmentCountExceeded -> {
                    context.getString(
                        R.string.stream_ui_message_composer_error_attachment_count,
                        validationError.maxAttachmentCount
                    )
                }
                is ValidationError.AttachmentSizeExceeded -> {
                    context.getString(
                        R.string.stream_ui_message_composer_error_file_size,
                        MediaStringUtil.convertFileSizeByteCount(validationError.maxAttachmentSize)
                    )
                }
            }
            binding.messageEditText.error = errorMessage
        }
    }

    /**
     * Allows to override default attachment previews. Useful when you want to add support for custom attachments previews.
     */
    public fun addAttachmentViewFactory(vararg factory: MessageComposerImageAttachmentPreviewFactory) {
        attachmentPreviewFactories = attachmentPreviewFactories + factory
        attachmentsAdapter.apply {
            viewFactories = attachmentPreviewFactories
            notifyDataSetChanged()
        }
    }
}

/**
 * [RecyclerView.Adapter] rendering attachments previews
 *
 * @property onAttachmentRemovedCallback Callback invoked when specific [Attachment] gets removed by the user
 */
internal class MessageComposerAttachmentsAdapter(
    private val onAttachmentRemovedCallback: (Attachment) -> Unit,
) : RecyclerView.Adapter<MessageComposerViewHolder>() {
    /**
     * List of attachments to render
     */
    private val attachments: MutableList<Attachment> = mutableListOf()

    /**
     * List of [MessageComposerAttachmentPreviewFactory] instances responsible for providing specific [View] for given [Attachment]
     */
    public var viewFactories: List<MessageComposerAttachmentPreviewFactory> = listOf()

    /**
     * Updates the list of currently displayed attachments. Re-renders all the attachments.
     */
    internal fun setAttachments(attachments: List<Attachment>) {
        this.attachments.apply {
            clear()
            addAll(attachments)
            notifyDataSetChanged()
        }
    }

    /**
     * Removes all the attachments
     */
    fun clear() {
        attachments.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageComposerViewHolder {
        return MessageComposerViewHolder(parent, viewFactories, { onAttachmentRemovedCallback(it) })
    }

    override fun onBindViewHolder(holder: MessageComposerViewHolder, position: Int) {
        holder.bindData(attachments[position])
    }

    override fun getItemCount(): Int = attachments.size
}

/**
 * [RecyclerView.ViewHolder] implementation responsible for rendering previews of various [Attachment] types
 */
internal class MessageComposerViewHolder(
    private val parent: ViewGroup,
    private val attachmentViewFactories: List<MessageComposerAttachmentPreviewFactory>,
    private val onAttachmentRemovedCallback: (Attachment) -> Unit,
    private val binding: StreamUiMessageComposerAttachmentContainerBinding =
        StreamUiMessageComposerAttachmentContainerBinding.inflate(parent.streamThemeInflater, parent, false),
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Picks the first factory capable of rendering given attachment and use it to create the preview.
     * Pushes the [View] created by factory into the attachment container
     */
    fun bindData(attachment: Attachment) {
        val attachmentContainer = binding.root
        val previewFactory = attachmentViewFactories.firstOrNull { it.canHandle(attachment) }
            ?: throw IllegalStateException("No MessageComposerAttachmentPreviewFactory instances found capable of handling attachment: $attachment")
        val view = previewFactory.createAttachmentPreview(parent, attachment) {
            onAttachmentRemovedCallback(attachment)
        }
        attachmentContainer.removeAllViews()
        attachmentContainer.addView(view)
    }
}

/**
 * Default factory providing preview for [Attachment] of image type, e.g. gallery image, photo
 */
public open class MessageComposerImageAttachmentPreviewFactory : MessageComposerAttachmentPreviewFactory {

    public override fun canHandle(attachment: Attachment): Boolean {
        return attachment.isMedia()
    }

    public override fun createAttachmentPreview(
        parent: ViewGroup,
        attachment: Attachment,
        onAttachmentRemovedCallback: (Attachment) -> Unit,
    ): View {
        val context = parent.context
        return StreamUiMediaAttachmentPreviewBinding.inflate(context.streamThemeInflater, parent, false)
            .apply {
                mediaImage.loadAttachmentThumb(attachment)
                removeButton.setOnClickListener { onAttachmentRemovedCallback(attachment) }
            }.root
    }
}

/**
 * Default factory providing preview for [Attachment] of file type
 */
public open class MessageComposerFileAttachmentPreviewFactory : MessageComposerAttachmentPreviewFactory {

    public override fun canHandle(attachment: Attachment): Boolean {
        return attachment.upload != null || attachment.uploadId != null
    }

    public override fun createAttachmentPreview(
        parent: ViewGroup,
        attachment: Attachment,
        onAttachmentRemovedCallback: (Attachment) -> Unit,
    ): View {
        val context = parent.context
        return StreamUiFileAttachmentPreviewBinding.inflate(context.streamThemeInflater, parent, false)
            .apply {
                fileThumb.loadAttachmentThumb(attachment)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong())
                fileTitle.text = attachment.title
                fileTitle.setTextColor(ContextCompat.getColor(context, R.color.stream_ui_black))
                removeButton.setOnClickListener { onAttachmentRemovedCallback(attachment) }
            }.root
    }
}

/**
 * Factory returning [View] instance which is a preview of a given [Attachment].
 * It is also providing information if the specific [Attachment] can be rendered with it.
 */
public interface MessageComposerAttachmentPreviewFactory {
    /**
     * @param attachment instance of [Attachment] to check if is compatible with this [MessageComposerAttachmentPreviewFactory] instance
     * @return true if the factory is able to provide preview for the given [Attachment], false otherwise
     */
    public fun canHandle(attachment: Attachment): Boolean

    /**
     * @param attachment [Attachment] for which the preview is returned
     * @return [View] which is supposed to be a preview of the [Attachment]
     */
    public fun createAttachmentPreview(
        parent: ViewGroup,
        attachment: Attachment,
        onAttachmentRemovedCallback: (Attachment) -> Unit,
    ): View
}
