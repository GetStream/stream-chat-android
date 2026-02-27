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

package io.getstream.chat.android.compose.ui.channels.list

import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Visual style for a swipe action slot.
 *
 * The style determines the background and content colors of the action button.
 * It is slot-based: the same action can be styled differently depending on its position.
 */
public enum class SwipeActionStyle {
    /** Blue background ([ChatTheme.colors.accentPrimary]), white content. */
    Primary,

    /** Gray background ([ChatTheme.colors.accentNeutral]), white content. */
    Secondary,

    /** Red background ([ChatTheme.colors.accentError]), white content. */
    Destructive,
}
