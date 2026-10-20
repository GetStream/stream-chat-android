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

package io.getstream.chat.android.state.utils.internal

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType

private const val BLOCKED_FIELD_NAME = "blocked"
private const val CREATED_BY_CAMPAIGN_ID_FIELD_NAME = "created_by_campaign_id"

/**
 * Whether this new message makes the server show a hidden channel again for the current user. Only a regular message
 * or a reply does, and not when it is shadowed, sent by a campaign, sent in a blocked channel or sent by a user the
 * current user blocked.
 *
 * @param channelExtraData The channel's extra data, which holds the server's `blocked` flag.
 * @param blockedUserIds The ids of the users the current user blocked.
 */
internal fun Message.unhidesChannel(channelExtraData: Map<String, Any>, blockedUserIds: Collection<String>): Boolean =
    when (type) {
        MessageType.REGULAR, MessageType.REPLY ->
            !shadowed &&
                extraData[CREATED_BY_CAMPAIGN_ID_FIELD_NAME] == null &&
                channelExtraData[BLOCKED_FIELD_NAME] != true &&
                user.id !in blockedUserIds

        else -> false
    }
