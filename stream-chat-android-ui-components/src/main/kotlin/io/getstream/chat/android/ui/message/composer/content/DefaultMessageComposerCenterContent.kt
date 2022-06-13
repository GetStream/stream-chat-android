/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.message.composer.content

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.databinding.StreamUiFileAttachmentPreviewBinding
import io.getstream.chat.android.ui.databinding.StreamUiMediaAttachmentPreviewBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerAttachmentContainerBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultCenterContentBinding
import io.getstream.chat.android.ui.message.composer.MessageComposerContext
import io.getstream.chat.android.ui.message.composer.MessageComposerView
import io.getstream.chat.android.ui.message.composer.MessageComposerViewStyle

/**
 * Represents the default content shown at the center of [MessageComposerView].
 */
@ExperimentalStreamChatApi
public class DefaultMessageComposerCenterContent : FrameLayout, MessageComposerContent {
    /**
     * Generated binding class for the XML layout.
     */
    private lateinit var binding: StreamUiMessageComposerDefaultCenterContentBinding

    /**
     * The style for [MessageComposerView].
     */
    private lateinit var style: MessageComposerViewStyle

    /**
     * Text change listener invoked each time after text was changed.
     */
    public var textInputChangeListener: (String) -> Unit = {}

    /**
     * Click listener for the clear input button.
     */
    public var clearInputButtonClickListener: () -> Unit = {}

    /**
     * Click listener for the remove attachment button.
     */
    public var attachmentRemovalListener: (Attachment) -> Unit = {}

    /**
     * Default attachment preview factories.
     */
    private var attachmentPreviewFactories: List<MessageComposerAttachmentPreviewFactory> =
        listOf(
            MessageComposerImageAttachmentPreviewFactory(),
            MessageComposerFileAttachmentPreviewFactory(),
        )

    /**
     * Adapter used to render attachments previews list.
     */
    private val attachmentsAdapter: MessageComposerAttachmentsAdapter by lazy {
        MessageComposerAttachmentsAdapter { attachmentRemovalListener(it) }
    }

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init()
    }

    /**
     * Initializes the initial layout of the view.
     */
    private fun init() {
        binding = StreamUiMessageComposerDefaultCenterContentBinding.inflate(streamThemeInflater, this)
        binding.messageEditText.doAfterTextChanged { editable: Editable? ->
            textInputChangeListener(editable?.toString() ?: "")
        }
        binding.clearCommandButton.setOnClickListener {
            clearInputButtonClickListener()
        }
        attachmentsAdapter.viewFactories = this.attachmentPreviewFactories
        binding.attachmentsRecyclerView.adapter = attachmentsAdapter
    }

    /**
     * Initializes the content view with with [MessageComposerContext].
     *
     * @param messageComposerContext The context of this [MessageComposerView].
     */
    override fun attachContext(messageComposerContext: MessageComposerContext) {
        this.style = messageComposerContext.style

        binding.messageEditText.isVerticalScrollBarEnabled = style.messageInputScrollbarEnabled
        binding.messageEditText.isVerticalFadingEdgeEnabled = style.messageInputScrollbarFadingEnabled
    }

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    override fun renderState(state: MessageComposerState) {
        binding.messageEditText.apply {
            val currentValue = text.toString()
            val newValue = state.inputValue
            if (newValue != currentValue) {
                setText(state.inputValue)
                // placing cursor at the end of the text
                setSelection(length())
            }
        }

        val canSendMessage = state.ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)

        if (canSendMessage) {
            binding.messageEditText.isEnabled = true
            binding.messageEditText.hint = context.getString(R.string.stream_ui_message_input_hint)
        } else {
            binding.messageEditText.isEnabled = false
            binding.messageEditText.hint = context.getString(R.string.stream_ui_message_cannot_send_messages_hint)
        }

        attachmentsAdapter.setAttachments(state.attachments)

        val action = state.action
        if (action is Reply) {
            val message = action.message
            binding.messageReplyView.setMessage(
                message,
                ChatUI.currentUserProvider.getCurrentUser()?.id == message.user.id,
                null,
            )
            binding.messageReplyView.isVisible = true
        } else {
            binding.messageReplyView.isVisible = false
        }

        binding.selectedAttachmentsContainer.isVisible = state.attachments.isNotEmpty()
    }

    /**
     * Allows overriding default attachment previews. Useful when you want to add support for custom attachments previews.
     *
     * @param factory Implementation of [MessageComposerAttachmentPreviewFactory] interface.
     */
    public fun addAttachmentViewFactory(vararg factory: MessageComposerAttachmentPreviewFactory) {
        attachmentPreviewFactories = factory.toList() + attachmentPreviewFactories
        attachmentsAdapter.apply {
            viewFactories = attachmentPreviewFactories
            notifyDataSetChanged()
        }
    }
}

/**
 * [RecyclerView.Adapter] rendering attachments previews.
 *
 * @property attachmentRemovalListener Callback invoked when specific [Attachment] gets removed by the user.
 */
private class MessageComposerAttachmentsAdapter(
    private inline val attachmentRemovalListener: (Attachment) -> Unit,
) : RecyclerView.Adapter<MessageComposerAttachmentViewHolder>() {
    /**
     * List of attachments to render.
     */
    private val attachments: MutableList<Attachment> = mutableListOf()

    /**
     * List of [MessageComposerAttachmentPreviewFactory] instances responsible for providing specific [View] for given [Attachment].
     */
    var viewFactories: List<MessageComposerAttachmentPreviewFactory> = listOf()

    /**
     * Updates the list of currently displayed attachments. Re-renders all the attachments.
     */
    fun setAttachments(attachments: List<Attachment>) {
        this.attachments.apply {
            clear()
            addAll(attachments)
            notifyDataSetChanged()
        }
    }

    /**
     * Removes all the attachments.
     */
    fun clear() {
        attachments.clear()
        notifyDataSetChanged()
    }

    /**
     * Creates and instantiates a new instance of [MessageComposerAttachmentViewHolder].
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new [CommandViewHolder] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageComposerAttachmentViewHolder {
        return MessageComposerAttachmentViewHolder(
            StreamUiMessageComposerAttachmentContainerBinding.inflate(parent.streamThemeInflater, parent, false),
            viewFactories
        ) { attachmentRemovalListener(it) }
    }

    /**
     * Calls [MessageComposerAttachmentViewHolder] to update its [MessageComposerAttachmentViewHolder.itemView].
     */
    override fun onBindViewHolder(holder: MessageComposerAttachmentViewHolder, position: Int) {
        holder.bindData(attachments[position])
    }

    /**
     * @return size of the attachments list.
     */
    override fun getItemCount(): Int = attachments.size
}

/**
 * [RecyclerView.ViewHolder] implementation responsible for rendering previews of various [Attachment] types.
 */
private class MessageComposerAttachmentViewHolder(
    private val binding: StreamUiMessageComposerAttachmentContainerBinding,
    private val attachmentViewFactories: List<MessageComposerAttachmentPreviewFactory>,
    private inline val onRemoveAttachment: (Attachment) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Picks the first factory capable of rendering given attachment and use it to create the preview.
     * Pushes the [View] created by factory into the attachment container.
     */
    fun bindData(attachment: Attachment) {
        val attachmentContainer = binding.root
        val previewFactory = attachmentViewFactories.firstOrNull { it.canHandle(attachment) }
            ?: throw IllegalStateException("No MessageComposerAttachmentPreviewFactory instances found capable of handling attachment: $attachment")
        val view = previewFactory.createAttachmentPreview(binding.root, attachment) { onRemoveAttachment(attachment) }
        attachmentContainer.removeAllViews()
        attachmentContainer.addView(view)
    }
}

/**
 * Default factory providing preview for [Attachment] of image type, e.g. gallery image, photo.
 */
public open class MessageComposerImageAttachmentPreviewFactory : MessageComposerAttachmentPreviewFactory {

    /**
     * @return true if given attachment is of image or giphy type, false otherwise.
     */
    public override fun canHandle(attachment: Attachment): Boolean {
        return attachment.type in listOf(ModelType.attach_image, ModelType.attach_giphy)
    }

    /**
     * @return Image attachment view.
     */
    public override fun createAttachmentPreview(
        parent: ViewGroup,
        attachment: Attachment,
        attachmentRemovalListener: (Attachment) -> Unit,
    ): View {
        val context = parent.context
        return StreamUiMediaAttachmentPreviewBinding.inflate(context.streamThemeInflater, parent, false)
            .apply {
                val shapeAppearanceModel = mediaImage.shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(context.resources.getDimension(R.dimen.stream_ui_message_composer_attachment_corner_radius))
                    .build()
                mediaImage.shapeAppearanceModel = shapeAppearanceModel
                mediaImage.loadAttachmentThumb(attachment)
                removeButton.setOnClickListener { attachmentRemovalListener(attachment) }
            }.root
    }
}

/**
 * Default factory providing preview for [Attachment] of file type.
 */
public open class MessageComposerFileAttachmentPreviewFactory : MessageComposerAttachmentPreviewFactory {

    /**
     * @return true if given attachment is of file type, false otherwise.
     */
    public override fun canHandle(attachment: Attachment): Boolean =
        attachment.upload != null || attachment.uploadId != null

    /**
     * @return File attachment view.
     */
    public override fun createAttachmentPreview(
        parent: ViewGroup,
        attachment: Attachment,
        attachmentRemovalListener: (Attachment) -> Unit,
    ): View {
        val context = parent.context
        return StreamUiFileAttachmentPreviewBinding.inflate(context.streamThemeInflater, parent, false)
            .apply {
                fileThumb.loadAttachmentThumb(attachment)
                fileSize.text = MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong())
                fileTitle.text = attachment.title
                fileTitle.setTextColor(ContextCompat.getColor(context, R.color.stream_ui_black))
                removeButton.setOnClickListener { attachmentRemovalListener(attachment) }
            }.root
    }
}

/**
 * Factory returning [View] instance which is a preview of a given [Attachment].
 * It is also providing information if the specific [Attachment] can be rendered with it.
 */
public interface MessageComposerAttachmentPreviewFactory {
    /**
     * @param attachment instance of [Attachment] to check if is compatible with this [MessageComposerAttachmentPreviewFactory] instance.
     *
     * @return true if the factory is able to provide preview for the given [Attachment], false otherwise.
     */
    public fun canHandle(attachment: Attachment): Boolean

    /**
     * @param attachment [Attachment] for which the preview is returned.
     *
     * @return [View] which is supposed to be a preview of the [Attachment].
     */
    public fun createAttachmentPreview(
        parent: ViewGroup,
        attachment: Attachment,
        attachmentRemovalListener: (Attachment) -> Unit,
    ): View
}
