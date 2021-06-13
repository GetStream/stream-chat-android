package io.getstream.chat.android.ui.utils

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.ChatDomain

internal object FilterUtils {
    fun userFilter(chatDomain: ChatDomain): FilterObject {
        return chatDomain.user.value?.id?.let { id ->
            Filters.`in`("members", id)
        } ?: Filters.neutral()
    }
}
