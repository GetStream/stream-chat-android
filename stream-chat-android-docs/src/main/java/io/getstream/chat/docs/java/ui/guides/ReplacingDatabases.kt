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
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.docs.java.ui.guides.realm.entities.AttachmentEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.ChannelUserReadEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.CommandEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.ConfigEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.MemberEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.MessageEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.QueryChannelsEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.QuerySorterInfoEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.ReactionCountEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.ReactionEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.ReactionScoreEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.SortSpecificationEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.SyncStateEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.UploadStateEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.UserEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.repository.RealmAttachmentRepository
import io.getstream.chat.docs.java.ui.guides.realm.repository.RealmChannelConfigRepository
import io.getstream.chat.docs.java.ui.guides.realm.repository.RealmChannelRepository
import io.getstream.chat.docs.java.ui.guides.realm.repository.RealmMessageRepository
import io.getstream.chat.docs.java.ui.guides.realm.repository.RealmQueryChannelsRepository
import io.getstream.chat.docs.java.ui.guides.realm.repository.RealmReactionRepository
import io.getstream.chat.docs.java.ui.guides.realm.repository.RealmSyncStateRepository
import io.getstream.chat.docs.java.ui.guides.realm.repository.RealmUserRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import kotlin.reflect.KClass

/**
 * [Adding Custom Attachments](https://getstream.io/chat/docs/sdk/android/client/guides/replace-database/)
 */
class ReplacingDatabases {

    /**
     * [Using a custom RepositoryFactoryProvider ](https://getstream.io/chat/docs/sdk/android/client/guides/replace-database/#using-a-custom-repositoryfactoryprovider)
     */
    class UsingACustomRepositoryFactoryProvider: Fragment() {

private fun configureRealm(): Realm =
    RealmConfiguration.Builder(schema = realmSchema())
        .schemaVersion(1)
        .deleteRealmIfMigrationNeeded()
        .build()
        .let(Realm::open)

private fun realmSchema(): Set<KClass<out RealmObject>> =
    setOf(
        MessageEntityRealm::class,
        ChannelEntityRealm::class,
        UserEntityRealm::class,
        QueryChannelsEntityRealm::class,
        MemberEntityRealm::class,
        ChannelUserReadEntityRealm::class,
        ReactionEntityRealm::class,
        ReactionCountEntityRealm::class,
        ReactionScoreEntityRealm::class,
        ConfigEntityRealm::class,
        CommandEntityRealm::class,
        QuerySorterInfoEntityRealm::class,
        SortSpecificationEntityRealm::class,
        SyncStateEntityRealm::class,
        AttachmentEntityRealm::class,
        UploadStateEntityRealm::class,
    )

        val client = ChatClient.Builder("api_key_here", requireContext())
            .withRepositoryFactoryProvider { RealmRepositoryFactory(configureRealm()) }
            .build()

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
}
