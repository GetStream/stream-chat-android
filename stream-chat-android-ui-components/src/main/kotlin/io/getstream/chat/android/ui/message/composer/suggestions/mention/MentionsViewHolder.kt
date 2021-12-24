package io.getstream.chat.android.ui.message.composer.suggestions.mention

import coil.clear
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionBinding

/**
 * [RecyclerView.ViewHolder] used for rendering mention. Used by [MentionsAdapter].
 *
 * @param binding Handle to [StreamUiItemMentionBinding] instance.
 * @param onMentionSelected Callback invoked when mention suggestion item is clicked.
 */
internal class MentionsViewHolder(val binding: StreamUiItemMentionBinding, val onMentionSelected: (User) -> Unit) :
    SimpleListAdapter.ViewHolder<User>(binding.root) {

    /**
     * Updates [itemView] elements for a given [User] object.
     *
     * @param user Single mention suggestion represented by [User] class.
     */
    override fun bind(user: User) {
        binding.apply {
            root.setOnClickListener { onMentionSelected(user) }
            avatarView.setUserData(user)
            usernameTextView.text = user.name
            mentionNameTextView.text = itemView.context.getString(
                R.string.stream_ui_mention,
                user.name.lowercase()
            )
        }
    }

    /**
     * Cancels potential ongoing image loading request, to avoid image loading issues.
     */
    override fun unbind() {
        binding.avatarView.clear()
    }
}