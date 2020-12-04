package io.getstream.chat.android.ui.messages.reactions

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.updateConstraints
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamItemUserReactionBinding
import io.getstream.chat.android.ui.utils.extensions.context
import io.getstream.chat.android.ui.utils.extensions.getDimension

internal class UserReactionAdapter(
    private val reactionClickListener: ReactionClickListener
) : ListAdapter<ReactionItem, UserReactionAdapter.UserReactionViewHolder>(ReactionItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReactionViewHolder {
        return StreamItemUserReactionBinding
            .inflate(parent.context.inflater, parent, false)
            .let { UserReactionViewHolder(it, reactionClickListener) }
    }

    override fun onBindViewHolder(holder: UserReactionViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    class UserReactionViewHolder(
        private val binding: StreamItemUserReactionBinding,
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
                    clear(R.id.reactionsView, ConstraintSet.START)
                    clear(R.id.reactionsView, ConstraintSet.END)
                }

                val params = reactionsView.layoutParams as ConstraintLayout.LayoutParams
                if (reactionItem.isMine) {
                    reactionsView.setOrientation(ReactionsView.Orientation.LEFT)
                    params.endToEnd = ConstraintSet.PARENT_ID
                    params.marginEnd = context.getDimension(R.dimen.stream_ui_spacing_small)
                } else {
                    reactionsView.setOrientation(ReactionsView.Orientation.RIGHT)
                    params.startToStart = ConstraintSet.PARENT_ID
                    params.marginStart = context.getDimension(R.dimen.stream_ui_spacing_small)
                }

                reactionsView.setReaction(reactionItem.reaction, reactionItem.isMine)
            }
        }
    }
}
