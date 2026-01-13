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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.feature.messages.poll.PollResultsViewEvent
import io.getstream.chat.android.ui.common.state.messages.poll.PollResultsViewState
import io.getstream.chat.android.ui.databinding.StreamUiFragmentPollResultsBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemResultBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemResultUserBinding
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.viewmodel.messages.PollResultsViewModel

/**
 * Represent the bottom sheet dialog that allows users to pick attachments.
 */
public class PollResultsDialogFragment : AppCompatDialogFragment() {

    private var _binding: StreamUiFragmentPollResultsBinding? = null
    private val binding get() = _binding!!

    private val pollId: String
        get() = requireArguments().getString(ARG_POLL)
            ?: throw IllegalStateException("Poll ID not found in arguments")

    private val poll: Poll by lazy {
        polls[pollId] ?: throw IllegalStateException("Poll not found for ID: $pollId")
    }

    private val viewModel: PollResultsViewModel by viewModels {
        PollResultsViewModel.Factory(poll)
    }

    private val resultsAdapter = ResultsAdapter(::onShowAllVotesClick)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incrementPollReference(pollId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = StreamUiFragmentPollResultsBinding.inflate(requireContext().streamThemeInflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar)
        binding.optionList.adapter = resultsAdapter
        observeState()
        observeEvents()
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationOnClickListener { dismiss() }
        ContextCompat.getDrawable(requireContext(), R.drawable.stream_ui_arrow_left)?.apply {
            setTint(ContextCompat.getColor(requireContext(), R.color.stream_ui_black))
        }?.let(toolbar::setNavigationIcon)
        toolbar.setTitle(getString(R.string.stream_ui_poll_results_title))
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.question.text = state.pollName
            resultsAdapter.submitList(state.results)
            binding.loadingContainer.isVisible = false
        }
    }

    private fun observeEvents() {
        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is PollResultsViewEvent.LoadError -> {
                    val errorMessage = getString(R.string.stream_ui_poll_view_results_error)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onShowAllVotesClick(option: Option) {
        PollOptionVotesDialogFragment.newInstance(poll = poll, option = option)
            .show(parentFragmentManager, PollOptionVotesDialogFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        decrementPollReference(pollId)
    }

    public companion object {
        public const val TAG: String = "PollResultsDialogFragment"
        private const val ARG_POLL: String = "arg_poll"

        // Store polls temporarily to handle configuration changes
        // Use reference counting to handle multiple fragments or configuration changes
        private val polls = mutableMapOf<String, Poll>()
        private val pollReferenceCounts = mutableMapOf<String, Int>()

        /**
         * Creates a new instance of [PollResultsDialogFragment].
         *
         * @param poll The poll to display results for.
         * @return A new instance of [PollResultsDialogFragment].
         */
        public fun newInstance(poll: Poll): PollResultsDialogFragment =
            PollResultsDialogFragment().apply {
                polls[poll.id] = poll
                incrementPollReference(poll.id)
                arguments = bundleOf(ARG_POLL to poll.id)
            }

        private fun incrementPollReference(pollId: String) {
            pollReferenceCounts[pollId] = (pollReferenceCounts[pollId] ?: 0) + 1
        }

        private fun decrementPollReference(pollId: String) {
            val count = (pollReferenceCounts[pollId] ?: 0) - 1
            if (count <= 0) {
                polls.remove(pollId)
                pollReferenceCounts.remove(pollId)
            } else {
                pollReferenceCounts[pollId] = count
            }
        }
    }

    private class ResultsAdapter(
        private val onShowAllVotesClick: (Option) -> Unit,
    ) : androidx.recyclerview.widget.ListAdapter<PollResultsViewState.ResultItem, ResultsAdapter.ResultViewHolder>(
        object : DiffUtil.ItemCallback<PollResultsViewState.ResultItem>() {
            override fun areItemsTheSame(
                oldItem: PollResultsViewState.ResultItem,
                newItem: PollResultsViewState.ResultItem,
            ): Boolean = oldItem.option.id == newItem.option.id

            override fun areContentsTheSame(
                oldItem: PollResultsViewState.ResultItem,
                newItem: PollResultsViewState.ResultItem,
            ): Boolean = oldItem == newItem
        },
    ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder =
            ResultViewHolder(
                StreamUiItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                onShowAllVotesClick,
            )

        override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        private class ResultViewHolder(
            private val binding: StreamUiItemResultBinding,
            private val onShowAllVotesClick: (Option) -> Unit,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(result: PollResultsViewState.ResultItem) {
                binding.option.text = result.option.text
                binding.votes.text = binding.root.resources.getString(
                    R.string.stream_ui_poll_vote_counts,
                    result.voteCount,
                )
                val adapter = (binding.optionList.adapter as? UserVoteAdapter) ?: UserVoteAdapter().also {
                    binding.optionList.adapter = it
                }
                adapter.submitList(result.votes)
                binding.award.isVisible = result.isWinner
                binding.showAll.isVisible = result.showAllButton
                binding.showAll.setOnClickListener { onShowAllVotesClick(result.option) }
            }
        }
    }

    private class UserVoteAdapter :
        androidx.recyclerview.widget.ListAdapter<Vote, UserVoteAdapter.UserVoteViewHolder>(UserVoteDiffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVoteViewHolder =
            UserVoteViewHolder(
                StreamUiItemResultUserBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            )

        override fun onBindViewHolder(holder: UserVoteViewHolder, position: Int) {
            holder.bind(getItem(position))
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

        private object UserVoteDiffCallback : DiffUtil.ItemCallback<Vote>() {
            override fun areItemsTheSame(oldItem: Vote, newItem: Vote): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Vote, newItem: Vote): Boolean = oldItem == newItem
        }
    }
}
