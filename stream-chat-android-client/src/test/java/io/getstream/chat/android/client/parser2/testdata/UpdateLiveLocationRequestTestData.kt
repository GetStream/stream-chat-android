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

import io.getstream.chat.android.network.models.UpdateLiveLocationRequest
import org.intellij.lang.annotations.Language
import java.util.Date

internal object UpdateLiveLocationRequestTestData {

    // Coordinates a 32-bit float cannot represent, so the test fails if they are ever narrowed.
    private const val LATITUDE = 37.7749295
    private const val LONGITUDE = -122.4194155

    @Language("JSON")
    val coordinateUpdateJson =
        """{
          "message_id": "messageId",
          "latitude": 37.7749295,
          "longitude": -122.4194155
        }""".withoutWhitespace()

    val coordinateUpdate = UpdateLiveLocationRequest(
        messageId = "messageId",
        latitude = LATITUDE,
        longitude = LONGITUDE,
    )

    @Language("JSON")
    val stopSharingJson =
        """{
          "message_id": "messageId",
          "end_at": "2020-06-10T11:04:31.588Z"
        }""".withoutWhitespace()

    val stopSharing = UpdateLiveLocationRequest(
        messageId = "messageId",
        endAt = Date(1591787071588),
    )
}
