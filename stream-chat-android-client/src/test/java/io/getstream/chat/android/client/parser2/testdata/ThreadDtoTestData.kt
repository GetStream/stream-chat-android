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
              "thread_id": "parent_msg_id",
              "user_id": "user1",
              "user": ${UserDtoTestData.downstreamJson},
              "created_at": "2020-06-10T11:04:31.000Z",
              "last_read_at": "2020-06-10T11:04:31.588Z"
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
                user_id = "user1",
                user = UserDtoTestData.downstreamUser,
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
          "channel_cid": "messaging:789",
          "channel": ${ChannelDtoTestData.downstreamJsonWithoutExtraData},
          "parent_message_id": "parent_msg_id_3",
          "parent_message": ${MessageDtoTestData.downstreamJsonWithoutExtraData},
          "created_by_user_id": "user3",
          "created_by": ${UserDtoTestData.downstreamJson},
          "reply_count": 15,
          "participant_count": 8,
          "active_participant_count": 4,
          "thread_participants": [
            {
              "channel_cid": "messaging:789",
              "thread_id": "parent_msg_id_3",
              "user_id": "user1",
              "user": ${UserDtoTestData.downstreamJson},
              "created_at": "2020-06-10T11:04:31.000Z",
              "last_read_at": "2020-06-10T11:04:31.588Z"
            }
          ],
          "last_message_at": "2020-06-10T11:04:31.588Z",
          "created_at": "2020-06-10T11:04:31.000Z",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "deleted_at": null,
          "title": "Thread Info Title",
          "extraData": {
            "info_key1": "info_value1",
            "info_key2": false
          },
          "customInfoKey": "customInfoVal"
        }"""

    val downstreamThreadInfo = DownstreamThreadInfoDto(
        channel_cid = "messaging:789",
        channel = ChannelDtoTestData.downstreamChannelWithoutExtraData,
        parent_message_id = "parent_msg_id_3",
        parent_message = MessageDtoTestData.downstreamMessageWithoutExtraData,
        created_by_user_id = "user3",
        created_by = UserDtoTestData.downstreamUser,
        reply_count = 15,
        participant_count = 8,
        active_participant_count = 4,
        thread_participants = listOf(
            DownstreamThreadParticipantDto(
                channel_cid = "messaging:789",
                user_id = "user1",
                user = UserDtoTestData.downstreamUser,
            ),
        ),
        last_message_at = Date(1591787071588),
        created_at = Date(1591787071000),
        updated_at = Date(1591787071588),
        deleted_at = null,
        title = "Thread Info Title",
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
          "channel_cid": "messaging:000",
          "channel": null,
          "parent_message_id": "parent_msg_id_4",
          "parent_message": null,
          "created_by_user_id": "user4",
          "created_by": ${UserDtoTestData.downstreamJson},
          "reply_count": 0,
          "participant_count": 1,
          "active_participant_count": 1,
          "thread_participants": [],
          "last_message_at": null,
          "created_at": "2020-06-10T11:04:31.000Z",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "deleted_at": null,
          "title": "Minimal Thread Info"
        }"""

    val downstreamThreadInfoWithoutExtraData = DownstreamThreadInfoDto(
        channel_cid = "messaging:000",
        channel = null,
        parent_message_id = "parent_msg_id_4",
        parent_message = null,
        created_by_user_id = "user4",
        created_by = UserDtoTestData.downstreamUser,
        reply_count = 0,
        participant_count = 1,
        active_participant_count = 1,
        thread_participants = emptyList(),
        last_message_at = null,
        created_at = Date(1591787071000),
        updated_at = Date(1591787071588),
        deleted_at = null,
        title = "Minimal Thread Info",
        extraData = emptyMap(),
    )
}
