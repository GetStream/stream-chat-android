package io.getstream.chat.android.ui.messages.reactions.user

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemUserReactionBinding
import io.getstream.chat.android.ui.messages.reactions.ReactionClickListener
import io.getstream.chat.android.ui.messages.reactions.ReactionItem
import io.getstream.chat.android.ui.messages.reactions.ReactionItemDiffCallback
import io.getstream.chat.android.ui.utils.extensions.context
import io.getstream.chat.android.ui.utils.extensions.getDimension

internal class UserReactionAdapter(
    private val reactionClickListener: ReactionClickListener
) : ListAdapter<ReactionItem, UserReactionAdapter.UserReactionViewHolder>(ReactionItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReactionViewHolder {
        return StreamUiItemUserReactionBinding
            .inflate(parent.context.inflater, parent, false)
            .let { UserReactionViewHolder(it, reactionClickListener) }
    }

    override fun onBindViewHolder(holder: UserReactionViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    class UserReactionViewHolder(
        private val binding: StreamUiItemUserReactionBinding,
        private val reactionClickListener: ReactionClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var reactionItem: ReactionItem

        init {
            binding.root.setOnClickListener {
                reactionClickListener.onReactionClick(reactionItem.reaction)
            }
        }

        fun bind(reaction: ReactionItem) {
            this.reactionItem = reaction
            bindUserAvatar()
            bindUserName()
            bindUserReaction()
        }

        private fun bindUserAvatar() {
            val user = reactionItem.reaction.user
            binding.avatarView.setUserData(user!!)
        }

        private fun bindUserName() {
            binding.userNameTextView.text = reactionItem.reaction.user?.name
        }

        private fun bindUserReaction() {
            binding.apply {
                reactionContainer.updateConstraints {
                    clear(R.id.userReactionView, ConstraintSet.START)
                    clear(R.id.userReactionView, ConstraintSet.END)
                }
                userReactionView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    if (reactionItem.isMine) {
                        endToEnd = ConstraintSet.PARENT_ID
                        marginEnd = context.getDimension(R.dimen.stream_ui_spacing_small)
                    } else {
                        startToStart = ConstraintSet.PARENT_ID
                        marginStart = context.getDimension(R.dimen.stream_ui_spacing_small)
                    }
                }
                userReactionView.setReaction(reactionItem.reaction, reactionItem.isMine)
            }
        }
    }
}
