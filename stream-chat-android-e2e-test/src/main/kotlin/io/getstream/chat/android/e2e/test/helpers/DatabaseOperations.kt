/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.e2e.test.helpers

import android.database.sqlite.SQLiteDatabase
import io.getstream.chat.android.compose.uiautomator.appContext
import java.io.File

public open class DatabaseOperations {

    public open fun clearDatabases() {
        val databaseOperations = DatabaseOperations()
        val dbFiles = databaseOperations.getAllDatabaseFiles().filterNot { shouldIgnoreFile(it.path) }
        dbFiles.forEach { clearDatabase(it, databaseOperations) }
    }

    private fun shouldIgnoreFile(path: String): Boolean {
        val ignoredSuffixes = arrayOf("-journal", "-shm", "-uid", "-wal")
        return ignoredSuffixes.any { path.endsWith(it) }
    }

    private fun clearDatabase(dbFile: File, dbOperations: DatabaseOperations) {
        dbOperations.openDatabase(dbFile).use { database ->
            val tablesToClear = dbOperations.getTableNames(database).filterNot { it == "room_master_table" }
            tablesToClear.forEach { dbOperations.deleteTableContent(database, it) }
        }
    }

    private fun getAllDatabaseFiles(): List<File> {
        return appContext.let { context ->
            context.databaseList().map { context.getDatabasePath(it) }
        }
    }

    private fun openDatabase(databaseFile: File): SQLiteDatabase {
        return SQLiteDatabase.openDatabase(databaseFile.absolutePath, null, 0)
    }

    private fun getTableNames(sqLiteDatabase: SQLiteDatabase): List<String> {
        sqLiteDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type IN (?, ?)", arrayOf("table", "view"))
            .use { cursor ->
                val tableNames = ArrayList<String>()
                while (cursor.moveToNext()) {
                    tableNames.add(cursor.getString(0))
                }
                return tableNames
            }
    }

    private fun deleteTableContent(sqLiteDatabase: SQLiteDatabase, tableName: String) {
        sqLiteDatabase.delete(tableName, null, null)
    }
}
