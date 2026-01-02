/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.transformer

import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserTransformer

/**
 * A class that holds the transformers used to transform the API models.
 *
 * @param outgoingMessageTransformer The transformer used to transform the message before sending it to the API.
 * @param incomingMessageTransformer The transformer used to transform the message received from the API.
 * @param incomingChannelTransformer The transformer used to transform the channel received from the API.
 * @param outgoingUserTransformers The transformer used to transform the user before sending it to the API.
 * @param incomingUserTransformer The transformer used to transform the user received from the API.
 */
public class ApiModelTransformers(
    public val outgoingMessageTransformer: MessageTransformer = NoOpMessageTransformer,
    public val incomingMessageTransformer: MessageTransformer = NoOpMessageTransformer,
    public val incomingChannelTransformer: ChannelTransformer = NoOpChannelTransformer,
    public val outgoingUserTransformers: UserTransformer = NoOpUserTransformer,
    public val incomingUserTransformer: UserTransformer = NoOpUserTransformer,
)
