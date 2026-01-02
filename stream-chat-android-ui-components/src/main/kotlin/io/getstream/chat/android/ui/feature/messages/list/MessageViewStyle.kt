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

package io.getstream.chat.android.ui.feature.messages.list

import io.getstream.chat.android.ui.helper.ViewStyle

/**
 * Styles container for a view that is used to display a message.
 *
 * @param own Style for messages sent by the current user.
 * @param theirs Style for messages sent by other users.
 */
public data class MessageViewStyle<T : ViewStyle>(
    val own: T?,
    val theirs: T?,
) : ViewStyle
