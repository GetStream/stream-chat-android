/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.persistence.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import io.getstream.chat.android.client.persistence.db.converter.DateConverter
import io.getstream.chat.android.client.persistence.db.dao.MessageReceiptDao
import io.getstream.chat.android.client.persistence.db.entity.MessageReceiptEntity

@Database(
    entities = [MessageReceiptEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(
    DateConverter::class,
)
internal abstract class ChatClientDatabase : RoomDatabase() {
    abstract fun messageReceiptDao(): MessageReceiptDao

    companion object {
        fun build(context: Context) = Room.databaseBuilder(
            context = context.applicationContext,
            klass = ChatClientDatabase::class.java,
            name = "stream_chat_client.db",
        )
            .fallbackToDestructiveMigration()
            .addCallback(
                object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        db.execSQL("PRAGMA synchronous = NORMAL")
                    }
                },
            )
            .build()
    }
}
