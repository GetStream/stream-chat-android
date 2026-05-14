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

package io.getstream.chat.android.state.event.handler.chat

import io.getstream.chat.android.models.Channel

/**
 * Default [ChannelGroupResolver] backed by `channel.extraData`.
 *
 * Reads an explicit group key from `channel.extraData[groupFieldName]` (defaults to `"group"`)
 * and always includes the [allGroupKey] sentinel (defaults to `"all"`) so that a designated
 * "all channels" grouped query always sees every channel.
 *
 * @param groupFieldName The key in `channel.extraData` carrying the explicit group identifier.
 * @param allGroupKey The sentinel group key representing "every channel". Pass `null` to disable
 * the implicit sentinel.
 */
public class DefaultChannelGroupResolver(
    private val groupFieldName: String = DEFAULT_GROUP_FIELD_NAME,
    private val allGroupKey: String? = DEFAULT_ALL_GROUP_KEY,
) : ChannelGroupResolver {

    override fun resolve(channel: Channel, currentGroup: String): Set<String> = buildSet {
        (channel.extraData[groupFieldName] as? String)?.let(::add)
        allGroupKey?.let(::add)
    }

    public companion object {
        public const val DEFAULT_GROUP_FIELD_NAME: String = "group"
        public const val DEFAULT_ALL_GROUP_KEY: String = "all"
    }
}
