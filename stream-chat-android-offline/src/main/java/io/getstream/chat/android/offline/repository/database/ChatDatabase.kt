package io.getstream.chat.android.offline.repository.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import io.getstream.chat.android.offline.repository.database.converter.DateConverter
import io.getstream.chat.android.offline.repository.database.converter.ExtraDataConverter
import io.getstream.chat.android.offline.repository.database.converter.FilterObjectConverter
import io.getstream.chat.android.offline.repository.database.converter.ListConverter
import io.getstream.chat.android.offline.repository.database.converter.MapConverter
import io.getstream.chat.android.offline.repository.database.converter.SetConverter
import io.getstream.chat.android.offline.repository.database.converter.SyncStatusConverter
import io.getstream.chat.android.offline.repository.domain.channel.ChannelDao
import io.getstream.chat.android.offline.repository.domain.channel.ChannelEntity
import io.getstream.chat.android.offline.repository.domain.channelconfig.ChannelConfigDao
import io.getstream.chat.android.offline.repository.domain.channelconfig.ChannelConfigInnerEntity
import io.getstream.chat.android.offline.repository.domain.channelconfig.CommandInnerEntity
import io.getstream.chat.android.offline.repository.domain.message.MessageDao
import io.getstream.chat.android.offline.repository.domain.message.MessageInnerEntity
import io.getstream.chat.android.offline.repository.domain.message.attachment.AttachmentEntity
import io.getstream.chat.android.offline.repository.domain.queryChannels.ChannelSortInnerEntity
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsDao
import io.getstream.chat.android.offline.repository.domain.queryChannels.QueryChannelsEntity
import io.getstream.chat.android.offline.repository.domain.reaction.ReactionDao
import io.getstream.chat.android.offline.repository.domain.reaction.ReactionEntity
import io.getstream.chat.android.offline.repository.domain.syncState.SyncStateDao
import io.getstream.chat.android.offline.repository.domain.syncState.SyncStateEntity
import io.getstream.chat.android.offline.repository.domain.user.UserDao
import io.getstream.chat.android.offline.repository.domain.user.UserEntity

@Database(
    entities = [
        QueryChannelsEntity::class,
        ChannelSortInnerEntity::class,
        MessageInnerEntity::class,
        AttachmentEntity::class,
        UserEntity::class,
        ReactionEntity::class,
        ChannelEntity::class,
        ChannelConfigInnerEntity::class,
        CommandInnerEntity::class,
        SyncStateEntity::class
    ],
    version = 42,
    exportSchema = false
)

@TypeConverters(
    FilterObjectConverter::class,
    ExtraDataConverter::class,
    ListConverter::class,
    MapConverter::class,
    SetConverter::class,
    SyncStatusConverter::class,
    DateConverter::class
)
internal abstract class ChatDatabase : RoomDatabase() {
    abstract fun queryChannelsDao(): QueryChannelsDao
    abstract fun userDao(): UserDao
    abstract fun reactionDao(): ReactionDao
    abstract fun messageDao(): MessageDao
    abstract fun channelStateDao(): ChannelDao
    abstract fun channelConfigDao(): ChannelConfigDao
    abstract fun syncStateDao(): SyncStateDao

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
