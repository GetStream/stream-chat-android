package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.client.api2.model.dto.ChannelInfoDto
import org.intellij.lang.annotations.Language

internal object ChannelInfoDtoTestData {

    @Language("JSON")
    val channelInfoJsonWithoutMemberCount =
        """{
          "cid": "channelType:channelId",
          "id": "channelId",
          "type": "channelType",
          "name": "name"
        }
        """.withoutWhitespace()
    val channelInfoDtoWithoutMemberCount = ChannelInfoDto(
        cid = "channelType:channelId",
        id = "channelId",
        type = "channelType",
        member_count = 0,
        name = "name"
    )
}
