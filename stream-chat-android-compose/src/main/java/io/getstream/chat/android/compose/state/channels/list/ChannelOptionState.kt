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

package io.getstream.chat.android.compose.state.channels.list

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction

/**
 * UI representation of a Channel option, when the user selects a channel in the list.
 *
 * @param title The title to represent the action.
 * @param titleColor The color of the title text.
 * @param iconPainter The icon to represent the action.
 * @param iconColor The color of the icon.
 * @param action The [ChannelAction] the option represents.
 */
public class ChannelOptionState(
    public val title: String,
    public val titleColor: Color,
    public val iconPainter: Painter,
    public val iconColor: Color,
    public val action: ChannelAction,
)
