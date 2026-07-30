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

import io.getstream.chat.android.network.models.CreateReminderRequest
import io.getstream.chat.android.network.models.UpdateMessagePartialRequest
import io.getstream.chat.android.network.models.UpdateReminderRequest
import org.intellij.lang.annotations.Language
import java.util.Date

internal object MessageAndReminderRequestTestData {

    // UpdateMessagePartialRequest

    @Language("JSON")
    val updateMessagePartialRequestJson =
        """{
          "skip_enrich_url": true,
          "skip_push": false,
          "unset": [
            "pinned"
          ],
          "set": {
            "text": "updated"
          }
        }""".withoutWhitespace()

    val updateMessagePartialRequest = UpdateMessagePartialRequest(
        skipEnrichUrl = true,
        skipPush = false,
        unset = listOf("pinned"),
        set = mapOf("text" to "updated"),
    )

    @Language("JSON")
    val updateMessagePartialRequestJsonWithDefaults =
        """{
          "unset": [],
          "set": {}
        }""".withoutWhitespace()

    val updateMessagePartialRequestWithDefaults = UpdateMessagePartialRequest()

    // CreateReminderRequest

    private val remindAt = Date(1591787071588)

    @Language("JSON")
    val createReminderRequestJson =
        """{
          "remind_at": "2020-06-10T11:04:31.588Z"
        }""".withoutWhitespace()

    val createReminderRequest = CreateReminderRequest(remindAt = remindAt)

    @Language("JSON")
    val createReminderRequestJsonWithDefaults = """{}""".withoutWhitespace()

    val createReminderRequestWithDefaults = CreateReminderRequest()

    // UpdateReminderRequest

    @Language("JSON")
    val updateReminderRequestJson =
        """{
          "remind_at": "2020-06-10T11:04:31.588Z"
        }""".withoutWhitespace()

    val updateReminderRequest = UpdateReminderRequest(remindAt = remindAt)

    @Language("JSON")
    val updateReminderRequestJsonWithDefaults = """{}""".withoutWhitespace()

    val updateReminderRequestWithDefaults = UpdateReminderRequest()
}
