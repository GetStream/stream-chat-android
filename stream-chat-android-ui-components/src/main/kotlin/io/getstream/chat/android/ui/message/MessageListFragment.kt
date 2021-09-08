package io.getstream.chat.android.ui.message

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
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
@Suppress("MemberVisibilityCanBePrivate")
public open class MessageListFragment : Fragment() {

    protected val cid: String by lazy {
        requireNotNull(requireArguments().getString(ARG_CHANNEL_ID)) { "Channel cid must not be null" }
    }
    protected val themeResId: Int by lazy { requireArguments().getInt(ARG_THEME_RES_ID) }
    protected val messageId: String? by lazy { requireArguments().getString(ARG_MESSAGE_ID) }
    protected val showHeader: Boolean by lazy { requireArguments().getBoolean(ARG_SHOW_HEADER, false) }

    protected val factory: MessageListViewModelFactory by lazy { MessageListViewModelFactory(cid, messageId) }
    protected val messageListViewModel: MessageListViewModel by viewModels { factory }
    protected val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
    protected val messageInputViewModel: MessageInputViewModel by viewModels { factory }

    protected var backPressListener: BackPressListener? = null

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
        val layoutInflater = if (getTheme() != 0) {
            inflater.cloneInContext(ContextThemeWrapper(context, getTheme()))
        } else {
            inflater
        }
        return StreamUiFragmentMessageListBinding.inflate(layoutInflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMessageListHeader(binding.messageListHeaderView)
        setupMessageList(binding.messageListView)
        setupMessageInput(binding.messageInputView)
    }

    /**
     * Provides a custom theme for the screen.
     */
    @StyleRes
    protected open fun getTheme(): Int {
        return themeResId
    }

    /**
     * Configures [MessageListHeaderView]. Override the method for a custom setup.
     *
     * @param messageListHeaderView The message list header that is being configured.
     */
    protected open fun setupMessageListHeader(messageListHeaderView: MessageListHeaderView) {
        with(messageListHeaderView) {
            if (showHeader) {
                messageListHeaderViewModel.bindView(this, viewLifecycleOwner)

                setBackButtonClickListener {
                    messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
                }
            } else {
                isVisible = false
            }
        }
    }

    /**
     * Configures [MessageListView]. Override the method for a custom setup.
     *
     * @param messageListView The message list that is being configured.
     */
    protected open fun setupMessageList(messageListView: MessageListView) {
        messageListViewModel.bindView(messageListView, viewLifecycleOwner)

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

    /**
     * Configures [MessageInputView]. Override the method for a custom setup.
     *
     * @param messageInputView The message inout that is being configured.
     */
    protected open fun setupMessageInput(messageInputView: MessageInputView) {
        messageInputViewModel.apply {
            messageInputViewModel.bindView(messageInputView, viewLifecycleOwner)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        backPressListener = null
    }

    /**
     * Click listener for the navigation button in the header. Finishes Activity by default.
     *
     * **Note**: Implement the click listener in parent Fragment or Activity
     * to override the default behavior.
     */
    public fun interface BackPressListener {
        public fun onBackPress()
    }

    /**
     * Creates instances of [MessageListFragment].
     *
     * @param cid The full channel id. ie messaging:123.
     */
    public class Builder(private val cid: String) {
        private var themeResId: Int = 0
        private var showHeader: Boolean = false
        private var messageId: String? = null
        private var fragment: MessageListFragment? = null

        /**
         * Custom theme for the screen.
         */
        public fun customTheme(@StyleRes themeResId: Int): Builder = apply {
            this.themeResId = themeResId
        }

        /**
         * Whether the header is shown or hidden.
         */
        public fun showHeader(showHeader: Boolean): Builder = apply {
            this.showHeader = showHeader
        }

        /**
         * The id of the message to highlight.
         */
        public fun messageId(messageId: String?): Builder = apply {
            this.messageId = messageId
        }

        /**
         * Sets custom message list Fragment. The Fragment must be a subclass of [MessageListFragment].
         */
        public fun <T : MessageListFragment> setFragment(fragment: T): Builder = apply {
            this.fragment = fragment
        }

        public fun build(): MessageListFragment {
            return (fragment ?: MessageListFragment()).apply {
                arguments = bundleOf(
                    ARG_THEME_RES_ID to this@Builder.themeResId,
                    ARG_CHANNEL_ID to this@Builder.cid,
                    ARG_MESSAGE_ID to this@Builder.messageId,
                    ARG_SHOW_HEADER to this@Builder.showHeader,
                )
            }
        }
    }

    public companion object {
        private const val ARG_THEME_RES_ID: String = "theme_res_id"
        private const val ARG_CHANNEL_ID: String = "cid"
        private const val ARG_MESSAGE_ID: String = "message_id"
        private const val ARG_SHOW_HEADER: String = "show_header"

        /**
         * Creates instances of [MessageListFragment].
         *
         * @param cid The full channel id. ie messaging:123.
         * @param initializer The initializer to customize builder params.
         */
        public fun newInstance(cid: String, initializer: (Builder.() -> Unit)? = null): MessageListFragment {
            val builder = Builder(cid)
            initializer?.invoke(builder)
            return builder.build()
        }
    }
}
