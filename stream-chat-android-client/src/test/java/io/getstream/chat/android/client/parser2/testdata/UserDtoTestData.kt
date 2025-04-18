/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.PrivacySettingsDto
import io.getstream.chat.android.client.api2.model.dto.ReadReceiptsDto
import io.getstream.chat.android.client.api2.model.dto.TypingIndicatorsDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import org.intellij.lang.annotations.Language
import java.util.Date

internal object UserDtoTestData {

    @Language("JSON")
    val downstreamJsonWithoutExtraData =
        """{
            "id": "",
            "role": "",
            "name": "username",
            "image": "image",
            "invisible": false,
            "language": "language",
            "banned": false,
            "devices": [],
            "online": false,
            "created_at": null,
            "deactivated_at": null,
            "updated_at": null,
            "last_active": null,
            "total_unread_count": 0,
            "unread_channels": 0,
            "unread_count": 0,
            "mutes": [],
            "teams": [],
            "channel_mutes": []
         }"""

    val downstreamUserWithoutExtraData =
        DownstreamUserDto(
            banned = false,
            id = "",
            name = "username",
            image = "image",
            invisible = false,
            privacy_settings = null,
            language = "language",
            role = "",
            devices = emptyList(),
            online = false,
            updated_at = null,
            created_at = null,
            deactivated_at = null,
            last_active = null,
            total_unread_count = 0,
            unread_channels = 0,
            unread_count = 0,
            mutes = emptyList(),
            teams = emptyList(),
            teams_role = null,
            channel_mutes = emptyList(),
            blocked_user_ids = null,
            extraData = emptyMap(),
        )

    @Language("JSON")
    val downstreamJsonWithoutImageAndName =
        """{
            "id": "",
            "role": "",
            "name": null,
            "image": null,
            "invisible": false,
            "banned": false,
            "devices": [],
            "online": false,
            "created_at": null,
            "deactivated_at": null,
            "updated_at": null,
            "last_active": null,
            "total_unread_count": 0,
            "unread_channels": 0,
            "unread_count": 0,
            "mutes": [],
            "teams": [],
            "channel_mutes": []
         }"""

    val downstreamUserWithoutImageAndName =
        DownstreamUserDto(
            banned = false,
            id = "",
            name = null,
            image = null,
            invisible = false,
            privacy_settings = null,
            language = null,
            role = "",
            devices = emptyList(),
            online = false,
            updated_at = null,
            deactivated_at = null,
            created_at = null,
            last_active = null,
            total_unread_count = 0,
            unread_channels = 0,
            unread_count = 0,
            mutes = emptyList(),
            teams = emptyList(),
            teams_role = null,
            channel_mutes = emptyList(),
            blocked_user_ids = null,
            extraData = emptyMap(),
        )

    @Language("JSON")
    val downstreamJson =
        """{
            "id": "userId",
            "role": "owner",
            "invisible": false,
            "privacy_settings": {
              "typing_indicators": {
                "enabled": false
              },
              "read_receipts": {
                "enabled": false
              }
            },
            "language": "language",
            "banned": false,
            "devices": [
             {
              "id": "deviceId",
              "push_provider": "provider",
              "provider_name": "provider_name"
             }
            ],
            "online": true,
            "created_at": "2020-06-10T11:04:31.000Z",
            "updated_at": "2020-06-10T11:04:31.588Z",
            "deactivated_at": "2020-06-10T11:04:31.588Z",
            "last_active": "2020-06-10T11:04:31.588Z",
            "total_unread_count": 1,
            "unread_channels": 2,
            "unread_count": 3,
            "mutes": [
            {
             "user": $downstreamJsonWithoutExtraData,
             "target": $downstreamJsonWithoutExtraData,
             "created_at": "2020-06-10T11:04:31.000Z",
             "updated_at": "2020-06-10T11:04:31.588Z"
            }
            ],
            "teams": [ "team1", "team2" ],
            "teams_role": {
                "team1": "owner",
                "team2": "member"
            },
            "channel_mutes": [],
            "name": "username",
            "image": "image"
         }"""
    val downstreamUser =
        DownstreamUserDto(
            banned = false,
            id = "userId",
            name = "username",
            image = "image",
            invisible = false,
            privacy_settings = PrivacySettingsDto(
                typing_indicators = TypingIndicatorsDto(
                    enabled = false,
                ),
                read_receipts = ReadReceiptsDto(
                    enabled = false,
                ),
            ),
            language = "language",
            role = "owner",
            devices = listOf(DeviceDto(id = "deviceId", push_provider = "provider", provider_name = "provider_name")),
            online = true,
            updated_at = Date(1591787071588),
            created_at = Date(1591787071000),
            deactivated_at = Date(1591787071588),
            last_active = Date(1591787071588),
            total_unread_count = 1,
            unread_channels = 2,
            unread_count = 3,
            mutes = listOf(
                DownstreamMuteDto(
                    user = downstreamUserWithoutExtraData,
                    target = downstreamUserWithoutExtraData,
                    created_at = Date(1591787071000),
                    updated_at = Date(1591787071588),
                    null,
                ),
            ),
            teams = listOf("team1", "team2"),
            teams_role = mapOf(
                "team1" to "owner",
                "team2" to "member",
            ),
            channel_mutes = emptyList(),
            blocked_user_ids = null,
            extraData = emptyMap(),
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
              }
            },
            "language": "language",
            "role": "owner",
            "devices": [
             {
              "id": "deviceId",
              "push_provider": "provider",
              "provider_name": "provider_name"
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
            typing_indicators = TypingIndicatorsDto(
                enabled = false,
            ),
            read_receipts = ReadReceiptsDto(
                enabled = false,
            ),
        ),
        banned = false,
        devices = listOf(DeviceDto(id = "deviceId", push_provider = "provider", provider_name = "provider_name")),
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
