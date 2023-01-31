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

package io.getstream.chat.docs.java.ui.guides.realm.repository

import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.docs.java.ui.guides.realm.entities.ConfigEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.toDomain
import io.getstream.chat.docs.java.ui.guides.realm.entities.toRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query

internal class RealmChannelConfigRepository(private val realm: Realm) : ChannelConfigRepository {

    override suspend fun cacheChannelConfigs() {
        // There's no cache.
    }

    override fun selectChannelConfig(channelType: String): ChannelConfig? =
        realm.query<ConfigEntityRealm>("channel_type == '$channelType'")
            .first()
            .find()
            ?.toDomain()

    override suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>) {
        val configsRealm = configs.map { config -> config.toRealm() }
        realm.writeBlocking {
            configsRealm.forEach { configRealm ->
                copyToRealm(configRealm, UpdatePolicy.ALL)
            }
        }
    }

    override suspend fun insertChannelConfig(config: ChannelConfig) {
        realm.writeBlocking { copyToRealm(config.toRealm(), UpdatePolicy.ALL) }
    }

    override suspend fun clear() {
        val configs = realm.query<ConfigEntityRealm>().find()

        realm.write {
            delete(configs)
        }
    }
}
