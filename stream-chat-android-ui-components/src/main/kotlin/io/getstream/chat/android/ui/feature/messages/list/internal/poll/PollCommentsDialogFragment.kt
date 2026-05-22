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
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiFragmentPollCommentsBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemPollCommentBinding
import io.getstream.chat.android.ui.utils.extensions.applyEdgeToEdgePadding
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.viewmodel.messages.PollCommentsViewModel

internal class PollCommentsDialogFragment : AppCompatDialogFragment() {

    private var _binding: StreamUiFragmentPollCommentsBinding? = null
    private val binding get() = _binding!!

    private val cid: String
        get() = requireArguments().getString(ARG_CID) ?: error("Channel cid not found in arguments")

    private val messageId: String
        get() = requireArguments().getString(ARG_MESSAGE_ID) ?: error("Message ID not found in arguments")

    private val viewModel: PollCommentsViewModel by viewModels {
        PollCommentsViewModel.Factory(cid = cid, messageId = messageId)
    }

    private val commentsAdapter = CommentsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = StreamUiFragmentPollCommentsBinding.inflate(requireContext().streamThemeInflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applyEdgeToEdgePadding(typeMask = WindowInsetsCompat.Type.systemBars())
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        binding.commentList.adapter = commentsAdapter
        viewModel.poll.observe(viewLifecycleOwner, ::render)
    }

    private fun render(poll: Poll) {
        val showUser = poll.votingVisibility == VotingVisibility.PUBLIC
        commentsAdapter.submitList(poll.answers.map { CommentItem(it, showUser) })
        binding.ctaContainer.isVisible = !poll.closed
        if (poll.closed) return
        val ownAnswer = ChatUI.currentUserProvider.getCurrentUser()?.id
            ?.let { id -> poll.answers.firstOrNull { it.user?.id == id } }
        binding.ctaButton.setText(
            if (ownAnswer == null) {
                R.string.stream_ui_poll_add_a_comment_label
            } else {
                R.string.stream_ui_poll_update_comment_label
            },
        )
        binding.ctaButton.setOnClickListener {
            AddPollCommentDialogFragment
                .newInstance(messageId = messageId, pollId = poll.id, initialText = ownAnswer?.text)
                .show(parentFragmentManager, AddPollCommentDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG: String = "PollCommentsDialogFragment"
        private const val ARG_CID: String = "arg_cid"
        private const val ARG_MESSAGE_ID: String = "arg_message_id"

        fun newInstance(cid: String, messageId: String): PollCommentsDialogFragment =
            PollCommentsDialogFragment().apply {
                arguments = bundleOf(
                    ARG_CID to cid,
                    ARG_MESSAGE_ID to messageId,
                )
            }
    }

    private data class CommentItem(val answer: Answer, val showUser: Boolean)

    private class CommentsAdapter :
        ListAdapter<CommentItem, CommentsAdapter.CommentViewHolder>(CommentDiffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder =
            CommentViewHolder(
                StreamUiItemPollCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            )

        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        private object CommentDiffCallback : DiffUtil.ItemCallback<CommentItem>() {
            override fun areItemsTheSame(oldItem: CommentItem, newItem: CommentItem): Boolean =
                oldItem.answer.id == newItem.answer.id

            override fun areContentsTheSame(oldItem: CommentItem, newItem: CommentItem): Boolean =
                oldItem == newItem
        }

        private class CommentViewHolder(
            private val binding: StreamUiItemPollCommentBinding,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: CommentItem) {
                val answer = item.answer
                binding.answerText.text = answer.text
                val user = answer.user?.takeIf { item.showUser }
                if (user != null) {
                    binding.name.text = user.name
                    binding.userAvatarView.setUser(user)
                    binding.userAvatarView.isVisible = true
                } else {
                    binding.name.text = ""
                    binding.userAvatarView.isInvisible = true
                }
                binding.date.text = ChatUI.dateFormatter.formatRelativeDate(answer.createdAt)
                binding.time.text = ChatUI.dateFormatter.formatTime(answer.createdAt)
            }
        }
    }
}
