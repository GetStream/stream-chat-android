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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.network.models.ChannelMemberRequest
import io.getstream.chat.android.network.models.ChatPreferencesInput
import io.getstream.chat.android.network.models.PushPreferenceInput
import io.getstream.chat.android.network.models.QueryMembersPayload
import io.getstream.chat.android.network.models.SortParamRequest
import io.getstream.chat.android.network.models.UpsertPushPreferencesRequest
import java.util.Date

internal object RequestBodiesTestData {

    val queryMembers = QueryMembersPayload(
        type = "messaging",
        id = "channelId",
        filterConditions = mapOf("name" to mapOf("\$autocomplete" to "amit")),
        offset = 10,
        limit = 30,
        // Pinned as a literal rather than built from a sorter, so the expected wire shape is asserted
        // rather than restated by the same conversion the payload uses.
        sort = listOf(SortParamRequest(field = "created_at", direction = -1)),
        members = listOf(ChannelMemberRequest(userId = "amit", channelRole = "channel_moderator")),
    )

    // The member carries no custom data, and the collecting adapter flattens custom to the root, so no
    // `custom` key is emitted at all.
    val queryMembersJson =
        """{"type":"messaging","filter_conditions":{"name":{"${'$'}autocomplete":"amit"}},"id":"channelId",""" +
            """"limit":30,"offset":10,"members":[{"user_id":"amit","channel_role":"channel_moderator"}],""" +
            """"sort":[{"direction":-1,"field":"created_at"}]}"""

    val snoozeUserPushPreferences = UpsertPushPreferencesRequest(
        preferences = listOf(PushPreferenceInput(disabledUntil = Date(1614218400000))),
    )

    const val snoozeUserPushPreferencesJson =
        """{"preferences":[{"disabled_until":"2021-02-25T02:00:00.000Z"}]}"""

    val setChannelPushPreference = UpsertPushPreferencesRequest(
        preferences = listOf(
            PushPreferenceInput(
                channelCid = "messaging:channelId",
                chatLevel = PushPreferenceInput.ChatLevel.fromString("all"),
                removeDisable = true,
            ),
        ),
    )

    const val setChannelPushPreferenceJson =
        """{"preferences":[{"channel_cid":"messaging:channelId","chat_level":"all","remove_disable":true}]}"""

    val setChatPreferences = UpsertPushPreferencesRequest(
        preferences = listOf(
            PushPreferenceInput(
                chatPreferences = ChatPreferencesInput(
                    directMentions = ChatPreferencesInput.DirectMentions.fromString("all"),
                    threadReplies = ChatPreferencesInput.ThreadReplies.fromString("none"),
                    defaultPreference = ChatPreferencesInput.DefaultPreference.fromString("all"),
                ),
            ),
        ),
    )

    const val setChatPreferencesJson =
        """{"preferences":[{"chat_preferences":{"default_preference":"all","direct_mentions":"all",""" +
            """"thread_replies":"none"}}]}"""
}
