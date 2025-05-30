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

package io.getstream.chat.ui.sample.feature.chat.info

import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.ui.utils.extensions.getLastSeenText
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.getColorFromRes
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
     * Workaround to allow a downcast of the ChatInfoItem to T.
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
                binding.userAvatarView.isInvisible = false
                binding.userAvatarView.setUser(user)
            } else {
                binding.userAvatarView.isInvisible = true
            }
            binding.memberUsername.text = user.name
            binding.memberOnlineIndicator.isVisible = user.online
            binding.memberOnlineText.text = user.getLastSeenText(itemView.context)
            binding.mentionSymbolText.text = "@${user.name.lowercase()}"
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
        binding.optionArrowRight.isVisible = item.showRightArrow
        binding.optionCompound.isVisible = item.checkedState != null
        binding.optionCompound.isChecked = item.checkedState == true
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
            binding.userAvatarView.setUser(user)
            binding.nameTextView.text = user.name
            binding.mutedIcon.isVisible = notificationsMuted == true
            binding.onlineTextView.text = user.getLastSeenText(itemView.context)

            val getString = { resId: Int -> itemView.context.getString(resId) }
            binding.channelRoleView.text = when (item.isOwner) {
                true -> getString(R.string.chat_group_info_owner)
                else -> when (val role = item.member.channelRole) {
                    "channel_member" -> getString(R.string.chat_group_info_member)
                    "channel_moderator" -> getString(R.string.chat_group_info_moderator)
                    else -> role
                }
            }
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
            itemView.context.getString(R.string.stream_ui_channel_info_expand_button, item.membersToShow)
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
