/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import io.getstream.chat.android.e2e.test.uiautomator.allureLogcat
import io.getstream.chat.android.e2e.test.uiautomator.allureScreenrecord
import io.getstream.chat.android.e2e.test.uiautomator.allureScreenshot
import io.getstream.chat.android.e2e.test.uiautomator.allureWindowHierarchy
import io.getstream.chat.android.e2e.test.uiautomator.device
import io.qameta.allure.kotlin.Allure
import io.qameta.allure.kotlin.model.Stage
import io.qameta.allure.kotlin.model.TestResult
import io.qameta.allure.kotlin.util.ResultsUtils
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File
import java.util.UUID

/**
 * Rule to retry failed tests up to [count] attempts.
 *
 * Each failed attempt that is retried is written as its own Allure result sharing the real
 * test's historyId, so Allure TestOps groups the attempts as retries and can flag the test
 * as flaky. Screen recording only runs on retry attempts: the first attempt of a healthy
 * test passes, and recording it would be paid for and discarded on every test.
 */
public class RetryRule(private val count: Int) : TestRule {

    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            val testName = description.displayName
            val databaseOperations = DatabaseOperations()
            var caughtThrowable: Throwable? = null

            for (attempt in 1..count) {
                val recordVideo = attempt > 1
                val videoFilePath = "${Environment.getExternalStorageDirectory().absolutePath}/$testName.mp4"
                var recordingThread: Thread? = null
                val startMillis = System.currentTimeMillis()
                try {
                    System.err.println("$testName: run #$attempt started.")
                    device.executeShellCommand("logcat -c")
                    if (recordVideo) {
                        recordingThread = startVideoRecording(videoFilePath)
                    }
                    base.evaluate()
                    recordingThread?.let { stopVideoRecording(videoFilePath, it) }
                    return
                } catch (t: Throwable) {
                    System.err.println("$testName: run #$attempt failed.")
                    caughtThrowable = t
                    databaseOperations.clearDatabases()
                    recordingThread?.let { stopVideoRecording(videoFilePath, it) }
                    device.allureLogcat(name = "logcat_$attempt")
                    device.allureScreenshot(name = "screenshot_$attempt")
                    device.allureWindowHierarchy(name = "hierarchy_$attempt")
                    recordingThread?.let {
                        device.allureScreenrecord(name = "record_$attempt", file = File(videoFilePath))
                    }
                    if (attempt < count) {
                        writeFailedAttemptResult(t, startMillis)
                    }
                } finally {
                    if (recordingThread != null) {
                        device.executeShellCommand("rm $videoFilePath")
                    }
                }
            }

            throw caughtThrowable ?: IllegalStateException("$testName failed without a captured error")
        }
    }

    /**
     * Writes the failed attempt as a separate, already-finished Allure result. The current
     * (real) result keeps running; its accumulated steps and attachments are moved to the
     * attempt result so each result describes exactly one attempt.
     */
    private fun writeFailedAttemptResult(error: Throwable, startMillis: Long) {
        val lifecycle = Allure.lifecycle
        val attemptResult = TestResult(uuid = UUID.randomUUID().toString())
        var populated = false
        lifecycle.updateTestCase { current ->
            attemptResult.historyId = current.historyId
            attemptResult.testCaseId = current.testCaseId
            attemptResult.fullName = current.fullName
            attemptResult.name = current.name
            attemptResult.description = current.description
            attemptResult.labels.addAll(current.labels)
            attemptResult.links.addAll(current.links)
            attemptResult.parameters.addAll(current.parameters)
            attemptResult.steps.addAll(current.steps)
            attemptResult.attachments.addAll(current.attachments)
            current.steps.clear()
            current.attachments.clear()
            populated = true
        }
        if (!populated) {
            return
        }
        with(attemptResult) {
            status = ResultsUtils.getStatus(error)
            statusDetails = ResultsUtils.getStatusDetails(error)
            start = startMillis
            stop = System.currentTimeMillis()
        }
        // scheduleTestCase stores the result by reference and does not touch the thread context,
        // so the running test stays current. It resets the stage, hence FINISHED is set after.
        lifecycle.scheduleTestCase(attemptResult)
        attemptResult.stage = Stage.FINISHED
        lifecycle.writeTestCase(attemptResult.uuid)
    }

    private fun startVideoRecording(remoteVideoPath: String): Thread {
        return Thread {
            device.executeShellCommand(
                "screenrecord --bit-rate 8000000 --time-limit 180 $remoteVideoPath",
            )
        }.also { it.start() }
    }

    private fun stopVideoRecording(remoteVideoPath: String, thread: Thread) {
        device.executeShellCommand("pkill -INT screenrecord")
        thread.join(5000)
        waitUntil { !isScreenrecordRunning() }
        waitUntil { isFileStable(remoteVideoPath) }
    }

    private fun isScreenrecordRunning(): Boolean {
        val ps = device.executeShellCommand("ps | grep screenrecord || true")
        return ps.contains("screenrecord")
    }

    private fun isFileStable(path: String): Boolean {
        val output = device.executeShellCommand("ls -l $path")
        val size = output.trim().split(Regex("\\s+")).getOrNull(4)?.toLongOrNull() ?: 0L
        Thread.sleep(200)
        val output2 = device.executeShellCommand("ls -l $path")
        val size2 = output2.trim().split(Regex("\\s+")).getOrNull(4)?.toLongOrNull() ?: 0L
        return size > 0 && size == size2
    }

    @Suppress("TooGenericExceptionThrown")
    private fun waitUntil(timeoutMs: Long = 5000, condition: () -> Boolean) {
        val start = System.currentTimeMillis()
        while (!condition()) {
            if (System.currentTimeMillis() - start > timeoutMs) {
                throw RuntimeException("Timeout waiting for video recording to finish")
            }
            Thread.sleep(200)
        }
    }
}

private class DatabaseOperations {

    fun clearDatabases() {
        getAllDatabaseFiles()
            .filterNot(::shouldIgnoreFile)
            .forEach(::clearDatabase)
    }

    private fun shouldIgnoreFile(file: File): Boolean {
        val ignoredSuffixes = arrayOf("-journal", "-shm", "-uid", "-wal")
        return ignoredSuffixes.any { file.path.endsWith(it) }
    }

    private fun clearDatabase(dbFile: File) {
        openDatabase(dbFile).use { database ->
            getTableNames(database)
                .filterNot { it == "room_master_table" }
                .forEach { deleteTableContent(database, it) }
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
