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

package io.getstream.chat.android.compose.sample.ui.profile

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import io.getstream.chat.android.compose.sample.R
import java.io.File
import java.util.UUID

@Suppress("MagicNumber")
internal fun Context.formatTime(seconds: Long): String {
    val minutes = (seconds / 60).toInt()
    val remainingSeconds = (seconds % 60).toInt()
    return buildString {
        if (minutes > 0) {
            append(resources.getQuantityString(R.plurals.time_minutes, minutes, minutes))
        }
        if (remainingSeconds > 0) {
            if (isNotEmpty()) append(" ")
            append(resources.getQuantityString(R.plurals.time_seconds, remainingSeconds, remainingSeconds))
        }
    }
}

internal fun Uri.toCacheFile(context: Context): File {
    val cacheFile = File(context.cacheDir, UUID.randomUUID().toString())
    context.contentResolver.openInputStream(this).use { inputStream ->
        cacheFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
    }
    return cacheFile
}

internal fun Context.generateCameraImageFile(): File = cacheDir.resolve(relative = "camera_${UUID.randomUUID()}.jpg")

internal fun File.getUri(context: Context): Uri = FileProvider.getUriForFile(
    context,
    "${context.packageName}.streamfileprovider",
    this,
)
