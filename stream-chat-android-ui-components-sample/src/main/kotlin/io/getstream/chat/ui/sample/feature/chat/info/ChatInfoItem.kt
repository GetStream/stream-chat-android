package io.getstream.chat.ui.sample.feature.chat.info

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.ui.sample.R

sealed class ChatInfoItem {

    val id: String
        get() = when (this) {
            is MemberItem -> member.getUserId()
            else -> this::class.java.simpleName
        }

    data class MemberItem(val member: Member) : ChatInfoItem()
    data class MembersSeparator(val membersToShow: Int) : ChatInfoItem()
    data class ChannelName(val name: String) : ChatInfoItem()
    object Separator : ChatInfoItem()

    sealed class Option : ChatInfoItem() {

        @get:DrawableRes
        abstract val iconResId: Int

        @get:StringRes
        abstract val textResId: Int

        @get:ColorRes
        open val tintResId: Int = R.color.stream_ui_grey

        @get:ColorRes
        open val textColorResId: Int = R.color.stream_ui_black

        open val showRightArrow: Boolean = true

        object SharedMedia : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_media
            override val textResId: Int
                get() = R.string.chat_info_option_media
        }

        object SharedFiles : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_files
            override val textResId: Int
                get() = R.string.chat_info_option_files
        }

        object SharedGroups : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_new_group
            override val textResId: Int
                get() = R.string.chat_info_option_shared_groups
        }

        object DeleteConversation : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_delete
            override val textResId: Int
                get() = R.string.chat_info_option_delete_conversation
            override val tintResId: Int
                get() = R.color.red
            override val textColorResId: Int
                get() = R.color.red
            override val showRightArrow: Boolean = false
        }

        object LeaveGroup : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_leave_group
            override val textResId: Int
                get() = R.string.chat_group_info_option_leave
            override val showRightArrow: Boolean = false
        }

        sealed class Stateful : Option() {
            abstract val isChecked: Boolean

            data class Notifications(override val isChecked: Boolean) : Stateful() {
                override val iconResId: Int
                    get() = R.drawable.ic_notifications
                override val textResId: Int
                    get() = R.string.chat_info_option_notifications
            }

            data class MuteUser(override val isChecked: Boolean) : Stateful() {
                override val iconResId: Int
                    get() = R.drawable.ic_mute
                override val textResId: Int
                    get() = R.string.chat_info_option_mute_user
            }

            data class MuteChannel(override val isChecked: Boolean) : Stateful() {
                override val iconResId: Int
                    get() = R.drawable.ic_mute
                override val textResId: Int
                    get() = R.string.chat_group_info_option_mute
            }

            data class Block(override val isChecked: Boolean) : Stateful() {
                override val iconResId: Int
                    get() = R.drawable.ic_block
                override val textResId: Int
                    get() = R.string.chat_info_option_block_user
            }
        }
    }
}
