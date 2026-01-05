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

package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.ChannelCapabilities

/**
 * Creates a set of channel capabilities fully populated
 * with all values inside [ChannelCapabilities].
 *
 * Used for previews, using it in production would grant every user
 * all privileges.
 */
@InternalStreamChatApi
public fun ChannelCapabilities.toSet(): Set<String> = setOf(
    BAN_CHANNEL_MEMBERS,
    CONNECT_EVENTS,
    DELETE_ANY_MESSAGE,
    DELETE_CHANNEL,
    DELETE_OWN_MESSAGE,
    FLAG_MESSAGE,
    FREEZE_CHANNEL,
    LEAVE_CHANNEL,
    MUTE_CHANNEL,
    PIN_MESSAGE,
    QUOTE_MESSAGE,
    READ_EVENTS,
    SEARCH_MESSAGES,
    SEND_CUSTOM_EVENTS,
    SEND_LINKS,
    SEND_MESSAGE,
    SEND_REACTION,
    SEND_REPLY,
    SET_CHANNEL_COOLDOWN,
    SEND_TYPING_EVENTS,
    TYPING_EVENTS,
    UPDATE_ANY_MESSAGE,
    UPDATE_CHANNEL,
    UPDATE_CHANNEL_MEMBERS,
    UPDATE_OWN_MESSAGE,
    UPLOAD_FILE,
)
