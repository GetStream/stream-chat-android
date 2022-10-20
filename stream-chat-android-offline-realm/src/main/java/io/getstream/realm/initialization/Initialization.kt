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

package io.getstream.realm.initialization

import io.getstream.realm.entity.AttachmentEntityRealm
import io.getstream.realm.entity.ChannelEntityRealm
import io.getstream.realm.entity.ChannelUserReadEntityRealm
import io.getstream.realm.entity.CommandEntityRealm
import io.getstream.realm.entity.ConfigEntityRealm
import io.getstream.realm.entity.FilterNodeEntity
import io.getstream.realm.entity.MemberEntityRealm
import io.getstream.realm.entity.MessageEntityRealm
import io.getstream.realm.entity.QueryChannelsEntityRealm
import io.getstream.realm.entity.QuerySorterInfoEntityRealm
import io.getstream.realm.entity.ReactionCountEntityRealm
import io.getstream.realm.entity.ReactionEntityRealm
import io.getstream.realm.entity.ReactionScoreEntityRealm
import io.getstream.realm.entity.SortSpecificationEntityRealm
import io.getstream.realm.entity.SyncStateEntityRealm
import io.getstream.realm.entity.UploadStateEntityRealm
import io.getstream.realm.entity.UserEntityRealm
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
        FilterNodeEntity::class,
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
