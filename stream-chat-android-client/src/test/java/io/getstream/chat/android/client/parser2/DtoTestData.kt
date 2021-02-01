package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UserDto
import org.intellij.lang.annotations.Language
import java.util.Date

internal object DtoTestData {

    @Language("JSON")
    val downstreamJson =
        """{
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "cid": "cid",
          "text": "",
          "html": "",
          "user": {
            "id": "",
            "role": "",
            "invisible": false,
            "banned": false
          },
          "silent": false,
          "shadowed": false,
          "created_at": "2020-06-10T11:04:31.0Z",
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
        }""".withoutWhitespace()
    val downstreamMessage = DownstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        cid = "cid",
        text = "",
        html = "",
        parent_id = null,
        command = null,
        user = UserDto("", "", false, false),
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
        )
    )

    @Language("JSON")
    val downstreamJsonWithoutExtraData =
        """{
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "cid": "cid",
          "text": "",
          "html": "",
          "user": {
            "id": "",
            "role": "",
            "invisible": false,
            "banned": false
          },
          "silent": false,
          "shadowed": false,
          "created_at": "2020-06-10T11:04:31.0Z",
          "updated_at": "2020-06-10T11:04:31.588Z"
        }""".withoutWhitespace()
    val downstreamMessageWithoutExtraData = DownstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        cid = "cid",
        text = "",
        html = "",
        parent_id = null,
        command = null,
        user = UserDto("", "", false, false),
        silent = false,
        shadowed = false,
        created_at = Date(1591787071000),
        updated_at = Date(1591787071588),
        deleted_at = null,
        extraData = emptyMap()
    )

    @Language("JSON")
    val upstreamJson =
        """{
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "cid": "cid",
          "text": "",
          "html": "",
          "user": {
            "id": "",
            "role": "",
            "invisible": false,
            "banned": false
          },
          "silent": false,
          "shadowed": false,
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
        user = UserDto("", "", false, false),
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
        )
    )

    @Language("JSON")
    val upstreamJsonWithoutExtraData =
        """{
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "cid": "cid",
          "text": "",
          "html": "",
          "user": {
            "id": "",
            "role": "",
            "invisible": false,
            "banned": false
          },
          "silent": false,
          "shadowed": false
        }""".withoutWhitespace()
    val upstreamMessageWithoutExtraData = UpstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        cid = "cid",
        text = "",
        html = "",
        parent_id = null,
        command = null,
        user = UserDto("", "", false, false),
        silent = false,
        shadowed = false,
        extraData = emptyMap()
    )

    private fun String.withoutWhitespace() = filterNot(Char::isWhitespace)
}
