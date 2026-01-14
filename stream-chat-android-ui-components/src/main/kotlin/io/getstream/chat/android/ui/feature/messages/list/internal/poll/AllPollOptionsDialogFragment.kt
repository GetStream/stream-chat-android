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

package io.getstream.chat.android.ui.feature.messages.list.internal.poll

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.state.extensions.state
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAllPollOptionsBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemOptionBinding
import io.getstream.chat.android.ui.utils.extensions.applyEdgeToEdgePadding
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Represent the bottom sheet dialog that allows users to pick attachments.
 */
public class AllPollOptionsDialogFragment : AppCompatDialogFragment() {

    private var _binding: StreamUiFragmentAllPollOptionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AllPollOptionsViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AllPollOptionsViewModel(
                        messageId = requireArguments().getString(ARG_MESSAGE_ID)!!,
                        channelId = requireArguments().getString(ARG_CHANNEL_ID)!!,
                        channelType = requireArguments().getString(ARG_CHANNEL_TYPE)!!,
                        coroutineScope = viewLifecycleOwner.lifecycleScope,
                    ) as T
                }
            }
        },
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = StreamUiFragmentAllPollOptionsBinding
            .inflate(requireContext().streamThemeInflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applyEdgeToEdgePadding(typeMask = WindowInsetsCompat.Type.systemBars())
        setupDialog()
    }

    /**
     * Initializes the dialog.
     */
    private fun setupDialog() {
        setupToolbar(binding.toolbar)
        val adapter = OptionsAdapter {
            viewModel.onOptionClick(it)
        }
        binding.optionList.adapter = adapter
        lifecycleScope.launch {
            viewModel
                .pollState
                .mapNotNull { it }
                .collect { poll ->
                    binding.question.text = poll.name
                    adapter.setPoll(poll)
                }
        }
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationOnClickListener { dismiss() }
        ContextCompat.getDrawable(requireContext(), R.drawable.stream_ui_arrow_left)?.apply {
            setTint(ContextCompat.getColor(requireContext(), R.color.stream_ui_black))
        }?.let(toolbar::setNavigationIcon)
        toolbar.setTitle(getString(R.string.stream_ui_poll_options_title))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    public companion object {
        public const val TAG: String = "create_all_poll_options_dialog"
        private const val ARG_MESSAGE_ID = "message_id"
        private const val ARG_CHANNEL_ID = "channel_id"
        private const val ARG_CHANNEL_TYPE = "channel_type"

        /**
         * Creates a new instance of [AllPollOptionsDialogFragment].
         *
         * @return A new instance of [AllPollOptionsDialogFragment].
         */
        public fun newInstance(message: Message): AllPollOptionsDialogFragment = AllPollOptionsDialogFragment().apply {
            val (channelType, channelId) = message.cid.cidToTypeAndId()
            arguments = Bundle().apply {
                putString(ARG_MESSAGE_ID, message.id)
                putString(ARG_CHANNEL_ID, channelId)
                putString(ARG_CHANNEL_TYPE, channelType)
            }
        }
    }

    private class OptionsAdapter(
        private val onOptionClick: ((Option) -> Unit),
    ) : ListAdapter<OptionsAdapter.OptionItem, OptionsAdapter.OptionViewHolder>(OptionsDiffCallback) {

        fun setPoll(poll: Poll) {
            submitList(
                poll.options.map {
                    OptionItem(
                        option = it,
                        votes = poll.voteCountsByOption[it.id] ?: 0,
                        isVotedByUser = poll.ownVotes.any { vote -> vote.optionId == it.id },
                        readonly = poll.closed,
                    )
                },
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder =
            OptionViewHolder(
                StreamUiItemOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                onOptionClick,
            )

        override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        private data class OptionItem(
            val option: Option,
            val votes: Int,
            val isVotedByUser: Boolean,
            val readonly: Boolean,
        )

        private class OptionViewHolder(
            private val binding: StreamUiItemOptionBinding,
            private val onOptionClick: (Option) -> Unit,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(result: OptionItem) {
                binding.root.setOnClickListener { onOptionClick(result.option) }
                binding.option.text = result.option.text
                binding.votes.text = result.votes.toString()
                binding.check.isEnabled = result.isVotedByUser
                binding.check.isVisible = !result.readonly
            }
        }

        private object OptionsDiffCallback : DiffUtil.ItemCallback<OptionItem>() {
            override fun areContentsTheSame(oldItem: OptionItem, newItem: OptionItem): Boolean = oldItem == newItem
            override fun areItemsTheSame(oldItem: OptionItem, newItem: OptionItem): Boolean =
                oldItem.option.id == newItem.option.id
        }
    }

    private class AllPollOptionsViewModel(
        private val chatClient: ChatClient = ChatClient.instance(),
        private val messageId: String,
        channelId: String,
        channelType: String,
        private val coroutineScope: CoroutineScope,
    ) : ViewModel() {

        fun onOptionClick(option: Option) {
            coroutineScope.launch {
                pollState.value
                    ?.let { poll ->
                        poll.ownVotes.firstOrNull { it.optionId == option.id }
                            ?.let { chatClient.removePollVote(messageId, poll.id, it.id) }
                            ?: chatClient.castPollVote(messageId, poll.id, option.id)
                    }?.await()
            }
        }

        val pollState: StateFlow<Poll?> = chatClient.state
            .channel(channelType, channelId)
            .messages
            .mapLatest { it.firstOrNull { it.id == messageId }?.poll }
            .stateIn(coroutineScope, SharingStarted.Eagerly, null)
    }
}
