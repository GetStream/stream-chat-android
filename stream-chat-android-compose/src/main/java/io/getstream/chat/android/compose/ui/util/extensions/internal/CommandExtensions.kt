/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.util.extensions.internal

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.CommandDefaults
import io.getstream.chat.android.ui.common.R as UiCommonR

internal val Command.isPolychromaticIcon: Boolean get() = name == CommandDefaults.GIPHY

internal val Command.iconRes: Int
    @DrawableRes get() = when (name) {
        CommandDefaults.MUTE -> UiCommonR.drawable.stream_design_ic_mute
        CommandDefaults.UNMUTE -> R.drawable.stream_design_ic_audio
        CommandDefaults.BAN -> R.drawable.stream_design_ic_user_remove
        CommandDefaults.UNBAN -> R.drawable.stream_design_ic_user_add
        // fallback to the 'giphy' for backwards compatibility
        else -> R.drawable.stream_design_ic_giphy
    }

internal val Command.placeholderRes: Int
    @StringRes get() = when (name) {
        CommandDefaults.MUTE -> UiCommonR.string.stream_ui_message_composer_placeholder_command_mute
        CommandDefaults.UNMUTE -> UiCommonR.string.stream_ui_message_composer_placeholder_command_unmute
        CommandDefaults.BAN -> UiCommonR.string.stream_ui_message_composer_placeholder_command_ban
        CommandDefaults.UNBAN -> UiCommonR.string.stream_ui_message_composer_placeholder_command_unban
        // fallback to the 'giphy' for backwards compatibility
        else -> UiCommonR.string.stream_ui_message_composer_placeholder_command_giphy
    }
