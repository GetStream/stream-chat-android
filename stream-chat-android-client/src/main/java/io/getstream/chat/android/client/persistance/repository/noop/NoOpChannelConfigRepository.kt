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

package io.getstream.chat.android.client.persistance.repository.noop

import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.models.ChannelConfig

/**
 * No-Op ChannelConfigRepository.
 */
internal object NoOpChannelConfigRepository : ChannelConfigRepository {
    override suspend fun cacheChannelConfigs() { /* No-Op */ }
    override fun selectChannelConfig(channelType: String): ChannelConfig? = null
    override suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>) { /* No-Op */ }
    override suspend fun insertChannelConfig(config: ChannelConfig) { /* No-Op */ }
    override suspend fun clear() { /* No-Op */ }
}
