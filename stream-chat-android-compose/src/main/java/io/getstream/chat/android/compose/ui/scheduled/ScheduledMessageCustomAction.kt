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

package io.getstream.chat.android.compose.ui.scheduled

import androidx.compose.ui.graphics.painter.Painter

/**
 * An action an app adds to the actions sheet of a scheduled message, next to the default actions. The sheet shows it
 * right before Delete, or after the default actions when there is no Delete, and closes before [onClick] is called.
 *
 * @param title The text of the action.
 * @param icon The icon of the action.
 * @param onClick Called when the user picks the action.
 * @param isDestructive Whether the action removes something, which shows it in the error color.
 */
public class ScheduledMessageCustomAction(
    public val title: String,
    public val icon: Painter,
    public val onClick: () -> Unit,
    public val isDestructive: Boolean = false,
)
