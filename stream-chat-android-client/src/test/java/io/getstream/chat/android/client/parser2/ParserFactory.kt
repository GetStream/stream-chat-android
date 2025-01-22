package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.utils.ApiModelTransformers

internal object ParserFactory {
    fun createMoshiChatParser(
        currentUserIdProvider: () -> String = { "" },
        apiModelTransformers: ApiModelTransformers = ApiModelTransformers(),
    ): MoshiChatParser = MoshiChatParser(
        eventMapping = EventMapping(
            DomainMapping(
                currentUserIdProvider = currentUserIdProvider,
                channelTransformer = apiModelTransformers.receiveChannelTransformer,
                messageTransformer = apiModelTransformers.receiveMessageTransformer,
                userTransformer = apiModelTransformers.receiveUserTransformer,
            ),
        ),
        dtoMapping = DtoMapping(
            messageTransformer = apiModelTransformers.sendMessageTransformer,
            userTransformer = apiModelTransformers.sendUserTransformers,
        ),
    )
}