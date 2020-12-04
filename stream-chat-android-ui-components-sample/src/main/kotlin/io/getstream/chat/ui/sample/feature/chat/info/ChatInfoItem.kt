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
    object Separator : ChatInfoItem()

    sealed class Option : ChatInfoItem() {

        @get:DrawableRes
        abstract val iconResId: Int

        @get:StringRes
        abstract val textResId: Int

        @get:ColorRes
        abstract val tintResId: Int

        open val showRightArrow: Boolean = true

        object SharedMedia : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_media
            override val textResId: Int
                get() = R.string.chat_info_option_media
            override val tintResId: Int
                get() = R.color.black
        }

        object SharedFiles : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_files
            override val textResId: Int
                get() = R.string.chat_info_option_files
            override val tintResId: Int
                get() = R.color.black
        }

        object SharedGroups : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_new_group
            override val textResId: Int
                get() = R.string.chat_info_option_shared_groups
            override val tintResId: Int
                get() = R.color.black
        }

        object LeaveChannel : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_delete_contact
            override val textResId: Int
                get() = R.string.chat_info_option_delete_contact
            override val tintResId: Int
                get() = R.color.red
            override val showRightArrow: Boolean = false
        }

        sealed class Stateful : Option() {
            abstract val isChecked: Boolean

            data class Notifications(override val isChecked: Boolean) : Stateful() {
                override val iconResId: Int
                    get() = R.drawable.ic_notifications
                override val textResId: Int
                    get() = R.string.chat_info_option_notifications
                override val tintResId: Int
                    get() = R.color.black
            }

            data class Mute(override val isChecked: Boolean) : Stateful() {
                override val iconResId: Int
                    get() = R.drawable.ic_mute
                override val textResId: Int
                    get() = R.string.chat_info_option_mute_user
                override val tintResId: Int
                    get() = R.color.black
            }

            data class Block(override val isChecked: Boolean) : Stateful() {
                override val iconResId: Int
                    get() = R.drawable.ic_block
                override val textResId: Int
                    get() = R.string.chat_info_option_block_user
                override val tintResId: Int
                    get() = R.color.black
            }
        }
    }
}
