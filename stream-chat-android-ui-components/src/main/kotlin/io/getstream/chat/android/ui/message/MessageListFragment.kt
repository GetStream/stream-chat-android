package io.getstream.chat.android.ui.message

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.common.extensions.internal.findListener
import io.getstream.chat.android.ui.databinding.StreamUiFragmentMessageListBinding
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory

/**
 * Self-contained chat screen which internally contains the following components:
 *
 * - [MessageListHeaderView] - displays the navigation icon, the channel information
 *   and the channel image
 * - [MessageListView] - shows a list of paginated messages, with threads, replies,
 *   quotes, reactions and deleted messages
 * - [MessageInputView] - allows the user to send new messages as well as pick and
 *   choose attachments to send
 *
 * **Note**: Fragments representing self-contained screens are easy to use. They allow you
 * to explore the SDK's features in a breeze, however, they offer limited customization.
 */
public open class MessageListFragment : Fragment() {

    private val cid: String by lazy {
        requireNotNull(requireArguments().getString(ARG_CHANNEL_ID)) { "Channel cid must not be null" }
    }

    private val messageId: String? by lazy { requireArguments().getString(ARG_MESSAGE_ID) }
    private val messageLimit: String? by lazy { requireArguments().getString(ARG_MESSAGE_LIMIT) }

    private val showHeader: Boolean by lazy { requireArguments().getBoolean(ARG_SHOW_HEADER, false) }

    private val factory: MessageListViewModelFactory by lazy { MessageListViewModelFactory(cid, messageId) }

    private val messageListViewModel: MessageListViewModel by viewModels { factory }
    private val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
    private val messageInputViewModel: MessageInputViewModel by viewModels { factory }

    private var backPressListener: BackPressListener? = null

    private var _binding: StreamUiFragmentMessageListBinding? = null
    protected val binding: StreamUiFragmentMessageListBinding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        backPressListener = findListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return StreamUiFragmentMessageListBinding.inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMessageListHeader()
        setupMessageList()
        setupMessageInput()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        backPressListener = null
    }

    protected open fun setupMessageListHeader() {
        with(binding.messageListHeaderView) {
            messageListHeaderViewModel.bindView(this, viewLifecycleOwner)

            setBackButtonClickListener {
                messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
            }
        }
    }

    protected open fun setupMessageList() {
        messageListViewModel.bindView(binding.messageListView, viewLifecycleOwner)

        messageListViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is MessageListViewModel.State.Loading -> Unit
                is MessageListViewModel.State.Result -> Unit
                is MessageListViewModel.State.NavigateUp -> {
                    if (backPressListener == null) {
                        activity?.finish()
                    } else {
                        backPressListener?.onBackPress()
                    }
                }
            }
        }
    }

    protected open fun setupMessageInput() {
        messageInputViewModel.apply {
            messageInputViewModel.bindView(binding.messageInputView, viewLifecycleOwner)

            messageListViewModel.mode.observe(viewLifecycleOwner) {
                when (it) {
                    is MessageListViewModel.Mode.Thread -> {
                        messageListHeaderViewModel.setActiveThread(it.parentMessage)
                        messageInputViewModel.setActiveThread(it.parentMessage)
                    }
                    is MessageListViewModel.Mode.Normal -> {
                        messageListHeaderViewModel.resetThread()
                        messageInputViewModel.resetThread()
                    }
                }
            }
            binding.messageListView.setMessageEditHandler(::postMessageToEdit)
        }
    }

    public fun interface BackPressListener {
        public fun onBackPress()
    }

    /**
     * Creates instances of [MessageListFragment].
     *
     * @param cid the full channel id. ie messaging:123
     */
    public class Builder(private val cid: String) {
        private var showHeader: Boolean = false
        private var messageId: String? = null
        private var messageLimit: Int = 30

        private var fragment: MessageListFragment? = null

        /**
         * If we're showing the header or not
         */
        public fun showHeader(showHeader: Boolean): Builder = apply {
            this.showHeader = showHeader
        }

        /**
         * The limit of messages per query
         */
        public fun messageLimit(messageLimit: Int): Builder = apply {
            this.messageLimit = messageLimit
        }

        /**
         * The id of the message to highlight
         */
        public fun messageId(messageId: String): Builder = apply {
            this.messageId = messageId
        }

        public fun <T : MessageListFragment> setFragment(fragment: T): Builder = apply {
            this.fragment = fragment
        }

        public fun build(): MessageListFragment {
            return (fragment ?: MessageListFragment()).apply {
                arguments = bundleOf(
                    ARG_CHANNEL_ID to this@Builder.cid,
                    ARG_MESSAGE_ID to this@Builder.messageId,
                    ARG_SHOW_HEADER to this@Builder.showHeader,
                    ARG_MESSAGE_LIMIT to this@Builder.messageLimit,
                )
            }
        }
    }

    public companion object {
        public const val ARG_CHANNEL_ID: String = "cid"
        public const val ARG_MESSAGE_ID: String = "message_id"
        public const val ARG_SHOW_HEADER: String = "show_header"
        public const val ARG_MESSAGE_LIMIT: String = "message_limit"

        /**
         * Creates instances of [MessageListFragment].
         *
         * @param cid the full channel id. ie messaging:123
         * @param initializer the initializer to customize builder params
         */
        public fun newInstance(cid: String, initializer: Builder.() -> Unit): MessageListFragment {
            val builder = Builder(cid)
            builder.initializer()
            return builder.build()
        }
    }
}
