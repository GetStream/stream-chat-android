package io.getstream.chat.android.ui.messages.reactions.user

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemUserReactionBinding
import io.getstream.chat.android.ui.utils.extensions.context
import io.getstream.chat.android.ui.utils.extensions.getDimension

internal class UserReactionAdapter :
    ListAdapter<UserReactionItem, UserReactionAdapter.UserReactionViewHolder>(UserReactionItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReactionViewHolder {
        return StreamUiItemUserReactionBinding
            .inflate(parent.context.inflater, parent, false)
            .let(::UserReactionViewHolder)
    }

    override fun onBindViewHolder(holder: UserReactionViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    class UserReactionViewHolder(
        private val binding: StreamUiItemUserReactionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var userReactionItem: UserReactionItem

        fun bind(userReactionItem: UserReactionItem) {
            this.userReactionItem = userReactionItem
            bindUserAvatar()
            bindUserName()
            bindUserReaction()
        }

        private fun bindUserAvatar() {
            binding.avatarView.setUserData(userReactionItem.user)
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
                userReactionView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    if (userReactionItem.isMine) {
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
        override fun areItemsTheSame(oldItem: UserReactionItem, newItem: UserReactionItem): Boolean {
            return oldItem.user.id == newItem.user.id &&
                oldItem.reaction.type == newItem.reaction.type
        }

        override fun areContentsTheSame(oldItem: UserReactionItem, newItem: UserReactionItem): Boolean {
            return oldItem == newItem
        }
    }
}
