package io.getstream.chat.android.ui.options

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.databinding.StreamUiDialogMessageOptionsBinding
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewTypeMapper
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import java.io.Serializable

internal class MessageOptionsOverlayDialogFragment : DialogFragment() {

    private var _binding: StreamUiDialogMessageOptionsBinding? = null
    private val binding get() = _binding!!

    private val messageViewHolderFactory = MessageListItemViewHolderFactory()
    private val messageItem: MessageListItem.MessageItem by lazy {
        val wrapper = requireArguments().getSerializable(ARG_MESSAGE)
        wrapper as MessageItemWrapper
        wrapper.messageListItem
    }
    private lateinit var messageView: View

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
        configureMessageOptions(
            requireArguments().getSerializable(ARG_OPTIONS_CONFIG) as MessageOptionsView.Configuration
        )
    }

    private fun setupEditReactionsView() {
        binding.editReactionsView.setMessage(messageItem.message, messageItem.isMine)
        binding.editReactionsView.setReactionClickListener {
            // dismiss()
        }
        binding.messageContainer.setOnClickListener {}
        binding.messageOptionsView.setOnClickListener {}
    }

    private fun configureMessageOptions(configuration: MessageOptionsView.Configuration) {
        binding.messageOptionsView.configure(configuration)
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

    companion object {
        const val TAG = "messageOptions"

        private const val ARG_MESSAGE = "message"
        private const val ARG_OPTIONS_CONFIG = "optionsConfig"

        fun newInstance(
            messageItem: MessageListItem.MessageItem,
            configuration: MessageOptionsView.Configuration
        ): MessageOptionsOverlayDialogFragment {
            return MessageOptionsOverlayDialogFragment().apply {
                arguments = bundleOf(
                    ARG_MESSAGE to MessageItemWrapper(messageItem),
                    ARG_OPTIONS_CONFIG to configuration
                )
            }
        }
    }
}
