package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import org.intellij.lang.annotations.Language
import java.util.Date

internal object MessageDtoTestData {

    @Language("JSON")
    val downstreamJson =
        """{
          "attachments" : [${AttachmentDtoTestData.json}],
          "cid": "cid",
          "command": "command",
          "created_at": "2020-06-10T11:04:31.0Z",
          "deleted_at": "2020-06-10T11:04:31.588Z",
          "html": "html",
          "i18n": {},
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "latest_reactions": [${ReactionDtoTestData.downstreamJsonWithoutExtraData}],
          "mentioned_users": [${UserDtoTestData.downstreamJsonWithoutExtraData}],
          "own_reactions": [],
          "reaction_counts": {"like":  2},
          "reaction_scores": {"like":  10},
          "reply_count": 0,
          "parent_id": "parentId",
          "pinned": false,
          "pinned_by": ${UserDtoTestData.downstreamJsonWithoutExtraData},
          "quoted_message_id": "messageId",
          "shadowed": false,
          "show_in_channel": false,
          "silent": false,
          "text": "text",
          "thread_participants" : [],
          "type": "regular",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "user": ${UserDtoTestData.downstreamJson},
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
        text = "text",
        html = "html",
        parent_id = "parentId",
        command = "command",
        user = UserDtoTestData.downstreamUser,
        silent = false,
        shadowed = false,
        created_at = Date(1591787071000),
        updated_at = Date(1591787071588),
        deleted_at = Date(1591787071588),
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
        type = "regular",
        reply_count = 0,
        reaction_counts = mapOf("like" to 2),
        reaction_scores = mapOf("like" to 10),
        latest_reactions = listOf(ReactionDtoTestData.downstreamReactionWithoutExtraData),
        own_reactions = emptyList(),
        show_in_channel = false,
        mentioned_users = listOf(UserDtoTestData.downstreamUserWithoutExtraData),
        i18n = emptyMap(),
        thread_participants = emptyList(),
        attachments = listOf(AttachmentDtoTestData.attachment),
        quoted_message_id = "messageId",
        quoted_message = null,
        pinned = false,
        pinned_by = UserDtoTestData.downstreamUserWithoutExtraData,
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
          "user": ${UserDtoTestData.downstreamJson}
        }""".withoutWhitespace()
    val downstreamMessageWithoutExtraData = DownstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        cid = "cid",
        text = "",
        html = "",
        parent_id = null,
        command = null,
        user = UserDtoTestData.downstreamUser,
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
          "attachments": [${AttachmentDtoTestData.json}],
          "cid": "cid",
          "html": "html",
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "mentioned_users": [],
          "pinned": true,
          "shadowed": false,
          "show_in_channel": false,
          "silent": false,
          "text": "text",
          "thread_participants": [],
          "user": {
            "banned": false,
            "id": "",
            "invisible": false,
            "role": "",
            "devices": [],
            "teams": []
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
        text = "text",
        html = "html",
        parent_id = null,
        command = null,
        user = UpstreamUserDto(
            banned = false,
            id = "",
            invisible = false,
            role = "",
            devices = emptyList(),
            teams = emptyList(),
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
        attachments = listOf(AttachmentDtoTestData.attachment),
        quoted_message_id = null,
        quoted_message = null,
        pinned = true,
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
            "role": "",
            "devices": [],
            "teams": []
          }
        }""".withoutWhitespace()
    val upstreamMessageWithoutExtraData = UpstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        cid = "cid",
        text = "",
        html = "",
        parent_id = null,
        command = null,
        user = UpstreamUserDto(
            banned = false,
            id = "",
            invisible = false,
            role = "",
            devices = emptyList(),
            teams = emptyList(),
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
}
