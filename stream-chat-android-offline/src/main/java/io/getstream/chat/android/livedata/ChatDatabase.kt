package io.getstream.chat.android.livedata

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import io.getstream.chat.android.livedata.converter.DateConverter
import io.getstream.chat.android.livedata.converter.ExtraDataConverter
import io.getstream.chat.android.livedata.converter.FilterObjectConverter
import io.getstream.chat.android.livedata.converter.ListConverter
import io.getstream.chat.android.livedata.converter.MapConverter
import io.getstream.chat.android.livedata.converter.SetConverter
import io.getstream.chat.android.livedata.converter.SyncStatusConverter
import io.getstream.chat.android.livedata.dao.ChannelConfigDao
import io.getstream.chat.android.livedata.dao.ChannelDao
import io.getstream.chat.android.livedata.dao.MessageDao
import io.getstream.chat.android.livedata.dao.QueryChannelsDao
import io.getstream.chat.android.livedata.dao.ReactionDao
import io.getstream.chat.android.livedata.dao.SyncStateDao
import io.getstream.chat.android.livedata.dao.UserDao
import io.getstream.chat.android.livedata.entity.AttachmentEntity
import io.getstream.chat.android.livedata.entity.ChannelConfigInnerEntity
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.ChannelSortInnerEntity
import io.getstream.chat.android.livedata.entity.CommandInnerEntity
import io.getstream.chat.android.livedata.entity.MessageInnerEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.entity.SyncStateEntity
import io.getstream.chat.android.livedata.entity.UserEntity

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
    version = 37,
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
    abstract fun queryChannelsQDao(): QueryChannelsDao
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
