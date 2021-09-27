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
import androidx.lifecycle.LiveData
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.copyToClipboard
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.internal.FullScreenDialogFragment
import io.getstream.chat.android.ui.databinding.StreamUiDialogMessageOptionsBinding
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainerImpl
import io.getstream.chat.android.ui.message.list.adapter.internal.MessageListItemViewTypeMapper
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder
import java.io.Serializable

internal class MessageOptionsDialogFragment : FullScreenDialogFragment() {

    private var _binding: StreamUiDialogMessageOptionsBinding? = null
    private val binding get() = _binding!!

    private val currentUser: LiveData<User?> = ChatDomain.instance().user

    private val optionsMode: OptionsMode by lazy {
        requireArguments().getSerializable(ARG_OPTIONS_MODE) as OptionsMode
    }

    private val style by lazy { messageListViewStyle!! }

    private val configuration by lazy {
        requireArguments().getSerializable(ARG_OPTIONS_CONFIG) as MessageOptionsView.Configuration
    }

    private val optionsOffset: Int by lazy { requireContext().getDimension(R.dimen.stream_ui_spacing_medium) }

    private val messageItem: MessageListItem.MessageItem by lazy {
        MessageListItem.MessageItem(
            message,
            positions = listOf(MessageListItem.Position.BOTTOM),
            isMine = message.user.id == currentUser.value?.id
        )
    }

    private lateinit var message: Message
    private lateinit var viewHolder: BaseMessageItemViewHolder<out MessageListItem>

    private var reactionClickHandler: ReactionClickHandler? = null
    private var confirmDeleteMessageClickHandler: ConfirmDeleteMessageClickHandler? = null
    private var confirmFlagMessageClickHandler: ConfirmFlagMessageClickHandler? = null
    private var messageOptionsHandlers: MessageOptionsHandlers? = null

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
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        reactionClickHandler = null
        messageOptionsHandlers = null
        confirmDeleteMessageClickHandler = null
        confirmFlagMessageClickHandler = null
    }

    fun setReactionClickHandler(reactionClickHandler: ReactionClickHandler) {
        this.reactionClickHandler = reactionClickHandler
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
        viewHolder = MessageListItemViewHolderFactory()
            .apply {
                decoratorProvider = MessageOptionsDecoratorProvider(style.itemStyle, style.replyMessageStyle)
                setListenerContainer(MessageListListenerContainerImpl())
                // setAttachmentViewFactory(AttachmentViewFactory())
                setMessageListItemStyle(style.itemStyle)
            }
            .createViewHolder(
                binding.messageContainer,
                MessageListItemViewTypeMapper.getViewTypeValue(messageItem)
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
            currentUser.value?.let { user -> setMessage(message, user) }
        }
    }

    private fun isMessageAuthorMuted(): Boolean {
        return currentUser.value?.mutes?.any { mute -> mute.target.id == message.user.id } == true
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

    private fun anchorReactionsViewToMessageView() {
        val reactionsWidth = requireContext().getDimension(R.dimen.stream_ui_edit_reactions_total_width)
        val reactionsOffset = requireContext().getDimension(R.dimen.stream_ui_edit_reactions_horizontal_offset)

        when (val viewHolder = viewHolder) {
            is MessagePlainTextViewHolder -> viewHolder.binding.messageContainer
            is TextAndAttachmentsViewHolder -> viewHolder.binding.messageContainer
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

        fun newReactionOptionsInstance(
            message: Message,
            configuration: MessageOptionsView.Configuration,
            style: MessageListViewStyle,
        ): MessageOptionsDialogFragment {
            return newInstance(OptionsMode.REACTION_OPTIONS, message, configuration, style)
        }

        fun newMessageOptionsInstance(
            message: Message,
            configuration: MessageOptionsView.Configuration,
            style: MessageListViewStyle,
        ): MessageOptionsDialogFragment {
            return newInstance(OptionsMode.MESSAGE_OPTIONS, message, configuration, style)
        }

        private fun newInstance(
            optionsMode: OptionsMode,
            message: Message,
            configuration: MessageOptionsView.Configuration,
            style: MessageListViewStyle,
        ): MessageOptionsDialogFragment {
            messageListViewStyle = style
            return MessageOptionsDialogFragment().apply {
                arguments = bundleOf(
                    ARG_OPTIONS_MODE to optionsMode,
                    ARG_OPTIONS_CONFIG to configuration
                )
                // pass message via static field
                messageArg = message
            }
        }
    }
}
