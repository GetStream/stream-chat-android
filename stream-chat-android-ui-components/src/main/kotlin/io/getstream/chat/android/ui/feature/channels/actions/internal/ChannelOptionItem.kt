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

package io.getstream.chat.android.ui.feature.channels.actions.internal

import android.graphics.drawable.Drawable
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction

/**
 * UI representation of a Channel option, when the user selects a channel in the list.
 *
 * @param optionText The text of the option item.
 * @param optionIcon The icon of the option item.
 * @param channelAction The [ChannelAction] the option represents.
 * @param isWarningItem If the option item is dangerous.
 */
internal data class ChannelOptionItem(
    val optionIcon: Drawable,
    val optionText: String,
    val channelAction: ChannelAction,
    val isWarningItem: Boolean = false,
)
