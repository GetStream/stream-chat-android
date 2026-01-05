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

package io.getstream.chat.android.ui.feature.messages.list.options.message

import android.graphics.drawable.Drawable
import io.getstream.chat.android.ui.common.state.messages.MessageAction

/**
 * UI representation of a Message option, when the user selects a message in the list.
 *
 * @param optionText The text of the option item.
 * @param optionIcon The icon of the option item.
 * @param messageAction The [MessageAction] the option represents.
 * @param isWarningItem If the option item is dangerous.
 */
public data class MessageOptionItem(
    public val optionText: String,
    public val optionIcon: Drawable,
    public val messageAction: MessageAction,
    public val isWarningItem: Boolean = false,
)
