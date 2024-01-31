package io.getstream.chat.android.offline.repository.database.internal.debug

import androidx.room.RoomDatabase
import io.getstream.log.StreamLog

internal class RoomQueryLogger : RoomDatabase.QueryCallback {
    override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
        StreamLog.v("Chat:RoomLog") { "Query: $sqlQuery, Args: $bindArgs" }
    }
}