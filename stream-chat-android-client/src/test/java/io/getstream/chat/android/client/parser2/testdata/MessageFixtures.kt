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

import org.junit.jupiter.params.provider.Arguments

/**
 * Every message JSON fixture, reshaped by [WireShape] into what the backend sends, for parameterized tests over
 * the generated message models.
 */
internal object MessageFixtures {

    /**
     * A message whose author was deleted: the backend substitutes a placeholder user and sends its zero
     * fields as-is (NewUserResponseOrDeletedUser in commonpayloads/user.go).
     */
    val DELETED_AUTHOR_JSON = """
        {
          "id": "msg-deleted-author", "cid": "messaging:general", "text": "hi", "html": "<p>hi</p>",
          "type": "regular", "attachments": [], "latest_reactions": [], "own_reactions": [],
          "mentioned_users": [], "reply_count": 0, "deleted_reply_count": 0, "silent": false,
          "shadowed": false, "mentioned_channel": false, "mentioned_here": false, "pinned": false,
          "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z",
          "user": {
            "id": "deleted-user", "name": "Deleted User", "role": "", "language": "",
            "banned": false, "online": false,
            "created_at": "0001-01-01T00:00:00Z", "updated_at": "0001-01-01T00:00:00Z"
          }
        }
    """.trimIndent()

    /** Every JSON fixture that describes a message, keyed by its property name. */
    @JvmStatic
    fun fixtures(): List<Arguments> {
        val messageTestData = MessageTestData::class.java.declaredFields
            .filter { it.type == String::class.java }
            .onEach { it.isAccessible = true }
            .mapNotNull { field ->
                (field.get(MessageTestData) as? String)?.let { "MessageTestData.${field.name}" to it }
            }
            .filter { (_, json) -> json.trimStart().startsWith("{") }
        val dtoTestData = listOf(
            "MessageDtoTestData.downstreamJson" to MessageDtoTestData.downstreamJson,
            "MessageDtoTestData.downstreamJsonWithoutExtraData" to
                MessageDtoTestData.downstreamJsonWithoutExtraData,
            "deleted author" to DELETED_AUTHOR_JSON,
            "restricted visibility" to MessageDtoTestData.downstreamJsonWithoutExtraData.replaceFirst(
                "{",
                """{"restricted_visibility": ["user-a", "user-b"],""",
            ),
        )
        return (messageTestData + dtoTestData).map { (name, json) -> Arguments.of(name, WireShape.message(json)) }
    }
}
