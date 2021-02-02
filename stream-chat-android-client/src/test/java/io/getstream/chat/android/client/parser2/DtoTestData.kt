package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.ReactionDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UserDto
import org.intellij.lang.annotations.Language
import java.util.Date

internal object DtoTestData {

    @Language("JSON")
    val downstreamJson =
        """{
          "attachments" : [],
          "cid": "cid",
          "created_at": "2020-06-10T11:04:31.0Z",
          "html": "",
          "i18n": {},
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "latest_reactions": [],
          "mentioned_users": [],
          "own_reactions": [],
          "reaction_counts": {},
          "reaction_scores": {},
          "reply_count": 0,
          "pinned": false,
          "shadowed": false,
          "show_in_channel": false,
          "silent": false,
          "text": "",
          "thread_participants" : [],
          "type": "",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "user": {
            "id": "",
            "role": "",
            "invisible": false,
            "banned": false
          },
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
        }""".withoutWhitespace()
    val downstreamMessage = DownstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        cid = "cid",
        text = "",
        html = "",
        parent_id = null,
        command = null,
        user = UserDto(
            banned = false,
            id = "",
            invisible = false,
            role = "",
            extraData = emptyMap()
        ),
        silent = false,
        shadowed = false,
        created_at = Date(1591787071000),
        updated_at = Date(1591787071588),
        deleted_at = null,
        extraData = mapOf(
            "extraData" to mapOf(
                "key1" to "value1",
                "key2" to true,
                "key3" to mapOf(
                    "key4" to "val4"
                )
            ),
            "customKey1" to "customVal1",
            "customKey2" to true,
            "customKey3" to listOf(
                "a",
                "b",
                "c"
            ),
        ),
        type = "",
        reply_count = 0,
        reaction_counts = emptyMap(),
        reaction_scores = emptyMap(),
        latest_reactions = emptyList(),
        own_reactions = emptyList(),
        show_in_channel = false,
        mentioned_users = emptyList(),
        i18n = emptyMap(),
        thread_participants = emptyList(),
        attachments = emptyList(),
        quoted_message_id = null,
        quoted_message = null,
        pinned = false,
        pinned_by = null,
        pinned_at = null,
        pin_expires = null,
        channel = null,
    )

    @Language("JSON")
    val downstreamJsonWithoutExtraData =
        """{
          "attachments" : [],
          "cid": "cid",
          "created_at": "2020-06-10T11:04:31.0Z",
          "html": "",
          "i18n": {},
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "latest_reactions": [],
          "mentioned_users": [],
          "own_reactions": [],
          "reaction_counts": {},
          "reaction_scores": {},
          "reply_count": 0,
          "pinned": false,
          "shadowed": false,
          "show_in_channel": false,
          "silent": false,
          "text": "",
          "thread_participants" : [],
          "type": "",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "user": {
            "id": "",
            "role": "",
            "invisible": false,
            "banned": false
          }
        }""".withoutWhitespace()
    val downstreamMessageWithoutExtraData = DownstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        cid = "cid",
        text = "",
        html = "",
        parent_id = null,
        command = null,
        user = UserDto(
            banned = false,
            id = "",
            invisible = false,
            role = "",
            extraData = emptyMap()
        ),
        silent = false,
        shadowed = false,
        created_at = Date(1591787071000),
        updated_at = Date(1591787071588),
        deleted_at = null,
        extraData = emptyMap(),
        type = "",
        reply_count = 0,
        reaction_counts = emptyMap(),
        reaction_scores = emptyMap(),
        latest_reactions = emptyList(),
        own_reactions = emptyList(),
        show_in_channel = false,
        mentioned_users = emptyList(),
        i18n = emptyMap(),
        thread_participants = emptyList(),
        attachments = emptyList(),
        quoted_message_id = null,
        quoted_message = null,
        pinned = false,
        pinned_by = null,
        pinned_at = null,
        pin_expires = null,
        channel = null,
    )

    @Language("JSON")
    val upstreamJson =
        """{
          "attachments": [],
          "cid": "cid",
          "html": "",
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "mentioned_users": [],
          "pinned": false,
          "shadowed": false,
          "show_in_channel": false,
          "silent": false,
          "text": "",
          "thread_participants": [],
          "user": {
            "banned": false,
            "id": "",
            "invisible": false,
            "role": ""
          },
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
        }""".withoutWhitespace()
    val upstreamMessage = UpstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        cid = "cid",
        text = "",
        html = "",
        parent_id = null,
        command = null,
        user = UserDto(
            banned = false,
            id = "",
            invisible = false,
            role = "",
            extraData = emptyMap()
        ),
        silent = false,
        shadowed = false,
        extraData = mapOf(
            "extraData" to mapOf(
                "key1" to "value1",
                "key2" to true,
                "key3" to mapOf(
                    "key4" to "val4"
                )
            ),
            "customKey1" to "customVal1",
            "customKey2" to true,
            "customKey3" to listOf(
                "a",
                "b",
                "c"
            ),
        ),
        show_in_channel = false,
        mentioned_users = emptyList(),
        thread_participants = emptyList(),
        attachments = emptyList(),
        quoted_message_id = null,
        quoted_message = null,
        pinned = false,
        pinned_by = null,
        pinned_at = null,
        pin_expires = null,
    )

    @Language("JSON")
    val upstreamJsonWithoutExtraData =
        """{
          "attachments": [],
          "cid": "cid",
          "html": "",
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "mentioned_users": [],
          "pinned": false,
          "shadowed": false,
          "show_in_channel": false,
          "silent": false,
          "text": "",
          "thread_participants": [],
          "user": {
            "banned": false,
            "id": "",
            "invisible": false,
            "role": ""
          }
        }""".withoutWhitespace()
    val upstreamMessageWithoutExtraData = UpstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        cid = "cid",
        text = "",
        html = "",
        parent_id = null,
        command = null,
        user = UserDto(
            banned = false,
            id = "",
            invisible = false,
            role = "",
            extraData = emptyMap()
        ),
        silent = false,
        shadowed = false,
        extraData = emptyMap(),
        show_in_channel = false,
        mentioned_users = emptyList(),
        thread_participants = emptyList(),
        attachments = emptyList(),
        quoted_message_id = null,
        quoted_message = null,
        pinned = false,
        pinned_by = null,
        pinned_at = null,
        pin_expires = null,
    )

    @Language("JSON")
    val reactionJson =
        """{
          "message_id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "score": 0,
          "type": "like",
          "user": {
            "banned": false,
            "id": "",
            "invisible": false,
            "role": ""
          },
          "user_id": "",
          "extraData": {
            "key1": true
          },
          "customKey1": "customVal1"
        }
        """.withoutWhitespace()
    val reaction = ReactionDto(
        message_id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        type = "like",
        score = 0,
        user = UserDto(
            banned = false,
            id = "",
            invisible = false,
            role = "",
            extraData = emptyMap()
        ),
        user_id = "",
        created_at = null,
        updated_at = null,
        extraData = mapOf(
            "extraData" to mapOf(
                "key1" to true,
            ),
            "customKey1" to "customVal1",
        ),
    )

    @Language("JSON")
    val reactionJsonWithoutExtraData =
        """{
          "message_id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "score": 0,
          "type": "like",
          "user": {
            "banned": false,
            "id": "",
            "invisible": false,
            "role": ""
          },
          "user_id": ""
        }""".withoutWhitespace()
    val reactionWithoutExtraData = ReactionDto(
        message_id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        type = "like",
        score = 0,
        user = UserDto(
            banned = false,
            id = "",
            invisible = false,
            role = "",
            extraData = emptyMap()
        ),
        user_id = "",
        created_at = null,
        updated_at = null,
        extraData = emptyMap(),
    )

    private fun String.withoutWhitespace() = filterNot(Char::isWhitespace)
}
