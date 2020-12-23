package io.getstream.chat.android.ui.options

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.databinding.StreamUiDialogMessageOptionsBinding
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewTypeMapper
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import java.io.Serializable

internal class MessageOptionsOverlayDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "messageOptions"

        private const val ARG_MESSAGE = "message"
        private const val ARG_OPTIONS_CONFIG = "optionsConfig"

        fun newInstance(
            messageItem: MessageListItem.MessageItem,
            configuration: MessageOptionsView.Configuration,
        ): MessageOptionsOverlayDialogFragment {
            return MessageOptionsOverlayDialogFragment().apply {
                arguments = bundleOf(
                    ARG_MESSAGE to MessageItemWrapper(messageItem),
                    ARG_OPTIONS_CONFIG to configuration
                )
            }
        }
    }

    private var _binding: StreamUiDialogMessageOptionsBinding? = null
    private val binding get() = _binding!!

    private val messageViewHolderFactory = MessageListItemViewHolderFactory()
    private val messageItem: MessageListItem.MessageItem by lazy {
        val wrapper = requireArguments().getSerializable(ARG_MESSAGE)
        wrapper as MessageItemWrapper
        wrapper.messageListItem
    }
    private lateinit var messageView: View

    private var reactionClickListener: ReactionClickListener? = null
    private var handlers: Handlers? = null

    private val configuration by lazy {
        requireArguments().getSerializable(ARG_OPTIONS_CONFIG) as MessageOptionsView.Configuration
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.requestFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return StreamUiDialogMessageOptionsBinding.inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerView.setOnClickListener {
            dismiss()
        }

        setupEditReactionsView()
        setupMessageView()
        configureMessageOptions(configuration, messageItem.isTheirs, messageItem.message.syncStatus)
        handlers?.let(::setupMessageOptionClickListeners)
    }

    override fun onDestroy() {
        super.onDestroy()
        reactionClickListener = null
    }

    fun setReactionClickListener(reactionClickListener: ReactionClickListener) {
        this.reactionClickListener = reactionClickListener
    }

    fun setMessageOptionsHandlers(handlers: Handlers) {
        this.handlers = handlers
    }

    private fun setupEditReactionsView() {
        binding.editReactionsView.setMessage(messageItem.message, messageItem.isMine)
        binding.editReactionsView.setReactionClickListener {
            reactionClickListener?.onReactionClick(messageItem.message, it.type)
            dismiss()
        }
        binding.messageContainer.setOnClickListener {}
        binding.messageOptionsView.setOnClickListener {}
    }

    private fun configureMessageOptions(
        configuration: MessageOptionsView.Configuration,
        isTheirs: Boolean,
        syncStatus: SyncStatus
    ) {
        binding.messageOptionsView.configure(configuration, isTheirs, syncStatus)
    }

    private fun setupMessageOptionClickListeners(handlers: Handlers) {
        binding.messageOptionsView.run {
            setThreadListener {
                handlers.threadReplyHandler(messageItem.message)
                dismiss()
            }

            setRetryListener {
                handlers.retryHandler(messageItem.message)
                dismiss()
            }

            setCopyListener {
                copyText(messageItem.message)
                dismiss()
            }

            setEditMessageListener {
                handlers.editClickHandler(messageItem.message)
                dismiss()
            }

            setFlagMessageListener {
                handlers.flagClickHandler(messageItem.message)
                dismiss()
            }

            setMuteUserListener {
                handlers.muteClickHandler(messageItem.message.user)
                dismiss()
            }

            setBlockUserListener {
                handlers.blockClickHandler(messageItem.message.user)
                dismiss()
            }

            setDeleteMessageListener {
                if (configuration.deleteConfirmationEnabled) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(configuration.deleteConfirmationTitle)
                        .setMessage(configuration.deleteConfirmationMessage)
                        .setPositiveButton(configuration.deleteConfirmationPositiveButton) { dialog, _ ->
                            handlers.deleteClickHandler(messageItem.message)
                            dialog.dismiss()
                            dismiss()
                        }
                        .setNegativeButton(configuration.deleteConfirmationNegativeButton) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    handlers.deleteClickHandler(messageItem.message)
                    dismiss()
                }
            }
        }
    }

    private fun copyText(message: Message) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", message.text)
        clipboard.setPrimaryClip(clip)
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * TODO: find out a consistent way to get message bounds across all message view holders
     */
    private fun setupMessageView() {
        val messageView = messageViewHolderFactory
            .createViewHolder(
                binding.messageContainer,
                MessageListItemViewTypeMapper.getViewTypeValue(messageItem)
            ).also { viewHolder ->
                addMessageView(viewHolder.itemView)
            }

        when (messageView) {
            is MessagePlainTextViewHolder -> messageView.bind(messageItem)

            is OnlyMediaAttachmentsViewHolder -> messageView.bind(messageItem)
        }
    }

    private fun addMessageView(messageView: View) {
        binding.messageContainer.addView(
            messageView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private class MessageItemWrapper(val messageListItem: MessageListItem.MessageItem) : Serializable

    fun interface ReactionClickListener {
        fun onReactionClick(message: Message, reactionType: String)
    }

    internal class Handlers(
        val threadReplyHandler: (Message) -> Unit,
        val retryHandler: (Message) -> Unit,
        val editClickHandler: (Message) -> Unit,
        val flagClickHandler: (Message) -> Unit,
        val muteClickHandler: (User) -> Unit,
        val blockClickHandler: (User) -> Unit,
        val deleteClickHandler: (Message) -> Unit,
    ) : Serializable
}
