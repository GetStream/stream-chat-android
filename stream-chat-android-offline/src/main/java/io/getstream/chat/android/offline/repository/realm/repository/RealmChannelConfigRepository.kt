package io.getstream.chat.android.offline.repository.realm.repository

import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.offline.repository.realm.entity.ConfigEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.toDomain
import io.getstream.chat.android.offline.repository.realm.entity.toRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query

internal class RealmChannelConfigRepository(private val realm: Realm): ChannelConfigRepository {

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
        realm.writeBlocking { configsRealm.forEach(::copyToRealm) }
    }

    override suspend fun insertChannelConfig(config: ChannelConfig) {
        realm.writeBlocking { copyToRealm(config.toRealm()) }
    }

    override suspend fun clear() {
        TODO("Not yet implemented")
    }
}
