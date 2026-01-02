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

import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase

/**
 * A [ChannelConfigDao] implementation which lazily retrieves the original [ChannelConfigDao] from the currently active
 * [ChatDatabase] instance. The [ChatDatabase] instance can change in runtime if it becomes corrupted
 * and is manually recreated.
 *
 * @param getDatabase Method retrieving the current instance of [ChatDatabase].
 */
internal class RecoverableChannelConfigDao(private val getDatabase: () -> ChatDatabase) : ChannelConfigDao {

    private val delegate: ChannelConfigDao
        get() = getDatabase().channelConfigDao()

    override suspend fun insert(channelConfigEntities: List<ChannelConfigEntity>) {
        delegate.insert(channelConfigEntities)
    }

    override suspend fun insert(channelConfigEntity: ChannelConfigEntity) {
        delegate.insert(channelConfigEntity)
    }

    override suspend fun insertConfig(channelConfigInnerEntity: ChannelConfigInnerEntity) {
        delegate.insertConfig(channelConfigInnerEntity)
    }

    override suspend fun insertConfigs(channelConfigInnerEntities: List<ChannelConfigInnerEntity>) {
        delegate.insertConfigs(channelConfigInnerEntities)
    }

    override suspend fun insertCommands(commands: List<CommandInnerEntity>) {
        delegate.insertCommands(commands)
    }

    override suspend fun selectAll(): List<ChannelConfigEntity> {
        return delegate.selectAll()
    }

    override suspend fun deleteCommands() {
        delegate.deleteCommands()
    }

    override suspend fun deleteConfigs() {
        delegate.deleteConfigs()
    }

    override suspend fun deleteAll() {
        delegate.deleteAll()
    }
}
