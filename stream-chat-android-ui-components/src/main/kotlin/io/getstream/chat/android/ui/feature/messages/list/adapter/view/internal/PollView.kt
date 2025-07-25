/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.getstream.chat.android.client.extensions.internal.getVotesUnlessAnonymous
import io.getstream.chat.android.client.extensions.internal.getWinner
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.utils.PollsConstants
import io.getstream.chat.android.ui.common.utils.extensions.getSubtitle
import io.getstream.chat.android.ui.databinding.StreamUiItemPollAnswerBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemPollCloseBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemPollHeaderBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemPollResultsBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemPollShowAllOptionsBinding
import io.getstream.chat.android.ui.feature.messages.list.adapter.view.PollViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.log.taggedLogger

internal class PollView : RecyclerView {

    private val logger by taggedLogger("PollView")

    private lateinit var style: PollViewStyle

    private lateinit var pollAdapter: PollAdapter
    var onOptionClick: ((Option) -> Unit) = { _ -> }
    var onClosePollClick: ((Poll) -> Unit) = { _ -> }
    var onViewPollResultsClick: ((Poll) -> Unit) = { _ -> }
    var onShowAllPollOptionClick: (() -> Unit) = { }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(attrs)
    }

    init {
        layoutManager = LinearLayoutManager(context)
        itemAnimator = null
    }

    private fun init(attrs: AttributeSet?) {
        style = PollViewStyle(context, attrs)
    }

    fun setPoll(poll: Poll, isMine: Boolean) {
        logger.d { "[setPoll] poll: $poll" }
        if (!::pollAdapter.isInitialized) {
            pollAdapter = PollAdapter(
                pollViewStyle = style,
                onOptionClick = { option -> onOptionClick(option) },
                onClosePollClick = { onClosePollClick(poll) },
                onViewPollResultsClick = { onViewPollResultsClick(poll) },
                onShowAllOptionsClick = { onShowAllPollOptionClick() },
            )
            adapter = pollAdapter
        }

        val pollItems = mutableListOf<PollItem>()
        pollItems.add(
            PollItem.Header(
                title = poll.name,
                subtitle = poll.getSubtitle(context),
            ),
        )

        val winner = poll.getWinner()

        pollItems.addAll(
            poll.options
                .take(PollsConstants.MIN_NUMBER_OF_VISIBLE_OPTIONS)
                .map { option ->
                    PollItem.Answer(
                        option = option,
                        votes = poll.getVotesUnlessAnonymous(option),
                        voteCount = poll.voteCountsByOption[option.id] ?: 0,
                        isVotedByUser = poll.ownVotes.any { it.optionId == option.id },
                        totalVotes = poll.voteCountsByOption.values.sum(),
                        closed = poll.closed,
                        isWinner = winner == option,
                    )
                },
        )

        PollItem.ShowAllOptions(count = poll.options.size)
            .takeIf { poll.options.size > PollsConstants.MIN_NUMBER_OF_VISIBLE_OPTIONS }
            ?.let(pollItems::add)

        pollItems.add(PollItem.ViewResults)

        PollItem.Close.takeIf { isMine && !poll.closed }
            ?.let(pollItems::add)

        pollAdapter.submitList(pollItems)
    }
}

private class PollAdapter(
    private val pollViewStyle: PollViewStyle,
    private val onOptionClick: (Option) -> Unit,
    private val onClosePollClick: () -> Unit,
    private val onViewPollResultsClick: () -> Unit,
    private val onShowAllOptionsClick: () -> Unit,
) : ListAdapter<PollItem, PollItemViewHolder<out PollItem>>(PollItemDiffCallback) {

    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_ANSWER = 2
        private const val VIEW_TYPE_CLOSE = 3
        private const val VIEW_TYPE_RESULTS = 4
        private const val VIEW_TYPE_SHOW_ALL_OPTIONS = 5
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollItemViewHolder<out PollItem> {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                StreamUiItemPollHeaderBinding.inflate(parent.streamThemeInflater, parent, false)
                    .applyStyle(pollViewStyle),
            )

            VIEW_TYPE_ANSWER -> AnswerViewHolder(
                StreamUiItemPollAnswerBinding.inflate(parent.streamThemeInflater, parent, false)
                    .applyStyle(pollViewStyle),
                onOptionClick,
            )

            VIEW_TYPE_CLOSE -> CloseViewHolder(
                StreamUiItemPollCloseBinding.inflate(parent.streamThemeInflater, parent, false)
                    .applyStyle(pollViewStyle),
                onClosePollClick,
            )

            VIEW_TYPE_RESULTS -> ViewResultsViewHolder(
                StreamUiItemPollResultsBinding.inflate(parent.streamThemeInflater, parent, false)
                    .applyStyle(pollViewStyle),
                onViewPollResultsClick,
            )

            VIEW_TYPE_SHOW_ALL_OPTIONS -> ShowAllOptionsViewHolder(
                StreamUiItemPollShowAllOptionsBinding.inflate(parent.streamThemeInflater, parent, false)
                    .applyStyle(pollViewStyle),
                onShowAllOptionsClick,
            )

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is PollItem.Header -> VIEW_TYPE_HEADER
        is PollItem.Answer -> VIEW_TYPE_ANSWER
        PollItem.Close -> VIEW_TYPE_CLOSE
        PollItem.ViewResults -> VIEW_TYPE_RESULTS
        is PollItem.ShowAllOptions -> VIEW_TYPE_SHOW_ALL_OPTIONS
    }

    override fun onBindViewHolder(holder: PollItemViewHolder<out PollItem>, position: Int) {
        holder.bindPollItem(getItem(position))
    }
}

private object PollItemDiffCallback : DiffUtil.ItemCallback<PollItem>() {
    override fun areItemsTheSame(oldItem: PollItem, newItem: PollItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PollItem, newItem: PollItem): Boolean {
        return oldItem == newItem
    }
}

private sealed class PollItem {

    data class Header(
        val title: String,
        val subtitle: String,
    ) : PollItem()

    data class Answer(
        val option: Option,
        val votes: List<Vote>,
        val isVotedByUser: Boolean,
        val voteCount: Int,
        val totalVotes: Int,
        val closed: Boolean,
        val isWinner: Boolean,
    ) : PollItem()

    data object Close : PollItem()
    data class ShowAllOptions(val count: Int) : PollItem()
    data object ViewResults : PollItem()
}

private sealed class PollItemViewHolder<T : PollItem>(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bindPollItem(pollItem: PollItem) = bind(pollItem as T)
    abstract fun bind(pollItem: T)
}

private class HeaderViewHolder(
    private val binding: StreamUiItemPollHeaderBinding,
) : PollItemViewHolder<PollItem.Header>(binding) {
    override fun bind(pollItem: PollItem.Header) {
        binding.title.text = pollItem.title
        binding.subtitle.text = pollItem.subtitle
        binding.subtitle.isVisible = pollItem.subtitle.isNotBlank()
    }
}

private class AnswerViewHolder(
    private val binding: StreamUiItemPollAnswerBinding,
    private val onOptionClick: (Option) -> Unit,
) : PollItemViewHolder<PollItem.Answer>(binding) {
    override fun bind(pollItem: PollItem.Answer) {
        binding.root.isEnabled = !pollItem.closed
        binding.check.isVisible = !pollItem.closed
        if (!pollItem.closed) {
            binding.root.setOnClickListener {
                val newVotes = pollItem.voteCount + pollItem.isVotedByUser.invertVoteCount()
                drawVotes(newVotes, pollItem.totalVotes)
                binding.check.isEnabled = !binding.check.isEnabled
                onOptionClick(pollItem.option)
            }
        }
        binding.check.isEnabled = pollItem.isVotedByUser
        binding.option.text = pollItem.option.text
        binding.votesPercentage.setIndicatorColor(
            if (pollItem.isVotedByUser) {
                ContextCompat.getColor(binding.root.context, R.color.stream_ui_accent_green)
            } else {
                ContextCompat.getColor(binding.root.context, R.color.stream_ui_accent_blue)
            },
        )
        drawVotes(pollItem.voteCount, pollItem.totalVotes)
        pollItem.votes
            .firstOrNull()
            ?.user
            ?.let {
                binding.avatarFirstVote.apply {
                    setUser(it, false)
                    isVisible = true
                }
            } ?: run { binding.avatarFirstVote.isVisible = false }
        pollItem.votes
            .drop(1)
            .firstOrNull()
            ?.user
            ?.let {
                binding.avatarSecondVote.apply {
                    setUser(it, false)
                    isVisible = true
                }
            } ?: run { binding.avatarSecondVote.isVisible = false }
    }

    @Suppress("MagicNumber")
    private fun drawProgress(optionVotes: Int, totalVotes: Int) {
        binding.votesPercentage.progress = when (totalVotes) {
            0 -> 0
            else -> (optionVotes.toFloat() / totalVotes.toFloat() * 100).toInt()
        }
    }

    private fun drawVotes(optionVotes: Int, totalVotes: Int) {
        binding.votes.text = (optionVotes).toString()
        drawProgress(optionVotes, totalVotes)
    }

    private fun Boolean.invertVoteCount(): Int = if (this) -1 else 1
}

private class CloseViewHolder(
    private val binding: StreamUiItemPollCloseBinding,
    private val onClosePoll: () -> Unit,
) : PollItemViewHolder<PollItem.Close>(binding) {
    override fun bind(pollItem: PollItem.Close) {
        binding.root.setOnClickListener { onClosePoll() }
    }
}

private class ShowAllOptionsViewHolder(
    private val binding: StreamUiItemPollShowAllOptionsBinding,
    private val onShowAllOptions: () -> Unit,
) : PollItemViewHolder<PollItem.ShowAllOptions>(binding) {
    override fun bind(pollItem: PollItem.ShowAllOptions) {
        binding.pollShowAllOptions.text = binding.root.context.getString(
            R.string.stream_ui_poll_action_see_all,
            pollItem.count,
        )
        binding.root.setOnClickListener { onShowAllOptions() }
    }
}

private class ViewResultsViewHolder(
    private val binding: StreamUiItemPollResultsBinding,
    private val onViewPollResultsClick: () -> Unit,
) : PollItemViewHolder<PollItem.ViewResults>(binding) {
    override fun bind(pollItem: PollItem.ViewResults) {
        binding.root.setOnClickListener { onViewPollResultsClick() }
    }
}

private fun StreamUiItemPollHeaderBinding.applyStyle(style: PollViewStyle) = this.apply {
    title.setTextStyle(style.pollTitleTextStyle)
    subtitle.setTextStyle(style.pollSubtitleTextStyle)
}

private fun StreamUiItemPollAnswerBinding.applyStyle(style: PollViewStyle) = this.apply {
    check.setImageDrawable(style.pollOptionCheckDrawable.constantState?.newDrawable())
    option.setTextStyle(style.pollOptionTextStyle)
    votes.setTextStyle(style.pollOptionVotesTextStyle)
}

private fun StreamUiItemPollCloseBinding.applyStyle(style: PollViewStyle) = this.apply {
    pollClose.setTextStyle(style.pollCloseTextStyle)
}

private fun StreamUiItemPollResultsBinding.applyStyle(style: PollViewStyle) = this.apply {
    pollResults.setTextStyle(style.pollResultsTextStyle)
}

private fun StreamUiItemPollShowAllOptionsBinding.applyStyle(style: PollViewStyle) = this.apply {
    pollShowAllOptions.setTextStyle(style.pollShowAllOptionsTextStyle)
}
