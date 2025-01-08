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

package io.getstream.chat.android.compose.sample.feature.channel

import io.getstream.chat.android.models.Channel

/**
 * Constants related to channels for the app.
 */
object ChannelConstants {

    /**
     * Argument key for the "draft" extra data in the context of a channel.
     */
    const val CHANNEL_ARG_DRAFT = "draft"
}

/**
 * Checks if the channel is a draft channel.
 */
val Channel.isDraft: Boolean
    get() = getExtraValue(ChannelConstants.CHANNEL_ARG_DRAFT, false)
