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

package io.getstream.chat.android.ui.feature.messages.composer.content

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Editable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultCenterContentBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewFactoryManager
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewViewHolder
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Represents the default content shown at the center of [MessageComposerView].
 */
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
     * Text change listener invoked after each input change.
     */
    public var textInputChangeListener: (String) -> Unit = {}

    /**
     * Click listener for the remove attachment button.
     */
    public var attachmentRemovalListener: (Attachment) -> Unit = {}

    /**
     * Adapter used to render attachments previews list.
     */
    private val attachmentsAdapter: AttachmentPreviewAdapter by lazy {
        AttachmentPreviewAdapter(
            factoryManager = ChatUI.attachmentPreviewFactoryManager,
            style = style
        ) { attachment ->
            attachmentRemovalListener(attachment)
        }
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
    }

    /**
     * Initializes the content view with [MessageComposerContext].
     *
     * @param messageComposerContext The context of this [MessageComposerView].
     */
    override fun attachContext(messageComposerContext: MessageComposerContext) {
        this.style = messageComposerContext.style

        binding.attachmentsRecyclerView.adapter = attachmentsAdapter
        binding.messageInputContainer.background = style.messageInputBackgroundDrawable
        binding.messageEditText.setTextStyle(style.messageInputTextStyle)
        binding.messageEditText.isVerticalScrollBarEnabled = style.messageInputScrollbarEnabled
        binding.messageEditText.isVerticalFadingEdgeEnabled = style.messageInputScrollbarFadingEnabled
        binding.messageEditText.inputType = style.messageInputInputType
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            style.messageInputCursorDrawable?.let {
                binding.messageEditText.textCursorDrawable = it
            }
        }
    }

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    override fun renderState(state: MessageComposerState) {
        renderTextInputState(state)
        renderReplyState(state)
        renderAttachmentState(state)
    }

    /**
     * Renders the state of the text input field.
     *
     * @param state The state that will be used to render the updated UI.
     */
    private fun renderTextInputState(state: MessageComposerState) {
        binding.messageEditText.apply {
            val currentValue = text.toString()
            val newValue = state.inputValue
            if (newValue != currentValue) {
                setText(state.inputValue)
                // Place the cursor at the end
                setSelection(length())
            }
        }

        val canSendMessage = state.ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
        if (canSendMessage) {
            binding.messageEditText.isEnabled = true
            binding.messageEditText.hint = style.messageInputTextStyle.hint
            binding.messageEditText.maxLines = style.messageInputMaxLines
        } else {
            binding.messageEditText.isEnabled = false
            binding.messageEditText.hint = style.messageInputCannotSendHintText
            // Avoid multiline text input due to the long hint above
            binding.messageEditText.maxLines = 1
        }
    }

    /**
     * Renders a bubble with the message we are replying to.
     *
     * @param state The state that will be used to render the updated UI.
     */
    private fun renderReplyState(state: MessageComposerState) {
        val action = state.action
        if (action is Reply) {
            val quotedMessage = action.message
            binding.messageReplyView.setMessage(
                quotedMessage,
                ChatUI.currentUserProvider.getCurrentUser()?.id == quotedMessage.user.id,
                null,
            )
            binding.messageReplyView.isVisible = true
        } else {
            binding.messageReplyView.isVisible = false
        }
    }

    /**
     * Renders previews for the selected attachments.
     *
     * @param state The state that will be used to render the updated UI.
     */
    private fun renderAttachmentState(state: MessageComposerState) {
        binding.attachmentsRecyclerView.isVisible = state.attachments.isNotEmpty()
        attachmentsAdapter.setAttachments(state.attachments)
    }
}

/**
 * [RecyclerView.Adapter] responsible for displaying attachment previews in a RecyclerView.
 *
 * @param factoryManager A manager for registered attachment preview factories.
 * @param attachmentRemovalListener Click listener for the remove attachment button.
 */
private class AttachmentPreviewAdapter(
    private val factoryManager: AttachmentPreviewFactoryManager,
    private val style: MessageComposerViewStyle,
    private val attachmentRemovalListener: (Attachment) -> Unit,
) : RecyclerView.Adapter<AttachmentPreviewViewHolder>() {

    /**
     * The attachments that will be displayed in the list.
     */
    private val attachments = mutableListOf<Attachment>()

    /**
     * Replaces all the items and notifies that the data set has changed.
     *
     * @param attachments
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setAttachments(attachments: List<Attachment>) {
        this.attachments.clear()
        this.attachments.addAll(attachments)
        notifyDataSetChanged()
    }

    /**
     * Creates and instantiates a new instance of [AttachmentPreviewViewHolder].
     *
     * @param parentView The parent container.
     * @return An instance of attachment preview ViewHolder.
     */
    override fun onCreateViewHolder(parentView: ViewGroup, viewType: Int): AttachmentPreviewViewHolder {
        return factoryManager.onCreateViewHolder(
            parentView = parentView,
            viewType = viewType,
            attachmentRemovalListener = attachmentRemovalListener,
            style = style
        )
    }

    /**
     * Binds the created View in the ViewHolder to the attachment data.
     *
     * @param holder The ViewHolder which should be updated to represent the attachment.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: AttachmentPreviewViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    /**
     * Returns the view type associated with the factory that will handle this attachment.
     *
     * @param position The position of the item in the list.
     */
    override fun getItemViewType(position: Int): Int {
        return factoryManager.getItemViewType(attachments[position])
    }

    /**
     * Returns the number of attachment items.
     *
     * @return The number of attachments.
     */
    override fun getItemCount(): Int = attachments.size
}
