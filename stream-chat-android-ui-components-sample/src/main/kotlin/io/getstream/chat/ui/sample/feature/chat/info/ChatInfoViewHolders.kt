package io.getstream.chat.ui.sample.feature.chat.info

import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.utils.extensions.getLastSeenText
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.getColorFromRes
import io.getstream.chat.ui.sample.common.isOwner
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupMemberItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupNameItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoMemberItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoMembersSeparatorItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoOptionItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoSeparatorItemBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoStatefulOptionItemBinding
import io.getstream.chat.ui.sample.feature.chat.info.group.GroupChatInfoAdapter

abstract class BaseViewHolder<T : ChatInfoItem>(
    itemView: View,
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
            if (user.image.isNotEmpty()) {
                binding.memberAvatar.isInvisible = false
                binding.memberAvatar.setUserData(user)
            } else {
                binding.memberAvatar.isInvisible = true
            }
            binding.memberUsername.text = user.name
            binding.memberOnlineIndicator.isVisible = user.online
            binding.memberOnlineText.text = user.getLastSeenText(itemView.context)
            binding.mentionSymbolText.text = "@${user.name.toLowerCase()}"
        }
    }
}

class ChatInfoSeparatorViewHolder(binding: ChatInfoSeparatorItemBinding) :
    BaseViewHolder<ChatInfoItem.Separator>(binding.root) {

    override fun bind(item: ChatInfoItem.Separator) = Unit
}

class ChatInfoOptionViewHolder(
    private val binding: ChatInfoOptionItemBinding,
    private val optionClickListener: ChatInfoAdapter.ChatInfoOptionClickListener?,
) : BaseViewHolder<ChatInfoItem.Option>(binding.root) {

    private lateinit var option: ChatInfoItem.Option

    init {
        binding.optionContainer.setOnClickListener { optionClickListener?.onClick(option) }
    }

    override fun bind(item: ChatInfoItem.Option) {
        option = item
        binding.optionTextView.setText(item.textResId)
        binding.optionTextView.setTextColor(itemView.context.getColorFromRes(item.textColorResId))
        binding.optionImageView.setImageResource(item.iconResId)
        binding.optionImageView.setColorFilter(itemView.context.getColorFromRes(item.tintResId))
        binding.optionArrowRight.isInvisible = !item.showRightArrow
    }
}

class ChatInfoStatefulOptionViewHolder(
    private val binding: ChatInfoStatefulOptionItemBinding,
    private val optionChangedListener: ChatInfoAdapter.ChatInfoStatefulOptionChangedListener?,
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
        binding.optionImageView.setImageResource(item.iconResId)
        binding.optionImageView.setColorFilter(itemView.context.getColorFromRes(item.tintResId))
        binding.optionSwitch.isChecked = item.isChecked
    }
}

class ChatInfoGroupMemberViewHolder(
    private val binding: ChatInfoGroupMemberItemBinding,
    private val memberClickListener: GroupChatInfoAdapter.MemberClickListener?,
) : BaseViewHolder<ChatInfoItem.MemberItem>(binding.root) {

    private lateinit var member: Member

    init {
        binding.root.setOnClickListener {
            memberClickListener?.onClick(member)
        }
    }

    override fun bind(item: ChatInfoItem.MemberItem) {
        with(item.member) {
            member = this
            binding.userAvatar.setUserData(user)
            binding.nameTextView.text = user.name
            binding.onlineTextView.text = user.getLastSeenText(itemView.context)
            binding.ownerTextView.isVisible = item.member.isOwner
        }
    }
}

class ChatInfoMembersSeparatorViewHolder(
    private val binding: ChatInfoMembersSeparatorItemBinding,
    private val membersSeparatorClickListener: GroupChatInfoAdapter.MembersSeparatorClickListener?,
) : BaseViewHolder<ChatInfoItem.MembersSeparator>(binding.root) {

    init {
        binding.membersSeparatorTextView.setOnClickListener { membersSeparatorClickListener?.onClick() }
    }

    override fun bind(item: ChatInfoItem.MembersSeparator) {
        binding.membersSeparatorTextView.text =
            itemView.context.getString(R.string.chat_group_info_option_members_separator_title, item.membersToShow)
    }
}

class ChatInfoGroupNameViewHolder(
    private val binding: ChatInfoGroupNameItemBinding,
    private val nameChangedListener: GroupChatInfoAdapter.NameChangedListener?,
) : BaseViewHolder<ChatInfoItem.ChannelName>(binding.root) {

    init {
        binding.editNameView.setGroupNameChangedListener { name ->
            nameChangedListener?.onChange(name)
        }
    }

    override fun bind(item: ChatInfoItem.ChannelName) {
        binding.editNameView.setChannelName(item.name)
    }
}
