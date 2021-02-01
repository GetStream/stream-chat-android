package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UserDto
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

internal class DtoMapper {

    fun toDto(message: Message): UpstreamMessageDto = message.run {
        UpstreamMessageDto(
            id = id,
            cid = cid,
            text = text,
            html = html,
            parent_id = parentId,
            command = command,
            user = toDto(user),
            silent = silent,
            shadowed = shadowed,
            extraData = extraData,
        )
    }

    fun toDomain(message: DownstreamMessageDto): Message = message.run {
        return Message(
            id = id,
            cid = cid,
            text = text,
            html = html,
            parentId = parent_id,
            command = command,
            user = toDomain(user),
            silent = silent,
            shadowed = shadowed,
            createdAt = created_at,
            updatedAt = updated_at,
            deletedAt = deleted_at,
        )
    }

    fun toDto(user: User): UserDto = user.run {
        UserDto(
            id = id,
            role = role,
            invisible = invisible,
            banned = banned,
        )
    }

    fun toDomain(user: UserDto): User = user.run {
        return User(
            id = id,
            role = role,
            invisible = invisible,
            banned = banned,
        )
    }
}
