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

package io.getstream.chat.android.ui.feature.messages.list.options.message

import android.content.Context
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
import androidx.core.view.updateMarginsRelative
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.databinding.StreamUiDialogMessageOptionsBinding
import io.getstream.chat.android.ui.feature.messages.list.DefaultShowAvatarPredicate
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.DecoratorProvider
import io.getstream.chat.android.ui.feature.messages.list.background.MessageBackgroundFactory
import io.getstream.chat.android.ui.feature.messages.list.background.MessageBackgroundFactoryImpl
import io.getstream.chat.android.ui.feature.messages.list.options.message.internal.MessageOptionsDecoratorProvider
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.isRtlLayout
import io.getstream.chat.android.ui.widgets.FullScreenDialogFragment

/**
 * An overlay with available message options to the selected message. Also, allows leaving a reaction.
 */
public class MessageOptionsDialogFragment : FullScreenDialogFragment() {

    private var _binding: StreamUiDialogMessageOptionsBinding? = null
    private val binding get() = _binding!!

    /**
     * The options dialog type.
     */
    private lateinit var optionsDialogType: OptionsDialogType

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
     * The list of message options to display.
     */
    private lateinit var messageOptionItems: List<MessageOptionItem>

    /**
     * A callback for clicks on reactions.
     */
    private var reactionClickListener: ReactionClickListener? = null

    /**
     * A callback for clicks on users who reacted to the message.
     */
    private var userReactionClickListener: UserReactionClickListener? = null

    /**
     * A callback for clicks on message options.
     */
    private var messageOptionClickListener: MessageOptionClickListener? = null

    /**
     * The current user provider.
     */
    private var getCurrentUser: () -> User? = { null }

    private val optionsOffset: Int by lazy { requireContext().getDimension(R.dimen.stream_ui_spacing_medium) }

    private val messageItem: MessageListItem.MessageItem by lazy {
        MessageListItem.MessageItem(
            message,
            positions = listOf(MessagePosition.BOTTOM),
            isMine = message.user.id == ChatUI.currentUserProvider.getCurrentUser()?.id,
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
        val isInitialized = ::optionsDialogType.isInitialized && ::message.isInitialized && ::style.isInitialized &&
            ::messageListItemViewHolderFactory.isInitialized && ::messageOptionItems.isInitialized
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
        when (optionsDialogType) {
            OptionsDialogType.MESSAGE_OPTIONS -> setupMessageOptions()
            OptionsDialogType.REACTION_OPTIONS -> setupUserReactionsView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        reactionClickListener = null
        userReactionClickListener = null
        messageOptionClickListener = null
    }

    /**
     * Allows clients to set a click listener for reactions in the reaction butbble.
     *
     * @param reactionClickListener The callback to be invoked on reaction item click.
     */
    public fun setReactionClickListener(reactionClickListener: ReactionClickListener) {
        this.reactionClickListener = reactionClickListener
    }

    /**
     * Allows clients to set a click listener for reactions in the reaction butbble.
     *
     * @param userReactionClickListener The callback to be invoked on user reaction item click.
     */
    public fun setUserReactionClickListener(userReactionClickListener: UserReactionClickListener) {
        this.userReactionClickListener = userReactionClickListener
    }

    /**
     * Allows clients to set a click listener for message option items.
     *
     * @param messageOptionClickListener The callback to be invoked on message option item click.
     */
    public fun setMessageOptionClickListener(messageOptionClickListener: MessageOptionClickListener) {
        this.messageOptionClickListener = messageOptionClickListener
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

            val isMessageSynced = message.syncStatus == SyncStatus.COMPLETED
            if (style.reactionsEnabled && (isMessageSynced || style.showReactionsForUnsentMessages)) {
                setMessage(message, messageItem.isMine)
                setReactionClickListener {
                    reactionClickListener?.onReactionClick(message, it)
                    dismiss()
                }
            } else {
                isVisible = false
            }

            val params = (layoutParams as ViewGroup.MarginLayoutParams)
            params.updateMarginsRelative(
                top = style.optionsOverlayEditReactionsMarginTop,
                bottom = style.optionsOverlayEditReactionsMarginBottom,
                start = style.optionsOverlayEditReactionsMarginStart,
                end = style.optionsOverlayEditReactionsMarginEnd,
            )
            (params as? LinearLayout.LayoutParams)?.gravity = if (messageItem.isMine) Gravity.END else Gravity.START
        }
    }

    private fun setupMessageView() {
        messageListItemViewHolderFactory.withDecoratorProvider(messageOptionsDecoratorProvider) {
            viewHolder = it.createViewHolder(
                binding.messageContainer,
                it.getItemViewType(messageItem),
            ).also { viewHolder ->
                viewHolder.itemView.setOnClickListener {
                    dismiss()
                }
                binding.messageContainer.addView(
                    viewHolder.itemView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                    ),
                )
                viewHolder.bindListItem(messageItem, MessageListItemPayloadDiff.FULL)
            }
        }
    }

    private fun setupUserReactionsView() {
        with(binding.userReactionsView) {
            isVisible = true
            configure(style)
            ChatUI.currentUserProvider.getCurrentUser()?.let { user -> setMessage(message, user) }

            setOnUserReactionClickListener { user, reaction ->
                userReactionClickListener?.let {
                    it.onUserReactionClick(message, user, reaction)
                    dismiss()
                }
            }

            val params = (layoutParams as ViewGroup.MarginLayoutParams)
            params.updateMarginsRelative(
                top = style.optionsOverlayUserReactionsMarginTop,
                bottom = style.optionsOverlayUserReactionsMarginBottom,
                start = style.optionsOverlayUserReactionsMarginStart,
                end = style.optionsOverlayUserReactionsMarginEnd,
            )
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
                messageOptionClickListener?.onMessageOptionClick(messageAction)
                dismiss()
            }

            val params = (layoutParams as ViewGroup.MarginLayoutParams)
            params.updateMarginsRelative(
                top = style.optionsOverlayMessageOptionsMarginTop,
                bottom = style.optionsOverlayMessageOptionsMarginBottom,
                start = style.optionsOverlayMessageOptionsMarginStart,
                end = style.optionsOverlayMessageOptionsMarginEnd,
            )
        }
    }

    /**
     * Positions the reactions bubble near the message bubble according to the design.
     */
    private fun anchorReactionsViewToMessageView() {
        val context = requireContext()
        val reactionsOffset = context.getDimension(R.dimen.stream_ui_edit_reactions_horizontal_offset)

        viewHolder.messageContainerView()?.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            with(binding) {
                val rightAlignment = right + reactionsOffset - editReactionsView.left
                val leftAlignment = left + reactionsOffset - editReactionsView.left
                val isRtl = context.isRtlLayout

                editReactionsView.positionBubbleTail(
                    when {
                        messageItem.isMine && !isRtl -> leftAlignment
                        messageItem.isMine && isRtl -> rightAlignment
                        !messageItem.isMine && !isRtl -> rightAlignment
                        else -> leftAlignment
                    }.toFloat(),
                )
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
    public fun interface ReactionClickListener {
        public fun onReactionClick(message: Message, reactionType: String)
    }

    /**
     * A listener for clicks on users who left the reactions.
     */
    public fun interface UserReactionClickListener {
        public fun onUserReactionClick(message: Message, user: User, reaction: Reaction)
    }

    /**
     * A listener for message option clicks.
     */
    public fun interface MessageOptionClickListener {
        public fun onMessageOptionClick(messageAction: MessageAction)
    }

    /**
     * Represents the type of message options dialogs.
     */
    public enum class OptionsDialogType {
        /**
         * Options dialog with message options.
         */
        MESSAGE_OPTIONS,

        /**
         * Options dialog with user reactions.
         */
        REACTION_OPTIONS,
    }

    public companion object {
        public const val TAG: String = "MessageOptionsDialogFragment"

        /**
         * Creates a new instance of [MessageOptionsDialogFragment].
         *
         * @param context The context to load resources.
         * @param message The selected message.
         * @param messageOptionItems The list of message options to display.
         * @param optionsDialogType The type of options dialog.
         * @param style The style for the dialog.
         * @param messageBackgroundFactory The factory for message background.
         * @param attachmentFactoryManager The factory for the attachments in the selected message.
         * @param messageListItemViewHolderFactory The factory class for message items.
         * @param showAvatarPredicate If an avatar should be shown for the message.

         */
        public fun newInstance(
            context: Context,
            message: Message,
            messageOptionItems: List<MessageOptionItem>,
            optionsDialogType: OptionsDialogType = OptionsDialogType.MESSAGE_OPTIONS,
            style: MessageListViewStyle = MessageListViewStyle.createDefault(context),
            messageBackgroundFactory: MessageBackgroundFactory = MessageBackgroundFactoryImpl(style.itemStyle),
            attachmentFactoryManager: AttachmentFactoryManager = ChatUI.attachmentFactoryManager,
            messageListItemViewHolderFactory: MessageListItemViewHolderFactory = MessageListItemViewHolderFactory()
                .apply {
                    setAttachmentFactoryManager(attachmentFactoryManager)
                    setMessageListItemStyle(style.itemStyle)
                    setGiphyViewHolderStyle(style.giphyViewHolderStyle)
                    setAudioRecordViewStyle(style.audioRecordPlayerViewStyle)
                    setReplyMessageListItemViewStyle(style.replyMessageStyle)
                    decoratorProvider = object : DecoratorProvider {
                        override val decorators: List<Decorator> get() = emptyList()
                    }
                },
            showAvatarPredicate: MessageListView.ShowAvatarPredicate = DefaultShowAvatarPredicate(),
        ): MessageOptionsDialogFragment {
            return MessageOptionsDialogFragment().also {
                it.message = message
                it.optionsDialogType = optionsDialogType
                it.style = style
                it.messageListItemViewHolderFactory = messageListItemViewHolderFactory
                it.messageOptionsDecoratorProvider = MessageOptionsDecoratorProvider(
                    style.itemStyle,
                    style.replyMessageStyle,
                    messageBackgroundFactory,
                    showAvatarPredicate,
                )
                it.messageOptionItems = messageOptionItems
            }
        }
    }
}
