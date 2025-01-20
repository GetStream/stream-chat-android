package io.getstream.chat.android.client.utils

import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer

/**
 * A class that holds the transformers used to transform the API models.
 *
 * @param sendMessageTransformer The transformer used to transform the message before sending it to the API.
 * @param receiveMessageTransformer The transformer used to transform the message received from the API.
 * @param receiveChannelTransformer The transformer used to transform the channel received from the API.
 */
public class ApiModelTransformers(
    public val sendMessageTransformer: MessageTransformer = NoOpMessageTransformer,
    public val receiveMessageTransformer: MessageTransformer = NoOpMessageTransformer,
    public val receiveChannelTransformer: ChannelTransformer = NoOpChannelTransformer,
)