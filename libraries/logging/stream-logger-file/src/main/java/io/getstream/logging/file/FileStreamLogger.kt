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

package io.getstream.logging.file

import io.getstream.logging.Priority
import io.getstream.logging.StreamLogger
import io.getstream.logging.helper.stringify
import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.Writer
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val DEFAULT_SIZE: Int = 12 * 1024 * 1024

private const val SHAREABLE_FILE: String = "stream_log_%s.txt"
private const val INTERNAL_FILE_0: String = "internal_0.txt"
private const val INTERNAL_FILE_1: String = "internal_1.txt"

/**
 * The [StreamLogger] implementation with log file persistence.
 */
public class FileStreamLogger(
    private val config: Config,
) : StreamLogger {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    private val timeFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss''SSS", Locale.ENGLISH)
    private val dateFormat: DateFormat = SimpleDateFormat("yyMMddHHmm_ss", Locale.ENGLISH)

    private val internalFile0: File = File(config.filesDir, INTERNAL_FILE_0)
    private val internalFile1: File = File(config.filesDir, INTERNAL_FILE_1)
    private val internalFiles: Array<File> = arrayOf(internalFile0, internalFile1)

    private var currentFile: File? = null
    private var currentWriter: Writer? = null

    override fun log(
        priority: Priority,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        val thread = Thread.currentThread()
        executor.execute {
            initIfNeeded()
            swapFiles()
            currentWriter?.runCatching {
                val formattedDateTime = timeFormat.format(Date())
                val formattedThread = "%20s".format(thread.stringify())
                val formattedPriority = priority.stringify()
                val formatterPrefix = "$formattedDateTime $formattedPriority/$formattedThread [$tag]: "

                write(formatterPrefix)
                write(message)
                appendLine()
                if (write(throwable)) {
                    appendLine()
                }
                flush()
            }
        }
    }

    public fun share(callback: (File) -> Unit): Unit = executor.execute {
        generateShareableFile().getOrNull()?.also(callback)
    }

    public fun clear(): Unit = executor.execute {
        internalFile0.writeText("")
        internalFile1.writeText("")
    }

    private fun initIfNeeded() {
        if (currentFile == null) {
            val internalFile: File = when {
                !internalFile0.exists() || !internalFile1.exists() -> internalFile0
                internalFile0.lastModified() > internalFile1.lastModified() -> internalFile0
                else -> internalFile1
            }
            currentFile = internalFile
            currentWriter = internalFile.fileWriter()
        }
    }

    private fun swapFiles(): Result<Unit> = runCatching {
        val curLen = currentFile?.length() ?: 0
        if (curLen >= config.maxLogSize / 2) {
            currentFile = if (currentFile === internalFile0) internalFile1 else internalFile0
            currentFile?.writeText(text = "")
            currentWriter?.closeSilently()
            currentWriter = currentFile?.fileWriter()
        }
    }

    private fun generateShareableFile(): Result<File> = runCatching {
        val filename = SHAREABLE_FILE.format(dateFormat.format(Date()))
        File(config.externalFilesDir, filename).apply {
            bufferedWriter().use { writer ->
                writer.write(config.buildHeader())
                val files = internalFiles.asSequence().filter { it.exists() }.sortedBy { it.lastModified() }.toList()
                files.forEach { file ->
                    file.bufferedReader().use { reader ->
                        reader.copyTo(writer)
                    }
                }
            }
        }
    }

    private fun Config.buildHeader(): String = """
            |======================================================================
            |Logs date time: ${timeFormat.format(Date())}
            |Version code: ${app.versionCode}
            |Version name: ${app.versionName}
            |Android API level: ${device.androidApiLevel}
            |Device: ${device.model}
            |======================================================================
            |""".trimMargin()

    public data class Config(
        val maxLogSize: Int = DEFAULT_SIZE,
        val filesDir: File,
        val externalFilesDir: File?,
        val app: App,
        val device: Device,
    ) {
        public data class App(
            val versionCode: Long,
            val versionName: String,
        )
        public data class Device(
            val model: String,
            val androidApiLevel: Int,
        )
    }
}

private fun File.fileWriter(): Writer = BufferedWriter(OutputStreamWriter(FileOutputStream(this, true), Charsets.UTF_8))
private fun Closeable.closeSilently(): Result<Unit> = runCatching {
    close()
}

private fun Writer.write(throwable: Throwable?): Boolean {
    throwable?.printStackTrace(PrintWriter(this)) ?: return false
    return true
}
