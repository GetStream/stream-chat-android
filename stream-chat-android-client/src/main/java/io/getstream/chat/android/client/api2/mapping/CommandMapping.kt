package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.CommandDto
import io.getstream.chat.android.client.models.Command

internal fun CommandDto.toDomain(): Command {
    return Command(
        name = name,
        description = description,
        args = args,
        set = set,
    )
}
