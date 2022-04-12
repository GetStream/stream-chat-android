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

package io.getstream.chat.android.offline.repository.database.internal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import io.getstream.chat.android.offline.repository.database.converter.internal.DateConverter
import io.getstream.chat.android.offline.repository.database.converter.internal.ExtraDataConverter
import io.getstream.chat.android.offline.repository.database.converter.internal.FilterObjectConverter
import io.getstream.chat.android.offline.repository.database.converter.internal.ListConverter
import io.getstream.chat.android.offline.repository.database.converter.internal.MapConverter
import io.getstream.chat.android.offline.repository.database.converter.internal.QuerySortConverter
import io.getstream.chat.android.offline.repository.database.converter.internal.SetConverter
import io.getstream.chat.android.offline.repository.database.converter.internal.SyncStatusConverter
import io.getstream.chat.android.offline.repository.domain.channel.internal.ChannelDao
import io.getstream.chat.android.offline.repository.domain.channel.internal.ChannelEntity
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.ChannelConfigDao
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.ChannelConfigInnerEntity
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.CommandInnerEntity
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentDao
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentEntity
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageDao
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageInnerEntity
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsDao
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsEntity
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionDao
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionEntity
import io.getstream.chat.android.offline.repository.domain.syncState.internal.SyncStateDao
import io.getstream.chat.android.offline.repository.domain.syncState.internal.SyncStateEntity
import io.getstream.chat.android.offline.repository.domain.user.internal.UserDao
import io.getstream.chat.android.offline.repository.domain.user.internal.UserEntity

@Database(
    entities = [
        QueryChannelsEntity::class,
        MessageInnerEntity::class,
        AttachmentEntity::class,
        UserEntity::class,
        ReactionEntity::class,
        ChannelEntity::class,
        ChannelConfigInnerEntity::class,
        CommandInnerEntity::class,
        SyncStateEntity::class,
    ],
    version = 56,
    exportSchema = false
)
@TypeConverters(
    FilterObjectConverter::class,
    ListConverter::class,
    MapConverter::class,
    QuerySortConverter::class,
    ExtraDataConverter::class,
    SetConverter::class,
    SyncStatusConverter::class,
    DateConverter::class,
)
internal abstract class ChatDatabase : RoomDatabase() {
    abstract fun queryChannelsDao(): QueryChannelsDao
    abstract fun userDao(): UserDao
    abstract fun reactionDao(): ReactionDao
    abstract fun messageDao(): MessageDao
    abstract fun channelStateDao(): ChannelDao
    abstract fun channelConfigDao(): ChannelConfigDao
    abstract fun syncStateDao(): SyncStateDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        @Volatile
        private var INSTANCES: MutableMap<String, ChatDatabase?> = mutableMapOf()

        fun getDatabase(context: Context, userId: String): ChatDatabase {
            if (!INSTANCES.containsKey(userId)) {
                synchronized(this) {
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        ChatDatabase::class.java,
                        "stream_chat_database_$userId"
                    ).fallbackToDestructiveMigration()
                        .addCallback(
                            object : Callback() {
                                override fun onOpen(db: SupportSQLiteDatabase) {
                                    db.execSQL("PRAGMA synchronous = 1")
                                }
                            }
                        )
                        .build()
                    INSTANCES[userId] = db
                }
            }
            return INSTANCES[userId] ?: error("DB not created")
        }
    }
}
