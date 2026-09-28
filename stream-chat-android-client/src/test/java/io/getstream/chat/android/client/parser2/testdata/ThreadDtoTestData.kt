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

import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.network.models.MessageResponse
import io.getstream.chat.android.network.models.ReadStateResponse
import io.getstream.chat.android.network.models.ThreadParticipant
import io.getstream.chat.android.network.models.ThreadResponse
import io.getstream.chat.android.network.models.ThreadStateResponse
import org.intellij.lang.annotations.Language
import java.util.Date

internal object ThreadDtoTestData {

    // A message as the wire sends it: every field the backend always emits (plain Go tags).
    @Language("JSON")
    private val messageJson =
        """{
          "id": "message-id",
          "cid": "messaging:123",
          "text": "hello",
          "html": "<p>hello</p>",
          "type": "regular",
          "created_at": "2020-06-10T11:04:31.000Z",
          "updated_at": "2020-06-10T11:04:31.000Z",
          "mentioned_channel": false,
          "mentioned_here": false,
          "pinned": false,
          "shadowed": false,
          "silent": false,
          "reply_count": 0,
          "deleted_reply_count": 0,
          "user": ${UserDtoTestData.userResponseJson}
        }"""

    // Nested messages go through the generated MessageResponse, whose parsing has its own tests.
    private val message: MessageResponse = ParserFactory.createMoshiChatParser()
        .fromJson(messageJson, MessageResponse::class.java)

    @Language("JSON")
    val downstreamThreadJson =
        """{
          "active_participant_count": 3,
          "channel": ${ChannelDtoTestData.channelResponseJson},
          "channel_cid": "messaging:123",
          "created_at": "2020-06-10T11:04:31.000Z",
          "created_by": ${UserDtoTestData.userResponseJson},
          "created_by_user_id": "user1",
          "deleted_at": null,
          "draft": null,
          "last_message_at": "2020-06-10T11:04:31.588Z",
          "latest_replies": [$messageJson],
          "parent_message": $messageJson,
          "parent_message_id": "parent_msg_id",
          "participant_count": 5,
          "read": [
            {
              "user": ${UserDtoTestData.userResponseJson},
              "last_read": "2020-06-10T11:04:31.0Z",
              "unread_messages": 1,
              "last_read_message_id": "messageId"
            }
          ],
          "reply_count": 10,
          "thread_participants": [
            {
              "app_pk": 1,
              "channel_cid": "messaging:channelId",
              "created_at": "2020-06-10T11:04:31.588Z",
              "last_read_at": "2020-06-10T11:04:31.588Z",
              "user_id": "user1",
              "user": ${UserDtoTestData.userResponseJson},
              "last_thread_message_at": null,
              "custom": null
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

    val downstreamThread = ThreadStateResponse(
        activeParticipantCount = 3,
        channel = ChannelDtoTestData.channelResponse,
        channelCid = "messaging:123",
        createdAt = Date(1591787071000),
        createdBy = UserDtoTestData.userResponse,
        createdByUserId = "user1",
        lastMessageAt = Date(1591787071588),
        latestReplies = listOf(message),
        parentMessage = message,
        parentMessageId = "parent_msg_id",
        participantCount = 5,
        read = listOf(
            ReadStateResponse(
                user = UserDtoTestData.userResponse,
                lastRead = Date(1591787071000),
                unreadMessages = 1,
                lastReadMessageId = "messageId",
            ),
        ),
        replyCount = 10,
        threadParticipants = listOf(
            ThreadParticipant(
                channelCid = "messaging:channelId",
                createdAt = Date(1591787071588),
                lastReadAt = Date(1591787071588),
                userId = "user1",
                user = UserDtoTestData.userResponse,
                lastThreadMessageAt = null,
            ),
        ),
        title = "Thread Title",
        updatedAt = Date(1591787071588),
        custom = mapOf(
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
          "channel": ${ChannelDtoTestData.channelResponseJson},
          "channel_cid": "messaging:456",
          "created_at": "2020-06-10T11:04:31.000Z",
          "created_by": ${UserDtoTestData.userResponseJson},
          "created_by_user_id": "user2",
          "deleted_at": null,
          "draft": null,
          "last_message_at": "2020-06-10T11:04:31.588Z",
          "latest_replies": [],
          "parent_message": $messageJson,
          "parent_message_id": "parent_msg_id_2",
          "participant_count": 2,
          "read": [],
          "reply_count": 0,
          "thread_participants": [],
          "title": "Simple Thread",
          "updated_at": "2020-06-10T11:04:31.588Z"
        }"""

    val downstreamThreadWithoutExtraData = ThreadStateResponse(
        activeParticipantCount = 2,
        channel = ChannelDtoTestData.channelResponse,
        channelCid = "messaging:456",
        createdAt = Date(1591787071000),
        createdBy = UserDtoTestData.userResponse,
        createdByUserId = "user2",
        lastMessageAt = Date(1591787071588),
        latestReplies = emptyList(),
        parentMessage = message,
        parentMessageId = "parent_msg_id_2",
        participantCount = 2,
        read = emptyList(),
        replyCount = 0,
        threadParticipants = emptyList(),
        title = "Simple Thread",
        updatedAt = Date(1591787071588),
        custom = emptyMap(),
    )

    @Language("JSON")
    val downstreamThreadInfoJson =
        """{
          "channel_cid": "messaging:789",
          "channel": ${ChannelDtoTestData.channelResponseJson},
          "parent_message_id": "parent_msg_id_3",
          "parent_message": $messageJson,
          "created_by_user_id": "user3",
          "created_by": ${UserDtoTestData.userResponseJson},
          "reply_count": 15,
          "participant_count": 8,
          "active_participant_count": 4,
          "thread_participants": [
            {
              "app_pk": 1,
              "channel_cid": "messaging:channelId",
              "created_at": "2020-06-10T11:04:31.588Z",
              "last_read_at": "2020-06-10T11:04:31.588Z",
              "user_id": "user1",
              "user": ${UserDtoTestData.userResponseJson},
              "last_thread_message_at": null,
              "custom": null
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

    val downstreamThreadInfo = ThreadResponse(
        channelCid = "messaging:789",
        channel = ChannelDtoTestData.channelResponse,
        parentMessageId = "parent_msg_id_3",
        parentMessage = message,
        createdByUserId = "user3",
        createdBy = UserDtoTestData.userResponse,
        replyCount = 15,
        participantCount = 8,
        activeParticipantCount = 4,
        threadParticipants = listOf(
            ThreadParticipant(
                channelCid = "messaging:channelId",
                createdAt = Date(1591787071588),
                lastReadAt = Date(1591787071588),
                userId = "user1",
                user = UserDtoTestData.userResponse,
                lastThreadMessageAt = null,
            ),
        ),
        lastMessageAt = Date(1591787071588),
        createdAt = Date(1591787071000),
        updatedAt = Date(1591787071588),
        title = "Thread Info Title",
        custom = mapOf(
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
          "created_by": ${UserDtoTestData.userResponseJson},
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

    val downstreamThreadInfoWithoutExtraData = ThreadResponse(
        channelCid = "messaging:000",
        channel = null,
        parentMessageId = "parent_msg_id_4",
        parentMessage = null,
        createdByUserId = "user4",
        createdBy = UserDtoTestData.userResponse,
        replyCount = 0,
        participantCount = 1,
        activeParticipantCount = 1,
        threadParticipants = emptyList(),
        lastMessageAt = null,
        createdAt = Date(1591787071000),
        updatedAt = Date(1591787071588),
        title = "Minimal Thread Info",
        custom = emptyMap(),
    )
}
