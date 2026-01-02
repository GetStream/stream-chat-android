/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages

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
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.list.DeleteMessage
import io.getstream.chat.android.ui.common.state.messages.list.EditMessage
import io.getstream.chat.android.ui.common.state.messages.list.SendAnyway
import io.getstream.chat.android.ui.databinding.StreamUiFragmentMessageListBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.header.MessageListHeaderView
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.utils.extensions.findListener
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView

/**
 * Self-contained chat screen which internally contains the following components:
 *
 * - [MessageListHeaderView] - displays the navigation icon, the channel information
 *   and the channel image
 * - [MessageListView] - shows a list of paginated messages, with threads, replies,
 *   quotes, reactions and deleted messages
 * - [MessageComposerView] - allows the user to send new messages as well as pick and
 *   choose attachments to send
 *
 * **Note**: Fragments representing self-contained screens are easy to use. They allow you
 * to explore the SDK's features in a breeze, however, they offer limited customization.
 */
@Suppress("MemberVisibilityCanBePrivate")
public open class MessageListFragment : Fragment() {

    /** A specific channel cid to be connected with the Stream channel. */
    protected val cid: String by lazy(LazyThreadSafetyMode.NONE) {
        requireNotNull(requireArguments().getString(ARG_CHANNEL_ID)) { "Channel cid must not be null" }
    }

    /** A custom theme resource for the screen. */
    protected val themeResId: Int by lazy(LazyThreadSafetyMode.NONE) { requireArguments().getInt(ARG_THEME_RES_ID) }

    /** A specific message Id to move on the message list. */
    protected val messageId: String? by lazy(LazyThreadSafetyMode.NONE) { requireArguments().getString(ARG_MESSAGE_ID) }

    /** A flag for visibility of the header. */
    protected val showHeader: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getBoolean(ARG_SHOW_HEADER, false)
    }

    protected val threadLoadOlderToNewer: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getBoolean(ARG_THREAD_LOAD_OLDER_TO_NEWER, false)
    }

    /** A ViewModel factory for creating message list relevant ViewModels. */
    protected val factory: MessageListViewModelFactory by lazy(LazyThreadSafetyMode.NONE) {
        MessageListViewModelFactory(
            context = requireContext().applicationContext,
            cid = cid,
            messageId = messageId,
            threadLoadOlderToNewer = threadLoadOlderToNewer,
        )
    }

    /** A message list header ViewModel for binding [MessageListHeaderView]. */
    protected val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }

    /** A message list ViewModel for binding [MessageListView]. */
    protected val messageListViewModel: MessageListViewModel by viewModels { factory }

    /** A message composer ViewModel for binding [MessageComposerView]. */
    protected val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }

    /** A click listener for the navigation button in the header. */
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
        setupMessageComposer(binding.messageComposerView)
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

        binding.messageListView.setModeratedMessageHandler { message, action ->
            when (action) {
                DeleteMessage -> messageListViewModel.onEvent(MessageListViewModel.Event.DeleteMessage(message))
                EditMessage -> messageComposerViewModel.performMessageAction(Edit(message))
                SendAnyway -> messageListViewModel.onEvent(MessageListViewModel.Event.RetryMessage(message))
                else -> Unit
            }
        }
    }

    /**
     * Configures [MessageComposerView]. Override the method for a custom setup.
     *
     * @param messageComposerView The message composer that is being configured.
     */
    protected open fun setupMessageComposer(messageComposerView: MessageComposerView) {
        messageComposerViewModel.bindView(binding.messageComposerView, viewLifecycleOwner)

        messageListViewModel.mode.observe(viewLifecycleOwner) { mode ->
            when (mode) {
                is MessageMode.MessageThread -> {
                    messageListHeaderViewModel.setActiveThread(mode.parentMessage)
                    messageComposerViewModel.setMessageMode(MessageMode.MessageThread(mode.parentMessage))
                }
                is MessageMode.Normal -> {
                    messageListHeaderViewModel.resetThread()
                    messageComposerViewModel.leaveThread()
                }
            }
        }
        binding.messageListView.setMessageReplyHandler { _, message ->
            messageComposerViewModel.performMessageAction(Reply(message))
        }
        binding.messageListView.setMessageEditHandler { message ->
            messageComposerViewModel.performMessageAction(Edit(message))
        }
        binding.messageListView.setModeratedMessageHandler { message, action ->
            when (action) {
                DeleteMessage -> messageListViewModel.onEvent(MessageListViewModel.Event.DeleteMessage(message))
                EditMessage -> messageComposerViewModel.performMessageAction(Edit(message))
                SendAnyway -> messageListViewModel.onEvent(MessageListViewModel.Event.RetryMessage(message))
                else -> Unit
            }
        }
        binding.messageListView.setAttachmentReplyOptionClickHandler { result ->
            messageListViewModel.getMessageById(result.messageId)?.let { message ->
                messageComposerViewModel.performMessageAction(Reply(message))
            }
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
        private var threadLoadOlderToNewer = false

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
         * Whether the thread messages should be loaded from older to newer.
         */
        public fun threadLoadOlderToNewer(threadLoadOlderToNewer: Boolean): Builder = apply {
            this.threadLoadOlderToNewer = threadLoadOlderToNewer
        }

        /**
         * Sets custom message list Fragment. The Fragment must be a subclass of [MessageListFragment].
         */
        public fun <T : MessageListFragment> setFragment(fragment: T): Builder = apply {
            this.fragment = fragment
        }

        /**
         * Builds a custom message list Fragment.
         *
         * @return A customized [MessageListFragment].
         */
        public fun build(): MessageListFragment {
            return (fragment ?: MessageListFragment()).apply {
                arguments = bundleOf(
                    ARG_THEME_RES_ID to this@Builder.themeResId,
                    ARG_CHANNEL_ID to this@Builder.cid,
                    ARG_MESSAGE_ID to this@Builder.messageId,
                    ARG_SHOW_HEADER to this@Builder.showHeader,
                    ARG_THREAD_LOAD_OLDER_TO_NEWER to this@Builder.threadLoadOlderToNewer,
                )
            }
        }
    }

    public companion object {
        private const val ARG_THEME_RES_ID: String = "theme_res_id"
        private const val ARG_CHANNEL_ID: String = "cid"
        private const val ARG_MESSAGE_ID: String = "message_id"
        private const val ARG_SHOW_HEADER: String = "show_header"
        private const val ARG_THREAD_LOAD_OLDER_TO_NEWER: String = "thread_load_older_to_newer"

        /**
         * Creates instances of [MessageListFragment].
         *
         * @param cid The full channel id. ie messaging:123.
         * @param initializer The initializer to customize builder params.
         */
        @JvmStatic
        @JvmOverloads
        public fun newInstance(cid: String, initializer: (Builder.() -> Unit)? = null): MessageListFragment {
            val builder = Builder(cid)
            initializer?.invoke(builder)
            return builder.build()
        }
    }
}
