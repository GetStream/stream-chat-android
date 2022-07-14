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

package io.getstream.chat.android.ui.message.list.options.message.internal

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.internal.FullScreenDialogFragment
import io.getstream.chat.android.ui.databinding.StreamUiDialogMessageOptionsBinding
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.message.list.adapter.internal.MessageListItemViewTypeMapper
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.CustomAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ImageAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.background.MessageBackgroundFactory
import io.getstream.chat.android.ui.message.list.options.message.MessageOptionItem

/**
 * An overlay with available message options to the selected message. Also, allows leaving a reaction.
 */
internal class MessageOptionsDialogFragment : FullScreenDialogFragment() {

    private var _binding: StreamUiDialogMessageOptionsBinding? = null
    private val binding get() = _binding!!

    /**
     * The options overlay type.
     */
    private lateinit var optionsMode: OptionsMode

    /**
     * The selected message
     */
    private lateinit var message: Message

    /**
     * Style for the dialog.
     */
    private lateinit var style: MessageListViewStyle

    /**
     * The factory class for message items.
     */
    private lateinit var messageListItemViewHolderFactory: MessageListItemViewHolderFactory

    /**
     * Creates a list of decorators for the message item.
     */
    private lateinit var messageOptionsDecoratorProvider: MessageOptionsDecoratorProvider

    /**
     * A factory for the attachments in the selected message.
     */
    private lateinit var attachmentFactoryManager: AttachmentFactoryManager

    /**
     * The list of message options to display.
     */
    private lateinit var messageOptionItems: List<MessageOptionItem>

    /**
     * A callback for clicks on reactions.
     */
    private var reactionClickHandler: ReactionClickHandler? = null

    /**
     * A callback for clicks on users who reacted to the message.
     */
    private var userReactionClickHandler: UserReactionClickHandler? = null

    /**
     * A callback for clicks on message actions.
     */
    private var messageActionClickHandler: MessageActionClickHandler? = null

    private val optionsOffset: Int by lazy { requireContext().getDimension(R.dimen.stream_ui_spacing_medium) }

    private val messageItem: MessageListItem.MessageItem by lazy {
        MessageListItem.MessageItem(
            message,
            positions = listOf(MessageListItem.Position.BOTTOM),
            isMine = message.user.id == ChatClient.instance().clientState.user.value?.id
        )
    }

    private lateinit var viewHolder: BaseMessageItemViewHolder<out MessageListItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return StreamUiDialogMessageOptionsBinding.inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isInitialized = ::optionsMode.isInitialized && ::message.isInitialized && ::style.isInitialized &&
            ::messageListItemViewHolderFactory.isInitialized && ::attachmentFactoryManager.isInitialized &&
            ::messageOptionItems.isInitialized
        if (savedInstanceState == null && isInitialized) {
            setupDialog()
        } else {
            dismiss()
        }
    }

    /**
     * Initializes the dialog.
     */
    private fun setupDialog() {
        setupDismissibleArea()
        setupEditReactionsView()
        setupMessageView()
        anchorReactionsViewToMessageView()
        when (optionsMode) {
            OptionsMode.MESSAGE_OPTIONS -> setupMessageOptions()
            OptionsMode.REACTION_OPTIONS -> setupUserReactionsView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        reactionClickHandler = null
        userReactionClickHandler = null
        messageActionClickHandler = null
    }

    fun setReactionClickHandler(reactionClickHandler: ReactionClickHandler) {
        this.reactionClickHandler = reactionClickHandler
    }

    fun setUserReactionClickHandler(userReactionClickHandler: UserReactionClickHandler) {
        this.userReactionClickHandler = userReactionClickHandler
    }

    fun setMessageActionClickHandler(messageActionClickHandler: MessageActionClickHandler) {
        this.messageActionClickHandler = messageActionClickHandler
    }

    private fun setupDismissibleArea() {
        binding.containerView.setOnClickListener {
            dismiss()
        }
        binding.messageContainer.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(style.optionsOverlayDimColor))
    }

    private fun setupEditReactionsView() {
        with(binding.editReactionsView) {
            applyStyle(style.itemStyle.editReactionsViewStyle)
            if (style.reactionsEnabled) {
                setMessage(message, messageItem.isMine)
                setReactionClickListener {
                    reactionClickHandler?.onReactionClick(message, it)
                    dismiss()
                }
            } else {
                isVisible = false
            }
        }
    }

    private fun setupMessageView() {
        messageListItemViewHolderFactory.withDecoratorProvider(messageOptionsDecoratorProvider) {
            viewHolder = it.createViewHolder(
                binding.messageContainer,
                MessageListItemViewTypeMapper.getViewTypeValue(messageItem, attachmentFactoryManager)
            ).also { viewHolder ->
                viewHolder.itemView.setOnClickListener {
                    dismiss()
                }
                binding.messageContainer.addView(
                    viewHolder.itemView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                )
                viewHolder.bindListItem(messageItem)
            }
        }
    }

    private fun setupUserReactionsView() {
        with(binding.userReactionsView) {
            isVisible = true
            configure(style)
            ChatClient.instance().clientState.user.value?.let { user -> setMessage(message, user) }

            setOnUserReactionClickListener { user, reaction ->
                userReactionClickHandler?.let {
                    it.onUserReactionClick(message, user, reaction)
                    dismiss()
                }
            }
        }
    }

    private fun setupMessageOptions() {
        with(binding.messageOptionsView) {
            isVisible = true

            updateLayoutParams<LinearLayout.LayoutParams> {
                gravity = if (messageItem.isMine) Gravity.END else Gravity.START
            }

            updateLayoutParams<ViewGroup.MarginLayoutParams> {
                if (messageItem.isMine) {
                    marginEnd = style.itemStyle.messageEndMargin + optionsOffset
                } else {
                    marginStart = style.itemStyle.messageStartMargin + optionsOffset
                }
            }

            setMessageOptions(messageOptionItems, style)

            setMessageActionClickListener { messageAction ->
                messageActionClickHandler?.onMessageActionClick(messageAction)
                dismiss()
            }
        }
    }

    /**
     * Positions the reactions bubble near the message bubble according to the design.
     */
    private fun anchorReactionsViewToMessageView() {
        val reactionsWidth = requireContext().getDimension(R.dimen.stream_ui_edit_reactions_total_width)
        val reactionsOffset = requireContext().getDimension(R.dimen.stream_ui_edit_reactions_horizontal_offset)

        when (val viewHolder = viewHolder) {
            is MessagePlainTextViewHolder -> viewHolder.binding.messageContainer
            is CustomAttachmentsViewHolder -> viewHolder.binding.messageContainer
            is LinkAttachmentsViewHolder -> viewHolder.binding.messageContainer
            is FileAttachmentsViewHolder -> viewHolder.binding.messageContainer
            is GiphyAttachmentViewHolder -> viewHolder.binding.messageContainer
            is ImageAttachmentViewHolder -> viewHolder.binding.messageContainer
            else -> null
        }?.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            with(binding) {
                val maxTranslation = messageContainer.width / 2 - reactionsWidth / 2
                editReactionsView.translationX = if (messageItem.isMine) {
                    left - messageContainer.width / 2 - reactionsOffset
                } else {
                    right - messageContainer.width / 2 + reactionsOffset
                }.coerceIn(-maxTranslation, maxTranslation).toFloat()
            }
        }
    }

    /**
     * Executes the given [block] function on [MessageListItemViewHolderFactory] with
     * the provided decorators and then resets them to the previous value.
     *
     * @param messageOptionsDecoratorProvider The temporary provider of item decorators.
     * @param block The block of code that will be invoked with the modified item factory.
     */
    private inline fun MessageListItemViewHolderFactory.withDecoratorProvider(
        messageOptionsDecoratorProvider: MessageOptionsDecoratorProvider,
        block: (MessageListItemViewHolderFactory) -> Unit,
    ) {
        val tempDecoratorProvider = decoratorProvider

        decoratorProvider = messageOptionsDecoratorProvider
        try {
            block(this)
        } finally {
            decoratorProvider = tempDecoratorProvider
        }
    }

    /**
     * A listener for reaction clicks.
     */
    internal fun interface ReactionClickHandler {
        fun onReactionClick(message: Message, reactionType: String)
    }

    /**
     * A listener for clicks on users who left the reactions.
     */
    internal fun interface UserReactionClickHandler {
        fun onUserReactionClick(message: Message, user: User, reaction: Reaction)
    }

    /**
     * A listener for message option clicks.
     */
    internal fun interface MessageActionClickHandler {
        fun onMessageActionClick(messageAction: MessageAction)
    }

    /**
     * Represents the type of message options dialogs.
     */
    internal enum class OptionsMode {
        MESSAGE_OPTIONS,
        REACTION_OPTIONS
    }

    companion object {
        const val TAG = "MessageOptionsDialogFragment"

        /**
         * Creates a new instance of [MessageOptionsDialogFragment].
         *
         * @param optionsMode The type of options dialog.
         * @param message The selected message.
         * @param style The style for the dialog.
         * @param messageListItemViewHolderFactory The factory class for message items.
         * @param messageBackgroundFactory The factory for message background.
         * @param attachmentFactoryManager The factory for the attachments in the selected message.
         * @param showAvatarPredicate If an avatar should be shown for the message.
         * @param messageOptionItems The list of message options to display.
         */
        fun newInstance(
            optionsMode: OptionsMode,
            message: Message,
            style: MessageListViewStyle,
            messageListItemViewHolderFactory: MessageListItemViewHolderFactory,
            messageBackgroundFactory: MessageBackgroundFactory,
            attachmentFactoryManager: AttachmentFactoryManager,
            showAvatarPredicate: MessageListView.ShowAvatarPredicate,
            messageOptionItems: List<MessageOptionItem>,
        ): MessageOptionsDialogFragment {
            return MessageOptionsDialogFragment().also {
                it.optionsMode = optionsMode
                it.message = message
                it.style = style
                it.attachmentFactoryManager = attachmentFactoryManager
                it.messageListItemViewHolderFactory = messageListItemViewHolderFactory
                it.messageOptionsDecoratorProvider = MessageOptionsDecoratorProvider(
                    style.itemStyle,
                    style.replyMessageStyle,
                    messageBackgroundFactory,
                    showAvatarPredicate
                )
                it.messageOptionItems = messageOptionItems
            }
        }
    }
}
