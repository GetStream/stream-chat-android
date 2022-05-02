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

package io.getstream.logging.android

import android.os.Build
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import io.getstream.logging.Priority
import io.getstream.logging.StreamLogger
import io.getstream.logging.helper.stringify

private const val MAX_TAG_LEN = 23

/**
 * The [StreamLogger] implementation for android projects.
 */
public class AndroidStreamLogger : StreamLogger {

    override fun log(priority: Priority, tag: String, message: String, throwable: Throwable?) {

        val androidPriority = priority.toAndroidPriority()
        val androidTag = tag.takeIf { it.length > MAX_TAG_LEN && !isNougatOrHigher() }
            ?.substring(0, MAX_TAG_LEN)
            ?: tag

        val thread = Thread.currentThread().run { "$name:$id" }
        val composed = "($thread) $message"
        val finalMessage = throwable?.let {
            "$composed\n${it.stringify()}"
        } ?: composed

        Log.println(androidPriority, androidTag, finalMessage)
    }

    private fun Priority.toAndroidPriority(): Int {
        return when (this) {
            Priority.VERBOSE -> Log.VERBOSE
            Priority.DEBUG -> Log.DEBUG
            Priority.INFO -> Log.INFO
            Priority.WARN -> Log.WARN
            Priority.ERROR -> Log.ERROR
            Priority.ASSERT -> Log.ASSERT
            else -> Log.ERROR
        }
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
    private fun isNougatOrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}
