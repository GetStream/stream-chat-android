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

package io.getstream.chat.android.ui.common.state.messages.composer

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.Reply

/**
 * Whether this [Command] is selectable in the composer given the currently-active composer
 * [action]:
 *
 * - [Edit] → always `false`.
 * - [Reply] → `false` for commands in the `moderation_set` (e.g. `/mute`, `/ban`); `true`
 *   otherwise.
 * - `null` or any other action → `true`.
 *
 * @param action The composer action currently active, or `null` when the composer is in its
 * default state.
 */
@InternalStreamChatApi
public fun Command.isAvailableFor(action: MessageAction?): Boolean = when (action) {
    is Edit -> false
    is Reply -> set != MODERATION_COMMAND_SET
    else -> true
}

private const val MODERATION_COMMAND_SET: String = "moderation_set"
