package io.getstream.chat.ui.sample.feature.chat.info

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.ui.sample.R

sealed class ChatInfoItem {

    val id: String
        get() = when (this) {
            is MemberItem -> chatMember.member.getUserId()
            is Option -> optionType.name
            is StatefulOption -> optionType.name
            else -> this::class.java.simpleName
        }

    data class MemberItem(val chatMember: ChatMember) : ChatInfoItem()
    data class MembersSeparator(val membersToShow: Int) : ChatInfoItem()
    data class ChannelName(val name: String) : ChatInfoItem()
    object Separator : ChatInfoItem()
    data class Option(val optionType: OptionType) : ChatInfoItem()
    data class StatefulOption(val optionType: OptionType, val isChecked: Boolean) : ChatInfoItem()
}

enum class OptionType(
    @DrawableRes val iconResId: Int,
    @StringRes val textResId: Int,
    @ColorRes val tintResId: Int,
    val showRightArrow: Boolean = true
) {
    SHARED_MEDIA(R.drawable.ic_media, R.string.chat_info_option_media, R.color.black),
    SHARED_FILES(R.drawable.ic_files, R.string.chat_info_option_files, R.color.black),
    SHARED_GROUPS(R.drawable.ic_new_group, R.string.chat_info_option_shared_groups, R.color.black),
    DELETE_CONVERSATION(
        R.drawable.ic_delete_contact,
        R.string.chat_info_option_delete_conversation,
        R.color.red,
        showRightArrow = false
    ),
    NOTIFICATIONS(R.drawable.ic_notifications, R.string.chat_info_option_notifications, R.color.black),
    MUTE(R.drawable.ic_mute, R.string.chat_info_option_mute_user, R.color.black),
    BLOCK(R.drawable.ic_block, R.string.chat_info_option_block_user, R.color.black),
    MUTE_GROUP(R.drawable.ic_mute, R.string.chat_group_info_option_mute, R.color.black),
    LEAVE_GROUP(
        R.drawable.ic_leave_group,
        R.string.chat_group_info_option_leave,
        R.color.black,
        showRightArrow = false
    ),
}

data class ChatMember(val member: Member, val isOwner: Boolean = false)
