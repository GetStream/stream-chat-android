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

package io.getstream.chat.android.ui.common.state.messages.list

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.R

/**
 * Represents possible options user can take upon moderating a message.
 *
 * @param text The text to be shown for the action.
 */
public sealed class ModeratedMessageOption(public val text: Int)

/**
 * Prompts the user to send the message anyway if the message was flagged by moderation.
 */
public object SendAnyway : ModeratedMessageOption(R.string.stream_ui_moderation_dialog_send)

/**
 * Prompts the user to edit the message if the message was flagged by moderation.
 */
public object EditMessage : ModeratedMessageOption(R.string.stream_ui_moderation_dialog_edit)

/**
 * Prompts the user to delete the message if the message was flagged by moderation.
 */
public object DeleteMessage : ModeratedMessageOption(R.string.stream_ui_moderation_dialog_delete)

/**
 * Custom actions that you can define for moderated messages.
 */
public class CustomModerationOption(
    text: Int,
    public val extraData: Map<String, Any> = emptyMap(),
) : ModeratedMessageOption(text)

/**
 * @return A list of [ModeratedMessageOption] to show to the user.
 */
@InternalStreamChatApi
public fun defaultMessageModerationOptions(): List<ModeratedMessageOption> = listOf(
    SendAnyway,
    EditMessage,
    DeleteMessage
)
