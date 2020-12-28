package io.getstream.chat.android.ui.messages.reactions

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
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiDialogReactionsBinding
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewTypeMapper
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.utils.extensions.cast
import io.getstream.chat.android.ui.utils.extensions.getDimension

internal class ReactionsOverlayDialogFragment : DialogFragment() {

    private var _binding: StreamUiDialogReactionsBinding? = null
    private val binding get() = _binding!!

    private val messageViewHolderFactory = MessageListItemViewHolderFactory(ChatDomain.instance().currentUser)
    private val messageItem: MessageListItem.MessageItem by lazy {
        requireArguments().getSerializable(ARG_MESSAGE) as MessageListItem.MessageItem
    }
    private lateinit var messageView: View

    private var reactionClickListener: ReactionClickListener? = null

    fun setReactionClickListener(reactionClickListener: ReactionClickListener) {
        this.reactionClickListener = reactionClickListener
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
        return StreamUiDialogReactionsBinding.inflate(inflater, container, false)
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
        setupUserReactionsView()
        anchorReactionsViewToMessageView()
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

    override fun onDestroy() {
        super.onDestroy()
        reactionClickListener = null
    }

    private fun setupEditReactionsView() {
        binding.editReactionsView.setMessage(messageItem.message, messageItem.isMine)
        binding.editReactionsView.setReactionClickListener {
            reactionClickListener?.onReactionClick(it)
            dismiss()
        }
    }

    /**
     * TODO: find out a consistent way to get message bounds across all message view holders
     */
    private fun setupMessageView() {
        messageView = messageViewHolderFactory
            .createViewHolder(
                binding.messageContainer,
                MessageListItemViewTypeMapper.getViewTypeValue(messageItem)
            )
            .cast<MessagePlainTextViewHolder>()
            .apply {
                addMessageView(itemView)
                bind(messageItem)
            }.binding.messageText
    }

    private fun setupUserReactionsView() {
        binding.userReactionsView.setMessage(messageItem.message)
    }

    private fun anchorReactionsViewToMessageView() {
        val reactionsWidth = requireContext().getDimension(R.dimen.stream_ui_edit_reactions_total_width)
        val reactionsOffset = requireContext().getDimension(R.dimen.stream_ui_edit_reactions_horizontal_offset)

        messageView.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
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

    private fun addMessageView(messageView: View) {
        binding.messageContainer.addView(
            messageView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    companion object {
        const val TAG = "reactions"

        private const val ARG_MESSAGE = "message"

        fun newInstance(messageItem: MessageListItem.MessageItem): ReactionsOverlayDialogFragment {
            return ReactionsOverlayDialogFragment().apply {
                arguments = bundleOf(ARG_MESSAGE to messageItem)
            }
        }
    }
}
