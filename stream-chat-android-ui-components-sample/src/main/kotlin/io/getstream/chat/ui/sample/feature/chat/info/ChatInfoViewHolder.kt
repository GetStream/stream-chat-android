package io.getstream.chat.ui.sample.feature.chat.info

import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.utils.extensions.getLastSeenText
import io.getstream.chat.ui.sample.common.getColorFromRes
import io.getstream.chat.ui.sample.databinding.ChatInfoMemberItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoOptionItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoSeparatorItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoStatefulOptionItemBinding

abstract class BaseViewHolder<T : ChatInfoItem>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    /**
     * Workaround to allow a downcast of the ChatInfoItem to T
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindListItem(item: ChatInfoItem) = bind(item as T)

    protected abstract fun bind(item: T)
}

class ChatInfoMemberViewHolder(private val binding: ChatInfoMemberItemBinding) :
    BaseViewHolder<ChatInfoItem.MemberItem>(binding.root) {

    override fun bind(item: ChatInfoItem.MemberItem) {
        with(item.member) {
            binding.memberAvatar.setUserData(user)
            binding.memberUsername.text = user.name
            binding.memberOnlineIndicator.isVisible = user.online
            binding.memberOnlineText.text = user.getLastSeenText(itemView.context)
            binding.memberMentionText.text = user.name
        }
    }
}

class ChatInfoSeparatorViewHolder(binding: ChatInfoSeparatorItemBinding) :
    BaseViewHolder<ChatInfoItem.Separator>(binding.root) {

    override fun bind(item: ChatInfoItem.Separator) = Unit
}

class ChatInfoOptionViewHolder(
    private val binding: ChatInfoOptionItemBinding,
    private val optionClickListener: ChatInfoAdapter.ChatInfoOptionClickListener?
) : BaseViewHolder<ChatInfoItem.Option>(binding.root) {

    private lateinit var option: ChatInfoItem.Option

    init {
        binding.optionContainer.setOnClickListener { optionClickListener?.onClick(option) }
    }

    override fun bind(item: ChatInfoItem.Option) {
        option = item
        binding.optionTextView.setText(item.textResId)
        binding.optionTextView.setTextColor(itemView.context.getColorFromRes(item.tintResId))
        binding.optionImageView.setImageResource(item.iconResId)
        binding.optionImageView.setColorFilter(itemView.context.getColorFromRes(item.tintResId))
        binding.optionArrowRight.isInvisible = !item.showRightArrow
    }
}

class ChatInfoStatefulOptionViewHolder(
    private val binding: ChatInfoStatefulOptionItemBinding,
    private val optionChangedListener: ChatInfoAdapter.ChatInfoStatefulOptionChangedListener?
) : BaseViewHolder<ChatInfoItem.Option.Stateful>(binding.root) {

    private lateinit var option: ChatInfoItem.Option.Stateful

    init {
        binding.optionSwitch.setOnCheckedChangeListener { _, isChecked ->
            optionChangedListener?.onClick(option, isChecked)
        }
    }

    override fun bind(item: ChatInfoItem.Option.Stateful) {
        option = item
        binding.optionTextView.setText(item.textResId)
        binding.optionTextView.setTextColor(itemView.context.getColorFromRes(item.tintResId))
        binding.optionImageView.setImageResource(item.iconResId)
        binding.optionImageView.setColorFilter(itemView.context.getColorFromRes(item.tintResId))
        binding.optionSwitch.isChecked = item.isChecked
    }
}
