/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.query

import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.Message
import java.util.Date

/**
 * Model holding data required for adding members to a channel.
 *
 * @param members The list of the members with extra data to be added.
 * @param systemMessage The system message that will be shown in the channel.
 * @param hideHistory Hides the history of the channel from the added members.
 * @param hideHistoryBefore Hides the channel history before the provided date from the added members. If [hideHistory]
 * and [hideHistoryBefore] are both specified, [hideHistoryBefore] takes precedence.
 * @param skipPush If true, skips sending push notifications.
 */
public data class AddMembersParams(
    val members: List<MemberData>,
    val systemMessage: Message? = null,
    val hideHistory: Boolean? = null,
    val hideHistoryBefore: Date? = null,
    val skipPush: Boolean? = null,
)
