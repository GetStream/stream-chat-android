package io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.internal

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionBinding
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem
import io.getstream.chat.android.ui.suggestion.list.adapter.viewholder.BaseSuggestionItemViewHolder

internal class MentionViewHolder(
    parent: ViewGroup,
    usernameStyle: TextStyle? = null,
    mentionNameStyle: TextStyle? = null,
    mentionIcon: Drawable? = null,
    // private val mentionClickListener: (User) -> Unit,
    private val binding: StreamUiItemMentionBinding = StreamUiItemMentionBinding
        .inflate(parent.streamThemeInflater, parent, false),
) : BaseSuggestionItemViewHolder<SuggestionListItem.MentionItem>(binding.root) {

    lateinit var user: User

    init {
        usernameStyle?.apply(binding.usernameTextView)
        mentionNameStyle?.apply(binding.mentionNameTextView)
        mentionIcon?.let { icon ->
            binding.mentionsIcon.setImageDrawable(icon)
        }
        // binding.root.setOnClickListener { mentionClickListener(user) }
    }

    override fun bindItem(item: SuggestionListItem.MentionItem) {
        this.user = item.user
        binding.apply {
            avatarView.setUserData(user)
            usernameTextView.text = user.name
            mentionNameTextView.text = itemView.context.getString(
                R.string.stream_ui_mention,
                user.name.lowercase()
            )
        }
    }
}
