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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.getstream.chat.android.models.Member
import io.getstream.chat.ui.sample.R

sealed class ChatInfoItem {

    val id: String
        get() = when (this) {
            is MemberItem -> member.user.id
            else -> this::class.java.simpleName
        }

    data class MemberItem(val member: Member, val isOwner: Boolean = false) : ChatInfoItem()
    data class MembersSeparator(val membersToShow: Int) : ChatInfoItem()
    data class ChannelName(val name: String) : ChatInfoItem()
    data object Separator : ChatInfoItem()

    sealed class Option : ChatInfoItem() {

        @get:DrawableRes
        abstract val iconResId: Int

        @get:StringRes
        abstract val textResId: Int

        @get:ColorRes
        open val tintResId: Int = R.color.stream_ui_grey

        @get:ColorRes
        open val textColorResId: Int = R.color.stream_ui_text_color_primary

        open val showRightArrow: Boolean = true

        open val checkedState: Boolean? = null

        data object PinnedMessages : Option() {
            override val iconResId: Int
                get() = R.drawable.stream_ui_ic_pin
            override val textResId: Int
                get() = R.string.stream_ui_channel_info_option_pinned_messages
        }

        data object SharedMedia : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_media
            override val textResId: Int
                get() = R.string.chat_info_option_media
        }

        data object SharedFiles : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_files
            override val textResId: Int
                get() = R.string.chat_info_option_files
        }

        data object SharedGroups : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_new_group
            override val textResId: Int
                get() = R.string.chat_info_option_shared_groups
        }

        data class DeleteChannel(@StringRes override val textResId: Int) : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_delete
            override val tintResId: Int
                get() = R.color.stream_ui_accent_red
            override val textColorResId: Int
                get() = R.color.stream_ui_accent_red
            override val showRightArrow: Boolean = false
        }

        data class LeaveChannel(@StringRes override val textResId: Int) : Option() {
            override val iconResId: Int
                get() = R.drawable.ic_leave_group
            override val tintResId: Int
                get() = R.color.stream_ui_accent_red
            override val textColorResId: Int
                get() = R.color.stream_ui_accent_red
            override val showRightArrow: Boolean = false
        }

        sealed class Stateful : Option() {
            abstract val isChecked: Boolean

            data class MuteChannel(
                @StringRes override val textResId: Int,
                override val isChecked: Boolean,
            ) : Stateful() {
                override val iconResId: Int
                    get() = R.drawable.ic_mute
            }

            data class HideChannel(
                @StringRes override val textResId: Int,
                override var isChecked: Boolean,
            ) : Stateful() {
                override val iconResId: Int
                    get() = R.drawable.stream_ic_hide
            }
        }
    }
}
