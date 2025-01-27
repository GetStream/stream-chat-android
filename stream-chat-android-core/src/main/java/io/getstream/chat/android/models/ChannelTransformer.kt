/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.models

/**
 * A transformer that can be used to transform a channel object before third parties can access it.
 * This is useful for adding extra data to the channel object and/or encrypting/decrypting it.
 */
public fun interface ChannelTransformer {

    /**
     * Transforms the [channel] before returning it to the caller.
     * This can be used to add extra data to the channel object and/or encrypt/decrypt it.
     *
     * @return The transformed channel.
     */
    public fun transform(channel: Channel): Channel
}

/**
 * A no-op implementation of [ChannelTransformer].
 */
public object NoOpChannelTransformer : ChannelTransformer {

    /**
     * Returns the [channel] as is.
     */
    override fun transform(channel: Channel): Channel = channel
}
