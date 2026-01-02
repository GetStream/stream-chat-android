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

import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadInfoDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadParticipantDto
import org.intellij.lang.annotations.Language
import java.util.Date

internal object ThreadDtoTestData {

    @Language("JSON")
    val downstreamThreadJson =
        """{
          "active_participant_count": 3,
          "channel": ${ChannelDtoTestData.downstreamJsonWithoutExtraData},
          "channel_cid": "messaging:123",
          "created_at": "2020-06-10T11:04:31.000Z",
          "created_by": ${UserDtoTestData.downstreamJson},
          "created_by_user_id": "user1",
          "deleted_at": null,
          "draft": null,
          "last_message_at": "2020-06-10T11:04:31.588Z",
          "latest_replies": [${MessageDtoTestData.downstreamJsonWithoutExtraData}],
          "parent_message": ${MessageDtoTestData.downstreamJsonWithoutExtraData},
          "parent_message_id": "parent_msg_id",
          "participant_count": 5,
          "read": [],
          "reply_count": 10,
          "thread_participants": [
            {
              "channel_cid": "messaging:123",
              "user": ${UserDtoTestData.downstreamJson},
              "user_id": "user1"
            }
          ],
          "title": "Thread Title",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "extraData": {
            "key1": "value1",
            "key2": true,
            "key3": {
              "key4": "val4"
            }
          },
          "customKey1": "customVal1",
          "customKey2": true,
          "customKey3": [
            "a",
            "b",
            "c"
          ]
        }"""

    val downstreamThread = DownstreamThreadDto(
        active_participant_count = 3,
        channel = ChannelDtoTestData.downstreamChannelWithoutExtraData,
        channel_cid = "messaging:123",
        created_at = Date(1591787071000),
        created_by = UserDtoTestData.downstreamUser,
        created_by_user_id = "user1",
        deleted_at = null,
        draft = null,
        last_message_at = Date(1591787071588),
        latest_replies = listOf(MessageDtoTestData.downstreamMessageWithoutExtraData),
        parent_message = MessageDtoTestData.downstreamMessageWithoutExtraData,
        parent_message_id = "parent_msg_id",
        participant_count = 5,
        read = emptyList(),
        reply_count = 10,
        thread_participants = listOf(
            DownstreamThreadParticipantDto(
                channel_cid = "messaging:123",
                user = UserDtoTestData.downstreamUser,
                user_id = "user1",
            ),
        ),
        title = "Thread Title",
        updated_at = Date(1591787071588),
        extraData = mapOf(
            "extraData" to mapOf(
                "key1" to "value1",
                "key2" to true,
                "key3" to mapOf(
                    "key4" to "val4",
                ),
            ),
            "customKey1" to "customVal1",
            "customKey2" to true,
            "customKey3" to listOf(
                "a",
                "b",
                "c",
            ),
        ),
    )

    @Language("JSON")
    val downstreamThreadJsonWithoutExtraData =
        """{
          "active_participant_count": 2,
          "channel": ${ChannelDtoTestData.downstreamJsonWithoutExtraData},
          "channel_cid": "messaging:456",
          "created_at": "2020-06-10T11:04:31.000Z",
          "created_by": ${UserDtoTestData.downstreamJson},
          "created_by_user_id": "user2",
          "deleted_at": null,
          "draft": null,
          "last_message_at": "2020-06-10T11:04:31.588Z",
          "latest_replies": [],
          "parent_message": ${MessageDtoTestData.downstreamJsonWithoutExtraData},
          "parent_message_id": "parent_msg_id_2",
          "participant_count": 2,
          "read": [],
          "reply_count": 0,
          "thread_participants": [],
          "title": "Simple Thread",
          "updated_at": "2020-06-10T11:04:31.588Z"
        }"""

    val downstreamThreadWithoutExtraData = DownstreamThreadDto(
        active_participant_count = 2,
        channel = ChannelDtoTestData.downstreamChannelWithoutExtraData,
        channel_cid = "messaging:456",
        created_at = Date(1591787071000),
        created_by = UserDtoTestData.downstreamUser,
        created_by_user_id = "user2",
        deleted_at = null,
        draft = null,
        last_message_at = Date(1591787071588),
        latest_replies = emptyList(),
        parent_message = MessageDtoTestData.downstreamMessageWithoutExtraData,
        parent_message_id = "parent_msg_id_2",
        participant_count = 2,
        read = emptyList(),
        reply_count = 0,
        thread_participants = emptyList(),
        title = "Simple Thread",
        updated_at = Date(1591787071588),
        extraData = emptyMap(),
    )

    @Language("JSON")
    val downstreamThreadInfoJson =
        """{
          "active_participant_count": 4,
          "channel_cid": "messaging:789",
          "created_at": "2020-06-10T11:04:31.000Z",
          "created_by": ${UserDtoTestData.downstreamJson},
          "created_by_user_id": "user3",
          "deleted_at": null,
          "last_message_at": "2020-06-10T11:04:31.588Z",
          "parent_message": ${MessageDtoTestData.downstreamJsonWithoutExtraData},
          "parent_message_id": "parent_msg_id_3",
          "participant_count": 8,
          "reply_count": 15,
          "title": "Thread Info Title",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "extraData": {
            "info_key1": "info_value1",
            "info_key2": false
          },
          "customInfoKey": "customInfoVal"
        }"""

    val downstreamThreadInfo = DownstreamThreadInfoDto(
        active_participant_count = 4,
        channel_cid = "messaging:789",
        created_at = Date(1591787071000),
        created_by = UserDtoTestData.downstreamUser,
        created_by_user_id = "user3",
        deleted_at = null,
        last_message_at = Date(1591787071588),
        parent_message = MessageDtoTestData.downstreamMessageWithoutExtraData,
        parent_message_id = "parent_msg_id_3",
        participant_count = 8,
        reply_count = 15,
        title = "Thread Info Title",
        updated_at = Date(1591787071588),
        extraData = mapOf(
            "extraData" to mapOf(
                "info_key1" to "info_value1",
                "info_key2" to false,
            ),
            "customInfoKey" to "customInfoVal",
        ),
    )

    @Language("JSON")
    val downstreamThreadInfoJsonWithoutExtraData =
        """{
          "active_participant_count": 1,
          "channel_cid": "messaging:000",
          "created_at": "2020-06-10T11:04:31.000Z",
          "created_by": ${UserDtoTestData.downstreamJson},
          "created_by_user_id": "user4",
          "deleted_at": null,
          "last_message_at": null,
          "parent_message": null,
          "parent_message_id": "parent_msg_id_4",
          "participant_count": 1,
          "reply_count": 0,
          "title": "Minimal Thread Info",
          "updated_at": "2020-06-10T11:04:31.588Z"
        }"""

    val downstreamThreadInfoWithoutExtraData = DownstreamThreadInfoDto(
        active_participant_count = 1,
        channel_cid = "messaging:000",
        created_at = Date(1591787071000),
        created_by = UserDtoTestData.downstreamUser,
        created_by_user_id = "user4",
        deleted_at = null,
        last_message_at = null,
        parent_message = null,
        parent_message_id = "parent_msg_id_4",
        participant_count = 1,
        reply_count = 0,
        title = "Minimal Thread Info",
        updated_at = Date(1591787071588),
        extraData = emptyMap(),
    )
}
