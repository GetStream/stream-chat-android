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
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewAction
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewEvent
import io.getstream.chat.android.ui.databinding.StreamUiFragmentPollOptionVotesBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemPollOptionHeaderBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemResultUserBinding
import io.getstream.chat.android.ui.utils.extensions.applyEdgeToEdgePadding
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.viewmodel.messages.PollOptionVotesViewModel
import io.getstream.chat.android.ui.widgets.EndlessScrollListener
import io.getstream.log.taggedLogger

internal class PollOptionVotesDialogFragment : AppCompatDialogFragment() {

    private val logger by taggedLogger("Chat:PollOptionVotesDialogFragment")

    private var _binding: StreamUiFragmentPollOptionVotesBinding? = null
    private val binding get() = _binding!!

    private val pollId: String
        get() = requireArguments().getString(ARG_POLL)
            ?: throw IllegalStateException("Poll ID not found in arguments")

    private val optionId: String
        get() = requireArguments().getString(ARG_OPTION)
            ?: throw IllegalStateException("Option ID not found in arguments")

    private val poll: Poll by lazy {
        polls[pollId] ?: throw IllegalStateException("Poll not found for ID: $pollId")
    }

    private val option: Option by lazy {
        options[optionId] ?: throw IllegalStateException("Option not found for ID: $optionId")
    }

    private val viewModel: PollOptionVotesViewModel by viewModels {
        PollOptionVotesViewModel.Factory(poll = poll, option = option)
    }

    private val votesAdapter = UserVoteAdapter()

    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        viewModel.onViewAction(PollOptionVotesViewAction.LoadMoreRequested)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = StreamUiFragmentPollOptionVotesBinding
            .inflate(requireContext().streamThemeInflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEdgeToEdge()
        setupToolbar(binding.toolbar)
        setupVotesList()
        observeState()
        observeEvents()
    }

    private fun setupEdgeToEdge() {
        binding.root.applyEdgeToEdgePadding(typeMask = WindowInsetsCompat.Type.systemBars())
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationOnClickListener { dismiss() }
        ContextCompat.getDrawable(requireContext(), R.drawable.stream_ui_arrow_left)?.apply {
            setTint(ContextCompat.getColor(requireContext(), R.color.stream_ui_black))
        }?.let(toolbar::setNavigationIcon)
        toolbar.title = option.text
    }

    private fun setupVotesList() {
        binding.voteList.adapter = votesAdapter
        binding.voteList.addOnScrollListener(scrollListener)
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.toolbar.title = state.option.text
            binding.voteList.isInvisible = state.isLoading
            binding.loadingContainer.isVisible = state.isLoading
            binding.loadingMoreProgress.isVisible = state.isLoadingMore && !state.isLoading
            val items = buildList {
                add(PollOptionVotesListItem.Header(isWinner = state.isWinner, voteCount = state.voteCount))
                addAll(state.results.map(PollOptionVotesListItem::VoteItem))
            }
            votesAdapter.submitList(items)
            if (!state.isLoading && !state.isLoadingMore && state.canLoadMore) {
                scrollListener.enablePagination()
            } else {
                scrollListener.disablePagination()
            }
        }
    }

    private fun observeEvents() {
        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is PollOptionVotesViewEvent.LoadError -> {
                    logger.e { "[observeEvents] error loading poll option results: ${event.error}" }
                    val errorMessage = getString(R.string.stream_ui_poll_option_results_error)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity?.isChangingConfigurations != true) {
            polls.remove(pollId)
            options.remove(optionId)
        }
    }

    companion object {
        const val TAG: String = "PollOptionVotesDialogFragment"
        private const val ARG_POLL: String = "arg_poll"
        private const val ARG_OPTION: String = "arg_option"
        private const val LOAD_MORE_THRESHOLD = 3
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_VOTE = 1

        private val polls = mutableMapOf<String, Poll>()
        private val options = mutableMapOf<String, Option>()

        fun newInstance(poll: Poll, option: Option): PollOptionVotesDialogFragment =
            PollOptionVotesDialogFragment().apply {
                polls[poll.id] = poll
                options[option.id] = option
                arguments = bundleOf(
                    ARG_POLL to poll.id,
                    ARG_OPTION to option.id,
                )
            }
    }

    private sealed interface PollOptionVotesListItem {
        data class Header(val isWinner: Boolean, val voteCount: Int) : PollOptionVotesListItem
        data class VoteItem(val vote: Vote) : PollOptionVotesListItem
    }

    private class UserVoteAdapter :
        ListAdapter<PollOptionVotesListItem, RecyclerView.ViewHolder>(UserVoteDiffCallback) {

        override fun getItemViewType(position: Int): Int =
            when (getItem(position)) {
                is PollOptionVotesListItem.Header -> VIEW_TYPE_HEADER
                is PollOptionVotesListItem.VoteItem -> VIEW_TYPE_VOTE
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                VIEW_TYPE_HEADER -> HeaderViewHolder(
                    StreamUiItemPollOptionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                )

                else -> UserVoteViewHolder(
                    StreamUiItemResultUserBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                )
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = getItem(position)) {
                is PollOptionVotesListItem.Header -> (holder as HeaderViewHolder).bind(item)
                is PollOptionVotesListItem.VoteItem -> (holder as UserVoteViewHolder).bind(item.vote)
            }
        }

        private class HeaderViewHolder(
            private val binding: StreamUiItemPollOptionHeaderBinding,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: PollOptionVotesListItem.Header) {
                binding.award.isVisible = item.isWinner
                binding.voteCount.text = binding.root.context.getString(
                    R.string.stream_ui_poll_vote_counts,
                    item.voteCount,
                )
            }
        }

        private class UserVoteViewHolder(
            private val binding: StreamUiItemResultUserBinding,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(vote: Vote) {
                val user = vote.user ?: return
                binding.name.text = user.name
                binding.userAvatarView.setUser(user)
                binding.date.text = ChatUI.dateFormatter.formatRelativeDate(vote.createdAt)
                binding.time.text = ChatUI.dateFormatter.formatTime(vote.createdAt)
            }
        }

        private object UserVoteDiffCallback : DiffUtil.ItemCallback<PollOptionVotesListItem>() {
            override fun areItemsTheSame(
                oldItem: PollOptionVotesListItem,
                newItem: PollOptionVotesListItem,
            ): Boolean = when {
                oldItem is PollOptionVotesListItem.Header && newItem is PollOptionVotesListItem.Header ->
                    true
                oldItem is PollOptionVotesListItem.VoteItem && newItem is PollOptionVotesListItem.VoteItem ->
                    oldItem.vote.id == newItem.vote.id

                else -> false
            }

            override fun areContentsTheSame(
                oldItem: PollOptionVotesListItem,
                newItem: PollOptionVotesListItem,
            ): Boolean = oldItem == newItem
        }
    }
}
