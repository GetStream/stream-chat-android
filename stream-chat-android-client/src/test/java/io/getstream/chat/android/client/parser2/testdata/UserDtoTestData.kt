package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import org.intellij.lang.annotations.Language

internal object UserDtoTestData {

    @Language("JSON")
    val downstreamJson =
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
    val downstreamUser =
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
}
