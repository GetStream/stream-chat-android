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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.offline.extensions.globalState
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

@Suppress("TooManyFunctions")
internal class MessageOptionsDialogFragment : FullScreenDialogFragment() {

    private var _binding: StreamUiDialogMessageOptionsBinding? = null
    private val binding get() = _binding!!

    private val optionsMode: OptionsMode by lazy {
        requireArguments().getSerializable(ARG_OPTIONS_MODE) as OptionsMode
    }
    private val reactionsEnabled: Boolean by lazy {
        requireArguments().getBoolean(ARG_REACTIONS_ENABLED)
    }

    private val style by lazy { messageListViewStyle!! }

    private val viewHolderFactory by lazy { messageViewHolderFactory!! }
    private val decoratorProvider by lazy { messageOptionsDecoratorProvider!! }
    private val messageOptions by lazy { messageOptionItems }

    private val optionsOffset: Int by lazy { requireContext().getDimension(R.dimen.stream_ui_spacing_medium) }

    private val messageItem: MessageListItem.MessageItem by lazy {
        MessageListItem.MessageItem(
            message,
            positions = listOf(MessageListItem.Position.BOTTOM),
            isMine = message.user.id == ChatClient.instance().globalState.user.value?.id
        )
    }

    private lateinit var message: Message
    private lateinit var viewHolder: BaseMessageItemViewHolder<out MessageListItem>

    private var reactionClickHandler: ReactionClickHandler? = null
    private var userReactionClickHandler: UserReactionClickHandler? = null
    private var messageActionClickHandler: MessageActionClickHandler? = null

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
        consumeMessageArg()
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
        messageListViewStyle = null
        messageViewHolderFactory = null
        messageOptionsDecoratorProvider = null
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

    private fun consumeMessageArg() {
        messageArg?.let {
            message = it
            messageArg = null
        } ?: dismiss()
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
            if (reactionsEnabled) {
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
        viewHolderFactory.withDecoratorProvider(decoratorProvider) {
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
            ChatClient.instance().globalState.user.value?.let { user -> setMessage(message, user) }

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

            setMessageOptions(messageOptions, style)

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

    internal fun interface ReactionClickHandler {
        fun onReactionClick(message: Message, reactionType: String)
    }

    internal fun interface UserReactionClickHandler {
        fun onUserReactionClick(message: Message, user: User, reaction: Reaction)
    }

    internal fun interface MessageActionClickHandler {
        fun onMessageActionClick(messageAction: MessageAction)
    }

    internal enum class OptionsMode {
        MESSAGE_OPTIONS,
        REACTION_OPTIONS
    }

    companion object {
        const val TAG = "MessageOptionsDialogFragment"

        private const val ARG_OPTIONS_MODE = "optionsMode"
        private const val ARG_REACTIONS_ENABLED = "reactionsEnabled"

        internal var messageListViewStyle: MessageListViewStyle? = null

        var messageArg: Message? = null
        var messageViewHolderFactory: MessageListItemViewHolderFactory? = null
        var messageOptionsDecoratorProvider: MessageOptionsDecoratorProvider? = null
        var attachmentFactoryManager: AttachmentFactoryManager = AttachmentFactoryManager()
        var messageOptionItems: List<MessageOptionItem> = emptyList()

        fun newReactionOptionsInstance(
            message: Message,
            reactionsEnabled: Boolean,
            style: MessageListViewStyle,
            messageViewHolderFactory: MessageListItemViewHolderFactory,
            messageBackgroundFactory: MessageBackgroundFactory,
            attachmentFactoryManager: AttachmentFactoryManager,
            showAvatarPredicate: MessageListView.ShowAvatarPredicate,
        ): MessageOptionsDialogFragment {
            return newInstance(
                OptionsMode.REACTION_OPTIONS,
                message,
                reactionsEnabled,
                style,
                messageViewHolderFactory,
                messageBackgroundFactory,
                attachmentFactoryManager,
                showAvatarPredicate,
                emptyList(),
            )
        }

        fun newMessageOptionsInstance(
            message: Message,
            reactionsEnabled: Boolean,
            style: MessageListViewStyle,
            messageViewHolderFactory: MessageListItemViewHolderFactory,
            messageBackgroundFactory: MessageBackgroundFactory,
            attachmentFactoryManager: AttachmentFactoryManager,
            showAvatarPredicate: MessageListView.ShowAvatarPredicate,
            messageOptionItems: List<MessageOptionItem>,
        ): MessageOptionsDialogFragment {
            return newInstance(
                OptionsMode.MESSAGE_OPTIONS,
                message,
                reactionsEnabled,
                style,
                messageViewHolderFactory,
                messageBackgroundFactory,
                attachmentFactoryManager,
                showAvatarPredicate,
                messageOptionItems,
            )
        }

        private fun newInstance(
            optionsMode: OptionsMode,
            message: Message,
            reactionsEnabled: Boolean,
            style: MessageListViewStyle,
            messageViewHolderFactory: MessageListItemViewHolderFactory,
            messageBackgroundFactory: MessageBackgroundFactory,
            attachmentFactoryManager: AttachmentFactoryManager,
            showAvatarPredicate: MessageListView.ShowAvatarPredicate,
            messageOptionItems: List<MessageOptionItem>,
        ): MessageOptionsDialogFragment {
            this.messageListViewStyle = style
            this.attachmentFactoryManager = attachmentFactoryManager
            this.messageViewHolderFactory = messageViewHolderFactory
            this.messageOptionsDecoratorProvider = MessageOptionsDecoratorProvider(
                style.itemStyle,
                style.replyMessageStyle,
                messageBackgroundFactory,
                showAvatarPredicate
            )
            this.messageOptionItems = messageOptionItems
            return MessageOptionsDialogFragment().apply {
                arguments = bundleOf(
                    ARG_OPTIONS_MODE to optionsMode,
                    ARG_REACTIONS_ENABLED to reactionsEnabled,
                )
                // pass message via static field
                messageArg = message
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
}
