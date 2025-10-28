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

package io.getstream.chat.android.ui.feature.messages.list.reactions.user.internal

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.context
import io.getstream.chat.android.ui.common.state.messages.list.MessageOptionsUserReactionAlignment
import io.getstream.chat.android.ui.common.state.messages.list.isStartAlignment
import io.getstream.chat.android.ui.databinding.StreamUiItemUserReactionBinding
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.extensions.updateConstraints

internal class UserReactionAdapter(
    private val userReactionClickListener: UserReactionClickListener,
) : ListAdapter<UserReactionItem, UserReactionAdapter.UserReactionViewHolder>(UserReactionItemDiffCallback) {

    var messageOptionsUserReactionAlignment: MessageOptionsUserReactionAlignment =
        MessageOptionsUserReactionAlignment.BY_USER
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReactionViewHolder = StreamUiItemUserReactionBinding
        .inflate(parent.streamThemeInflater, parent, false)
        .let { UserReactionViewHolder(it, userReactionClickListener, messageOptionsUserReactionAlignment) }

    override fun onBindViewHolder(holder: UserReactionViewHolder, position: Int) = holder.bind(getItem(position))

    class UserReactionViewHolder(
        private val binding: StreamUiItemUserReactionBinding,
        private val userReactionClickListener: UserReactionClickListener,
        private val messageOptionsUserReactionAlignment: MessageOptionsUserReactionAlignment =
            MessageOptionsUserReactionAlignment.BY_USER,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var userReactionItem: UserReactionItem

        init {
            binding.root.setOnClickListener { userReactionClickListener.onUserReactionClick(userReactionItem) }
        }

        fun bind(userReactionItem: UserReactionItem) {
            this.userReactionItem = userReactionItem
            bindUserAvatar()
            bindUserName()
            bindUserReaction()
        }

        private fun bindUserAvatar() {
            binding.userAvatarView.setUser(userReactionItem.user)
        }

        private fun bindUserName() {
            binding.userNameTextView.text = userReactionItem.user.name
        }

        private fun bindUserReaction() {
            binding.apply {
                reactionContainer.updateConstraints {
                    clear(R.id.userReactionView, ConstraintSet.START)
                    clear(R.id.userReactionView, ConstraintSet.END)
                }
                val isEndAlignment = !messageOptionsUserReactionAlignment.isStartAlignment(userReactionItem.isMine)

                userReactionView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    if (isEndAlignment) {
                        endToEnd = ConstraintSet.PARENT_ID
                        marginEnd = context.getDimension(R.dimen.stream_ui_spacing_small)
                    } else {
                        startToStart = ConstraintSet.PARENT_ID
                        marginStart = context.getDimension(R.dimen.stream_ui_spacing_small)
                    }
                }
                userReactionView.setReaction(userReactionItem)
            }
        }
    }

    private object UserReactionItemDiffCallback : DiffUtil.ItemCallback<UserReactionItem>() {
        override fun areItemsTheSame(oldItem: UserReactionItem, newItem: UserReactionItem): Boolean = oldItem.user.id == newItem.user.id &&
            oldItem.reaction.type == newItem.reaction.type

        override fun areContentsTheSame(oldItem: UserReactionItem, newItem: UserReactionItem): Boolean = oldItem == newItem
    }

    internal fun interface UserReactionClickListener {
        fun onUserReactionClick(userReaction: UserReactionItem)
    }
}
