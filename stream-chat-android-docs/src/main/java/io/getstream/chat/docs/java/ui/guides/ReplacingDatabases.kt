package io.getstream.chat.docs.java.ui.guides

import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.persistance.repository.AttachmentRepository
import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy

/**
 * [Adding Custom Attachments](https://getstream.io/chat/docs/sdk/android/client/guides/replace-database/)
 */
class ReplacingDatabases {

    /**
     * [Using a custom RepositoryFactoryProvider ](https://getstream.io/chat/docs/sdk/android/client/guides/replace-database/#using-a-custom-repositoryfactoryprovider)
     */
    class UsingACustomRepositoryFactoryProvider: Fragment() {

        val client = ChatClient.Builder("api_key_here", requireContext())
            .withRepositoryFactoryProvider { RealmRepositoryFactory(provideRealm()) }
            .build()

        public fun configureRealm(): Realm =
            RealmConfiguration.Builder(schema = realmSchema())
                .schemaVersion(SCHEMA_VERSION)
                .deleteRealmIfMigrationNeeded()
                .build()
                .let(Realm::open)


        public class RealmRepositoryFactory(private val realm: Realm) : RepositoryFactory {
            override fun createUserRepository(): UserRepository = RealmUserRepository(realm)

            override fun createChannelConfigRepository(): ChannelConfigRepository = RealmChannelConfigRepository(realm)

            override fun createQueryChannelsRepository(): QueryChannelsRepository = RealmQueryChannelsRepository(realm)

            override fun createSyncStateRepository(): SyncStateRepository = RealmSyncStateRepository(realm)

            override fun createAttachmentRepository(): AttachmentRepository = RealmAttachmentRepository(realm)

            override fun createReactionRepository(
                getUser: suspend (userId: String) -> User,
            ): ReactionRepository = RealmReactionRepository(realm)

            override fun createMessageRepository(
                getUser: suspend (userId: String) -> User,
            ): MessageRepository = RealmMessageRepository(realm)

            override fun createChannelRepository(
                getUser: suspend (userId: String) -> User,
                getMessage: suspend (messageId: String) -> Message?,
            ): ChannelRepository = RealmChannelRepository(realm)
        }
    }

    public class RealmChannelRepository(private val realm: Realm) : ChannelRepository {
        override suspend fun clear() {
            realm.writeBlocking {
                query<ChannelEntityRealm>().find().let(this::delete)
            }
        }

        override suspend fun deleteChannel(cid: String) {
            val channel = realm.query<ChannelEntityRealm>("cid == '$cid'")
                .first()
                .find()

            realm.writeBlocking {
                channel?.let(::findLatest)?.let(::delete)
            }
        }

        override suspend fun insertChannel(channel: Channel) {
            realm.writeBlocking {
                this.copyToRealm(channel.toRealm(), updatePolicy = UpdatePolicy.ALL)
            }
        }

        override suspend fun selectChannelByCid(cid: String): Channel? =
            selectChannelByCidRealm(cid)?.toDomain()

        private fun selectChannelByCidRealm(cid: String): ChannelEntityRealm? =
            realm.query<ChannelEntityRealm>("cid == '$cid'")
                .first()
                .find()

        // All the other methods must be implemented, following this same approach.
    }
}
