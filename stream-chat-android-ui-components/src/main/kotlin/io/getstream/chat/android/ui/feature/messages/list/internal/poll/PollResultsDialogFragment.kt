/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.extensions.internal.getWinner
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiFragmentPollResultsBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemResultBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemResultUserBinding
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Represent the bottom sheet dialog that allows users to pick attachments.
 */
public class PollResultsDialogFragment : AppCompatDialogFragment() {

    private var _binding: StreamUiFragmentPollResultsBinding? = null
    private val binding get() = _binding!!

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
        (polls[arguments?.getString(ARG_POLL)])?.let {
            setupDialog(it)
        } ?: dismiss()
    }

    /**
     * Initializes the dialog.
     */
    private fun setupDialog(poll: Poll) {
        setupToolbar(binding.toolbar)
        binding.question.text = poll.name
        binding.optionList.adapter = ResultsAdapter(poll)
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationOnClickListener { dismiss() }
        ContextCompat.getDrawable(requireContext(), R.drawable.stream_ui_arrow_left)?.apply {
            setTint(ContextCompat.getColor(requireContext(), R.color.stream_ui_black))
        }?.let(toolbar::setNavigationIcon)
        toolbar.setTitle(getString(R.string.stream_ui_poll_results_title))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    public companion object {
        public const val TAG: String = "create_poll_dialog_fragment"
        private const val ARG_POLL: String = "arg_poll"
        private val polls = mutableMapOf<String, Poll>()

        /**
         * Creates a new instance of [PollResultsDialogFragment].
         *
         * @return A new instance of [PollResultsDialogFragment].
         */
        public fun newInstance(poll: Poll): PollResultsDialogFragment = PollResultsDialogFragment().apply {
            polls[poll.id] = poll
            this.arguments = bundleOf(ARG_POLL to poll.id)
        }
    }

    private class ResultsAdapter(
        private val poll: Poll,
    ) : RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>() {

        private val winner = poll.getWinner()

        val results: List<ResultItem> = poll.options.map { option ->
            ResultItem(
                option = option.text,
                votes = poll.voteCountsByOption[option.id] ?: 0,
                users = poll.votes.filter { it.optionId == option.id }.filter { it.user != null },
                isWinner = winner == option,
            )
        }.sortedByDescending { it.votes }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder =
            ResultViewHolder(StreamUiItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = results.size

        override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
            holder.bind(results[position])
        }

        private data class ResultItem(
            val option: String,
            val votes: Int,
            val users: List<Vote>,
            val isWinner: Boolean,
        )

        private class ResultViewHolder(
            private val binding: StreamUiItemResultBinding,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(result: ResultItem) {
                binding.option.text = result.option
                binding.votes.text = binding.root.resources.getString(R.string.stream_ui_poll_vote_counts, result.votes)
                binding.optionList.adapter = UserVoteAdapter(result.users)
                binding.award.isVisible = result.isWinner
            }
        }
    }

    private class UserVoteAdapter(
        private val votes: List<Vote>,
    ) : RecyclerView.Adapter<UserVoteAdapter.UserVoteViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVoteViewHolder =
            UserVoteViewHolder(
                StreamUiItemResultUserBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            )

        override fun getItemCount(): Int = votes.size

        override fun onBindViewHolder(holder: UserVoteViewHolder, position: Int) {
            holder.bind(votes[position])
        }

        private class UserVoteViewHolder(
            private val binding: StreamUiItemResultUserBinding,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(vote: Vote) {
                vote.user?.let { user ->
                    binding.name.text = user.name
                    binding.userAvatarView.setUser(user)
                }
                binding.date.text = ChatUI.dateFormatter.formatRelativeDate(vote.createdAt)
                binding.time.text = ChatUI.dateFormatter.formatTime(vote.createdAt)
            }
        }
    }
}
