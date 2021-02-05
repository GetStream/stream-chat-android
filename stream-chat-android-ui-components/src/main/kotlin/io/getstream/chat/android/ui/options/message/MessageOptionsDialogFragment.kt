package io.getstream.chat.android.ui.options.message

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
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiDialogMessageOptionsBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewTypeMapper
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainerImpl
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.view.MessageListItemStyle
import io.getstream.chat.android.ui.messages.view.MessageListView
import io.getstream.chat.android.ui.utils.extensions.copyToClipboard
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.view.FullScreenDialogFragment
import java.io.Serializable

internal class MessageOptionsDialogFragment : FullScreenDialogFragment() {

    private var _binding: StreamUiDialogMessageOptionsBinding? = null
    private val binding get() = _binding!!

    private val currentUser = ChatDomain.instance().currentUser

    private val optionsMode: OptionsMode by lazy {
        requireArguments().getSerializable(ARG_OPTIONS_MODE) as OptionsMode
    }

    private val configuration by lazy {
        requireArguments().getSerializable(ARG_OPTIONS_CONFIG) as MessageOptionsView.Configuration
    }

    private val itemStyle by lazy {
        requireArguments().getSerializable(ARG_OPTIONS_ITEM_STYLE) as MessageListItemStyle
    }

    private val messageItem: MessageListItem.MessageItem by lazy {
        MessageListItem.MessageItem(
            message,
            positions = listOf(MessageListItem.Position.BOTTOM),
            isMine = message.user.id == currentUser.id
        )
    }

    private lateinit var message: Message
    private lateinit var viewHolder: BaseMessageItemViewHolder<out MessageListItem>

    private var reactionClickHandler: ReactionClickHandler? = null
    private var confirmDeleteMessageClickHandler: ConfirmDeleteMessageClickHandler? = null
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
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        reactionClickHandler = null
        messageOptionsHandlers = null
    }

    fun setReactionClickHandler(reactionClickHandler: ReactionClickHandler) {
        this.reactionClickHandler = reactionClickHandler
    }

    fun setConfirmDeleteMessageClickHandler(confirmDeleteMessageClickHandler: ConfirmDeleteMessageClickHandler) {
        this.confirmDeleteMessageClickHandler = confirmDeleteMessageClickHandler
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

    private fun setupEditReactionsView() {
        with(binding.editReactionsView) {
            setMessage(message, messageItem.isMine)
            setReactionClickListener {
                reactionClickHandler?.onReactionClick(message, it)
                dismiss()
            }
        }
    }

    private fun setupMessageView() {
        viewHolder = MessageListItemViewHolderFactory()
            .apply {
                decoratorProvider = MessageOptionsDecoratorProvider(itemStyle)
                setListenerContainer(MessageListListenerContainerImpl())
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
            setMessage(message, currentUser)
        }
    }

    private fun setupMessageOptions() {
        with(binding.messageOptionsView) {
            isVisible = true
            configure(configuration, messageItem.isTheirs, messageItem.message.syncStatus)
            updateLayoutParams<LinearLayout.LayoutParams> {
                gravity = if (messageItem.isMine) Gravity.END else Gravity.START
            }
            messageOptionsHandlers?.let(::setupOptionsClickListeners)
        }
    }

    private fun setupOptionsClickListeners(messageOptionsHandlers: MessageOptionsHandlers) {
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
                messageOptionsHandlers.flagClickHandler.onMessageFlag(message)
                dismiss()
            }
            setMuteUserListener {
                messageOptionsHandlers.muteClickHandler.onUserMute(message.user)
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
                if (configuration.deleteConfirmationEnabled) {
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
            is PlainTextWithMediaAttachmentsViewHolder -> viewHolder.binding.mediaAttachmentsGroupView
            is OnlyMediaAttachmentsViewHolder -> viewHolder.binding.mediaAttachmentsGroupView
            is OnlyFileAttachmentsViewHolder -> viewHolder.binding.fileAttachmentsView
            is PlainTextWithFileAttachmentsViewHolder -> viewHolder.binding.fileAttachmentsView
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

    internal class MessageOptionsHandlers(
        val threadReplyHandler: MessageListView.ThreadStartHandler,
        val retryHandler: MessageListView.MessageRetryHandler,
        val editClickHandler: MessageListView.MessageEditHandler,
        val flagClickHandler: MessageListView.MessageFlagHandler,
        val muteClickHandler: MessageListView.UserMuteHandler,
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
        private const val ARG_OPTIONS_ITEM_STYLE = "optionsMessageItemStyle"

        var messageArg: Message? = null

        fun newReactionOptionsInstance(
            message: Message,
            style: MessageListItemStyle,
        ): MessageOptionsDialogFragment {
            return newInstance(OptionsMode.REACTION_OPTIONS, message, null, style)
        }

        fun newMessageOptionsInstance(
            message: Message,
            configuration: MessageOptionsView.Configuration,
            style: MessageListItemStyle,
        ): MessageOptionsDialogFragment {
            return newInstance(OptionsMode.MESSAGE_OPTIONS, message, configuration, style)
        }

        private fun newInstance(
            optionsMode: OptionsMode,
            message: Message,
            configuration: MessageOptionsView.Configuration?,
            style: MessageListItemStyle,
        ): MessageOptionsDialogFragment {
            return MessageOptionsDialogFragment().apply {
                arguments = bundleOf(
                    ARG_OPTIONS_MODE to optionsMode,
                    ARG_OPTIONS_CONFIG to configuration,
                    ARG_OPTIONS_ITEM_STYLE to style
                )
                // pass message via static field
                messageArg = message
            }
        }
    }
}
