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

import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import org.intellij.lang.annotations.Language
import java.util.Date
import io.getstream.chat.android.network.models.DeliveryReceiptsResponse as DeliveryReceiptsDto
import io.getstream.chat.android.network.models.DeviceResponse as DeviceDto
import io.getstream.chat.android.network.models.PrivacySettingsResponse as PrivacySettingsDto
import io.getstream.chat.android.network.models.ReadReceiptsResponse as ReadReceiptsDto
import io.getstream.chat.android.network.models.TypingIndicatorsResponse as TypingIndicatorsDto

internal object UserDtoTestData {

    @Language("JSON")
    val downstreamJsonWithoutExtraData =
        """{
            "id": "",
            "role": "",
            "name": "username",
            "image": "image",
            "language": "language",
            "banned": false,
            "online": false,
            "created_at": "1970-01-01T00:00:00.000Z",
            "updated_at": "1970-01-01T00:00:00.000Z",
            "deactivated_at": null,
            "last_active": null,
            "teams": []
         }"""

    val downstreamUserWithoutExtraData =
        DownstreamUserDto(
            banned = false,
            id = "",
            name = "username",
            image = "image",
            language = "language",
            role = "",
            online = false,
            createdAt = Date(0),
            updatedAt = Date(0),
            teams = emptyList(),
            custom = emptyMap(),
        )

    @Language("JSON")
    val downstreamJsonWithoutImageAndName =
        """{
            "id": "",
            "role": "",
            "name": null,
            "image": null,
            "language": "",
            "banned": false,
            "online": false,
            "created_at": "1970-01-01T00:00:00.000Z",
            "updated_at": "1970-01-01T00:00:00.000Z",
            "deactivated_at": null,
            "last_active": null,
            "teams": []
         }"""

    val downstreamUserWithoutImageAndName =
        DownstreamUserDto(
            banned = false,
            id = "",
            name = null,
            image = null,
            language = "",
            role = "",
            online = false,
            createdAt = Date(0),
            updatedAt = Date(0),
            teams = emptyList(),
            custom = emptyMap(),
        )

    @Language("JSON")
    val downstreamJson =
        """{
            "id": "userId",
            "role": "owner",
            "language": "language",
            "banned": false,
            "online": true,
            "created_at": "2020-06-10T11:04:31.000Z",
            "updated_at": "2020-06-10T11:04:31.588Z",
            "deactivated_at": "2020-06-10T11:04:31.588Z",
            "last_active": "2020-06-10T11:04:31.588Z",
            "teams": [ "team1", "team2" ],
            "teams_role": {
                "team1": "owner",
                "team2": "member"
            },
            "name": "username",
            "image": "image",
            "avg_response_time": 1000
         }"""
    val downstreamUser =
        DownstreamUserDto(
            banned = false,
            id = "userId",
            name = "username",
            image = "image",
            language = "language",
            role = "owner",
            online = true,
            updatedAt = Date(1591787071588),
            createdAt = Date(1591787071000),
            deactivatedAt = Date(1591787071588),
            lastActive = Date(1591787071588),
            teams = listOf("team1", "team2"),
            teamsRole = mapOf(
                "team1" to "owner",
                "team2" to "member",
            ),
            blockedUserIds = emptyList(),
            avgResponseTime = 1000,
            custom = emptyMap(),
        )

    @Language("JSON")
    val upstreamJsonWithoutExtraData =
        """{
            "banned": true,
            "id": "userId",
            "name": "username",
            "image": "image",
            "invisible": false,
            "language": "language",
            "role": "owner",
            "devices": [],
            "teams": []
         }""".withoutWhitespace()

    val upstreamUserWithoutExtraData = UpstreamUserDto(
        id = "userId",
        role = "owner",
        name = "username",
        image = "image",
        language = "language",
        invisible = false,
        privacy_settings = null,
        banned = true,
        devices = emptyList(),
        teams = emptyList(),
        teams_role = null,
        extraData = emptyMap(),
    )

    @Language("JSON")
    val upstreamJson =
        """{
            "banned": false,
            "id": "userId",
            "name": "username",
            "image": "image",
            "invisible": false,
            "privacy_settings": {
              "typing_indicators": {
                "enabled": false
              },
              "read_receipts": {
                "enabled": false
              },
              "delivery_receipts": {
                "enabled": false
              }
            },
            "language": "language",
            "role": "owner",
            "devices": [
             {
              "id": "deviceId",
              "push_provider": "provider",
              "push_provider_name": "provider_name",
              "created_at": "2020-06-10T11:04:31.000Z",
              "user_id": "userId"
             }
            ],
            "teams": [ "team1", "team2"],
            "teams_role": {
                "team1": "owner",
                "team2": "member"
            }
         }""".withoutWhitespace()

    val upstreamUser = UpstreamUserDto(
        id = "userId",
        role = "owner",
        invisible = false,
        privacy_settings = PrivacySettingsDto(
            typingIndicators = TypingIndicatorsDto(
                enabled = false,
            ),
            readReceipts = ReadReceiptsDto(
                enabled = false,
            ),
            deliveryReceipts = DeliveryReceiptsDto(
                enabled = false,
            ),
        ),
        banned = false,
        devices = listOf(
            DeviceDto(
                id = "deviceId",
                pushProvider = "provider",
                pushProviderName = "provider_name",
                createdAt = Date(1591787071000),
                userId = "userId",
            ),
        ),
        teams = listOf("team1", "team2"),
        teams_role = mapOf(
            "team1" to "owner",
            "team2" to "member",
        ),
        name = "username",
        image = "image",
        language = "language",
        extraData = emptyMap(),
    )
}
