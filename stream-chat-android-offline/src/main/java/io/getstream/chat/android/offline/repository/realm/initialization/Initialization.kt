package io.getstream.chat.android.offline.repository.realm.initialization

import io.getstream.chat.android.offline.repository.realm.entity.ChannelEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.ChannelUserReadEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.MemberEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.MessageEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.QueryChannelsEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.ReactionCountEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.ReactionEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.ReactionScoreEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.UserEntityRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import kotlin.reflect.KClass

private const val SCHEMA_VERSION = 18L

public fun configureRealm(): Realm =
    RealmConfiguration.Builder(schema = realmSchema())
        .schemaVersion(SCHEMA_VERSION)
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
    )
