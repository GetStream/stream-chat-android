package io.getstream.chat.android.client.sample.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.getstream.chat.android.client.sample.common.Channel

@Database(entities = [Channel::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun channels(): ChannelsDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "main.db"
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
