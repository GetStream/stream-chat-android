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

import io.getstream.chat.android.network.models.UpdateChannelPartialRequest
import org.intellij.lang.annotations.Language

internal object UpdateChannelPartialRequestTestData {

    @Language("JSON")
    val updateChannelPartialRequestJson =
        """{
          "unset": [
            "config_overrides"
          ],
          "set": {
            "cooldown": 10
          }
        }""".withoutWhitespace()

    val updateChannelPartialRequest = UpdateChannelPartialRequest(
        unset = listOf("config_overrides"),
        set = mapOf("cooldown" to 10),
    )

    @Language("JSON")
    val updateChannelPartialRequestJsonWithDefaults =
        """{
          "unset": [],
          "set": {}
        }""".withoutWhitespace()

    val updateChannelPartialRequestWithDefaults = UpdateChannelPartialRequest()
}
