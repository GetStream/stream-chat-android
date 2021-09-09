package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.SearchWarningDto
import io.getstream.chat.android.client.models.SearchWarning

internal fun SearchWarningDto.toDomain(): SearchWarning {
    return SearchWarning(
        channelSearchCids = channel_search_cids,
        channelSearchCount = channel_search_count,
        warningCode = warning_code,
        warningDescription = warning_description,
    )
}
