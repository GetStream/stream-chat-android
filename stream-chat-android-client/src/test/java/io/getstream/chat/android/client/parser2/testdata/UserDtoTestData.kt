package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import org.intellij.lang.annotations.Language
import java.util.Date

internal object UserDtoTestData {

    @Language("JSON")
    val downstreamJsonWithoutExtraData =
        """{
            "id": "",
            "role": "",
            "invisible": false,
            "banned": false,
            "devices": [],
            "online": false,
            "created_at": null,
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
            invisible = false,
            role = "",
            devices = emptyList(),
            online = false,
            updated_at = null,
            created_at = null,
            last_active = null,
            total_unread_count = 0,
            unread_channels = 0,
            unread_count = 0,
            mutes = emptyList(),
            teams = emptyList(),
            channel_mutes = emptyList(),
            extraData = emptyMap(),
        )

    @Language("JSON")
    val downstreamJson =
        """{
            "id": "userId",
            "role": "owner",
            "invisible": false,
            "banned": false,
            "devices": [
             {
              "id": "deviceId",
              "push_provider": "provider"
             }
            ],
            "online": true,
            "created_at": "2020-06-10T11:04:31.000Z",
            "updated_at": "2020-06-10T11:04:31.588Z",
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
            "channel_mutes": [],
            "name": "username"
         }"""
    val downstreamUser =
        DownstreamUserDto(
            banned = false,
            id = "userId",
            invisible = false,
            role = "owner",
            devices = listOf(DeviceDto(id = "deviceId", push_provider = "provider")),
            online = true,
            updated_at = Date(1591787071588),
            created_at = Date(1591787071000),
            last_active = Date(1591787071588),
            total_unread_count = 1,
            unread_channels = 2,
            unread_count = 3,
            mutes = listOf(
                DownstreamMuteDto(
                    user = downstreamUserWithoutExtraData,
                    target = downstreamUserWithoutExtraData,
                    created_at = Date(1591787071000),
                    updated_at = Date(1591787071588)
                ),
            ),
            teams = listOf("team1", "team2"),
            channel_mutes = emptyList(),
            extraData = mapOf("name" to "username"),
        )

    @Language("JSON")
    val upstreamJsonWithoutExtraData =
        """{
            "banned": true,
            "id": "userId",
            "invisible": false,
            "role": "owner",
            "devices": [],
            "teams": []
         }""".withoutWhitespace()

    val upstreamUserWithoutExtraData = UpstreamUserDto(
        id = "userId",
        role = "owner",
        invisible = false,
        banned = true,
        devices = emptyList(),
        teams = emptyList(),
        extraData = emptyMap(),
    )

    @Language("JSON")
    val upstreamJson =
        """{
            "banned": false,
            "id": "userId",
            "invisible": false,
            "role": "owner",
            "devices": [
             {
              "id": "deviceId",
              "push_provider": "provider"
             }
            ],
            "teams": [ "team1", "team2"],
            "name": "username"
         }""".withoutWhitespace()

    val upstreamUser = UpstreamUserDto(
        id = "userId",
        role = "owner",
        invisible = false,
        banned = false,
        devices = listOf(DeviceDto(id = "deviceId", push_provider = "provider")),
        teams = listOf("team1", "team2"),
        extraData = mapOf("name" to "username"),
    )
}
