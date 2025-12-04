/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.domain.channelconfig.internal

import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.models.ChannelConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections

/**
 * The channel config repository stores all channel configs in room as well as in memory.
 * Call channelConfigRepository.load to load all configs into memory.
 */
internal class DatabaseChannelConfigRepository(
    private val channelConfigDao: ChannelConfigDao,
) : ChannelConfigRepository {
    private val channelConfigs: MutableMap<String, ChannelConfig> = Collections.synchronizedMap(mutableMapOf())
    private val mutex = Mutex()

    /**
     * Caches in memory data from DB.
     */
    override suspend fun cacheChannelConfigs() {
        channelConfigs += channelConfigDao.selectAll().map(ChannelConfigEntity::toModel)
            .associateBy(ChannelConfig::type)
    }

    /**
     * Select the [ChannelConfig] for a channel type.
     */
    override fun selectChannelConfig(channelType: String): ChannelConfig? {
        return channelConfigs[channelType]
    }

    /**
     * Writes many [ChannelConfig]
     */
    override suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>) {
        // update the local configs
        channelConfigs += configs.associateBy(ChannelConfig::type)

        // insert into room db
        mutex.withLock {
            channelConfigDao.insert(configs.map(ChannelConfig::toEntity))
        }
    }

    /**
     * Writes [ChannelConfig]
     */
    override suspend fun insertChannelConfig(config: ChannelConfig) {
        channelConfigs += config.type to config
        mutex.withLock {
            channelConfigDao.insert(config.toEntity())
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            channelConfigDao.deleteAll()
        }
    }
}
