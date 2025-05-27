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

package io.getstream.chat.android.e2e.test.rules

import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import io.getstream.chat.android.compose.uiautomator.allureLogcat
import io.getstream.chat.android.compose.uiautomator.allureScreenshot
import io.getstream.chat.android.compose.uiautomator.allureWindowHierarchy
import io.getstream.chat.android.compose.uiautomator.device
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File

/** Annotation to retry a specific failed test. **/
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
public annotation class Retry(val count: Int)

/** Rule to retry all failed tests. **/
public class RetryRule(private val count: Int) : TestRule {

    override fun apply(base: Statement, description: Description): Statement = statement(
        base,
        description,
    )

    private fun statement(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val retryAnnotation: Retry? = description.getAnnotation(Retry::class.java)
                val retryCount = retryAnnotation?.count ?: count
                val databaseOperations = DatabaseOperations()
                var caughtThrowable: Throwable? = null

                for (i in 0 until retryCount) {
                    try {
                        System.err.println("${description.displayName}: run #${i + 1} started.")
                        device.executeShellCommand("logcat -c")
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        System.err.println("${description.displayName}: run #${i + 1} failed.")
                        databaseOperations.clearDatabases()
                        caughtThrowable = t
                        device.allureLogcat(name = "logcat_${i + 1}")
                        device.allureScreenshot(name = "screenshot_${i + 1}")
                        device.allureWindowHierarchy(name = "hierarchy_${i + 1}")
                    }
                }

                throw caughtThrowable ?: IllegalStateException()
            }
        }
    }
}

public open class DatabaseOperations {

    public open fun clearDatabases() {
        val databaseOperations = DatabaseOperations()
        val dbFiles = databaseOperations.getAllDatabaseFiles().filterNot {
            shouldIgnoreFile(
                it.path,
            )
        }
        dbFiles.forEach { clearDatabase(it, databaseOperations) }
    }

    private fun shouldIgnoreFile(path: String): Boolean {
        val ignoredSuffixes = arrayOf("-journal", "-shm", "-uid", "-wal")
        return ignoredSuffixes.any { path.endsWith(it) }
    }

    private fun clearDatabase(dbFile: File, dbOperations: DatabaseOperations) {
        dbOperations.openDatabase(dbFile).use { database ->
            val tablesToClear = dbOperations.getTableNames(
                database,
            ).filterNot { it == "room_master_table" }
            tablesToClear.forEach { dbOperations.deleteTableContent(database, it) }
        }
    }

    private fun getAllDatabaseFiles(): List<File> {
        return InstrumentationRegistry.getInstrumentation().targetContext.let { context ->
            context.databaseList().map { context.getDatabasePath(it) }
        }
    }

    private fun openDatabase(databaseFile: File): SQLiteDatabase {
        return SQLiteDatabase.openDatabase(databaseFile.absolutePath, null, 0)
    }

    private fun getTableNames(sqLiteDatabase: SQLiteDatabase): List<String> {
        sqLiteDatabase.rawQuery(
            "SELECT name FROM sqlite_master WHERE type IN (?, ?)",
            arrayOf("table", "view"),
        )
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
