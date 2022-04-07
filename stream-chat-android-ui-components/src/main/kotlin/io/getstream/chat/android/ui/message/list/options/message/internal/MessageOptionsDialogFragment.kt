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
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.copyToClipboard
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
import java.io.Serializable

internal class MessageOptionsDialogFragment : FullScreenDialogFragment() {

    private var _binding: StreamUiDialogMessageOptionsBinding? = null
    private val binding get() = _binding!!

    private val optionsMode: OptionsMode by lazy {
        requireArguments().getSerializable(ARG_OPTIONS_MODE) as OptionsMode
    }

    private val style by lazy { messageListViewStyle!! }

    private val viewHolderFactory by lazy { messageViewHolderFactory!! }

    private val configuration by lazy {
        requireArguments().getSerializable(ARG_OPTIONS_CONFIG) as MessageOptionsView.Configuration
    }

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
    private var confirmDeleteMessageClickHandler: ConfirmDeleteMessageClickHandler? = null
    private var confirmFlagMessageClickHandler: ConfirmFlagMessageClickHandler? = null
    private var messageOptionsHandlers: MessageOptionsHandlers? = null
    private var userReactionClickHandler: UserReactionClickHandler? = null

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
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        reactionClickHandler = null
        messageOptionsHandlers = null
        confirmDeleteMessageClickHandler = null
        confirmFlagMessageClickHandler = null
        userReactionClickHandler = null
    }

    fun setReactionClickHandler(reactionClickHandler: ReactionClickHandler) {
        this.reactionClickHandler = reactionClickHandler
    }

    fun setUserReactionClickHandler(userReactionClickHandler: UserReactionClickHandler) {
        this.userReactionClickHandler = userReactionClickHandler
    }

    fun setConfirmDeleteMessageClickHandler(confirmDeleteMessageClickHandler: ConfirmDeleteMessageClickHandler) {
        this.confirmDeleteMessageClickHandler = confirmDeleteMessageClickHandler
    }

    fun setConfirmFlagMessageClickHandler(confirmFlagMessageClickHandler: ConfirmFlagMessageClickHandler) {
        this.confirmFlagMessageClickHandler = confirmFlagMessageClickHandler
    }

    fun setMessageOptionsHandlers(messageOptionsHandlers: MessageOptionsHandlers) {
        this.messageOptionsHandlers = messageOptionsHandlers
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
            if (configuration.reactionsEnabled) {
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
        viewHolder = viewHolderFactory
            .createViewHolder(
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

    private fun isMessageAuthorMuted(): Boolean {
        return ChatClient.instance().globalState.user.value?.mutes?.any { mute -> mute.target.id == message.user.id } == true
    }

    private fun setupMessageOptions() {
        with(binding.messageOptionsView) {
            isVisible = true
            val isMessageAuthorMuted = isMessageAuthorMuted()
            configure(
                configuration = configuration,
                style = style,
                isMessageTheirs = messageItem.isTheirs,
                syncStatus = messageItem.message.syncStatus,
                isMessageAuthorMuted = isMessageAuthorMuted,
                isMessagePinned = message.pinned
            )
            updateLayoutParams<LinearLayout.LayoutParams> {
                gravity = if (messageItem.isMine) Gravity.END else Gravity.START
            }
            messageOptionsHandlers?.let { messageOptionsHandlers ->
                setupOptionsClickListeners(
                    messageOptionsHandlers = messageOptionsHandlers,
                    isMessageAuthorMuted = isMessageAuthorMuted,
                    isMessagePinned = message.pinned
                )
            }

            updateLayoutParams<ViewGroup.MarginLayoutParams> {
                if (messageItem.isMine) {
                    marginEnd = style.itemStyle.messageEndMargin + optionsOffset
                } else {
                    marginStart = style.itemStyle.messageStartMargin + optionsOffset
                }
            }
        }
    }

    private fun setupOptionsClickListeners(
        messageOptionsHandlers: MessageOptionsHandlers,
        isMessageAuthorMuted: Boolean,
        isMessagePinned: Boolean,
    ) {
        binding.messageOptionsView.run {
            setThreadListener {
                messageOptionsHandlers.threadReplyHandler.onStartThread(message)
                dismiss()
            }
            setRetryListener {
                messageOptionsHandlers.retryHandler.onMessageRetry(message)
                dismiss()
            }
            setCopyListener {
                context.copyToClipboard(message.text)
                dismiss()
            }
            setEditMessageListener {
                messageOptionsHandlers.editClickHandler.onMessageEdit(message)
                dismiss()
            }
            setFlagMessageListener {
                if (style.deleteConfirmationEnabled) {
                    confirmFlagMessageClickHandler?.onConfirmFlagMessage(message) {
                        messageOptionsHandlers.flagClickHandler.onMessageFlag(message)
                    }
                } else {
                    messageOptionsHandlers.flagClickHandler.onMessageFlag(message)
                }
                dismiss()
            }
            setPinMessageListener {
                if (isMessagePinned) {
                    messageOptionsHandlers.unpinClickHandler.onMessageUnpin(message)
                } else {
                    messageOptionsHandlers.pinClickHandler.onMessagePin(message)
                }
                dismiss()
            }
            setMuteUserListener {
                if (isMessageAuthorMuted) {
                    messageOptionsHandlers.unmuteClickHandler.onUserUnmute(message.user)
                } else {
                    messageOptionsHandlers.muteClickHandler.onUserMute(message.user)
                }
                dismiss()
            }
            setBlockUserListener {
                messageOptionsHandlers.blockClickHandler.onUserBlock(message.user, message.cid)
                dismiss()
            }
            setReplyListener {
                messageOptionsHandlers.replyClickHandler.onMessageReply(messageItem.message.cid, messageItem.message)
                dismiss()
            }
            setDeleteMessageListener {
                if (style.deleteConfirmationEnabled) {
                    confirmDeleteMessageClickHandler?.onConfirmDeleteMessage(message) {
                        messageOptionsHandlers.deleteClickHandler.onMessageDelete(message)
                    }
                } else {
                    messageOptionsHandlers.deleteClickHandler.onMessageDelete(message)
                }
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

    internal fun interface ConfirmDeleteMessageClickHandler {
        fun onConfirmDeleteMessage(
            message: Message,
            callback: ConfirmDeleteMessageCallback,
        )

        fun interface ConfirmDeleteMessageCallback {
            fun onConfirmDeleteMessage()
        }
    }

    internal fun interface ConfirmFlagMessageClickHandler {
        fun onConfirmFlagMessage(message: Message, confirmCallback: () -> Unit)
    }

    internal class MessageOptionsHandlers(
        val threadReplyHandler: MessageListView.ThreadStartHandler,
        val retryHandler: MessageListView.MessageRetryHandler,
        val editClickHandler: MessageListView.MessageEditHandler,
        val flagClickHandler: MessageListView.MessageFlagHandler,
        val pinClickHandler: MessageListView.MessagePinHandler,
        val unpinClickHandler: MessageListView.MessageUnpinHandler,
        val muteClickHandler: MessageListView.UserMuteHandler,
        val unmuteClickHandler: MessageListView.UserUnmuteHandler,
        val blockClickHandler: MessageListView.UserBlockHandler,
        val deleteClickHandler: MessageListView.MessageDeleteHandler,
        val replyClickHandler: MessageListView.MessageReplyHandler,
    ) : Serializable

    internal enum class OptionsMode {
        MESSAGE_OPTIONS,
        REACTION_OPTIONS
    }

    companion object {
        const val TAG = "MessageOptionsDialogFragment"

        private const val ARG_OPTIONS_MODE = "optionsMode"
        private const val ARG_OPTIONS_CONFIG = "optionsConfig"

        internal var messageListViewStyle: MessageListViewStyle? = null

        var messageArg: Message? = null
        var messageViewHolderFactory: MessageListItemViewHolderFactory? = null
        var attachmentFactoryManager: AttachmentFactoryManager = AttachmentFactoryManager()

        fun newReactionOptionsInstance(
            message: Message,
            configuration: MessageOptionsView.Configuration,
            style: MessageListViewStyle,
            messageViewHolderFactory: MessageListItemViewHolderFactory,
            messageBackgroundFactory: MessageBackgroundFactory,
            attachmentFactoryManager: AttachmentFactoryManager,
            showAvatarPredicate: MessageListView.ShowAvatarPredicate,
        ): MessageOptionsDialogFragment {
            return newInstance(
                OptionsMode.REACTION_OPTIONS,
                message,
                configuration,
                style,
                messageViewHolderFactory,
                messageBackgroundFactory,
                attachmentFactoryManager,
                showAvatarPredicate
            )
        }

        fun newMessageOptionsInstance(
            message: Message,
            configuration: MessageOptionsView.Configuration,
            style: MessageListViewStyle,
            messageViewHolderFactory: MessageListItemViewHolderFactory,
            messageBackgroundFactory: MessageBackgroundFactory,
            attachmentFactoryManager: AttachmentFactoryManager,
            showAvatarPredicate: MessageListView.ShowAvatarPredicate,
        ): MessageOptionsDialogFragment {
            return newInstance(
                OptionsMode.MESSAGE_OPTIONS,
                message,
                configuration,
                style,
                messageViewHolderFactory,
                messageBackgroundFactory,
                attachmentFactoryManager,
                showAvatarPredicate
            )
        }

        private fun newInstance(
            optionsMode: OptionsMode,
            message: Message,
            configuration: MessageOptionsView.Configuration,
            style: MessageListViewStyle,
            messageViewHolderFactory: MessageListItemViewHolderFactory,
            messageBackgroundFactory: MessageBackgroundFactory,
            attachmentFactoryManager: AttachmentFactoryManager,
            showAvatarPredicate: MessageListView.ShowAvatarPredicate,
        ): MessageOptionsDialogFragment {
            this.messageListViewStyle = style
            this.attachmentFactoryManager = attachmentFactoryManager
            this.messageViewHolderFactory =
                messageViewHolderFactory.clone()
                    .apply {
                        /* Default listener. We don't want the message of this dialog to listen for clicks just like it was
                        * a normal message inside MessageListView
                        */
                        setListenerContainer(null)
                        decoratorProvider = MessageOptionsDecoratorProvider(
                            style.itemStyle,
                            style.replyMessageStyle,
                            messageBackgroundFactory,
                            showAvatarPredicate
                        )
                    }
            return MessageOptionsDialogFragment().apply {
                arguments = bundleOf(
                    ARG_OPTIONS_MODE to optionsMode,
                    ARG_OPTIONS_CONFIG to configuration,
                )
                // pass message via static field
                messageArg = message
            }
        }
    }
}
