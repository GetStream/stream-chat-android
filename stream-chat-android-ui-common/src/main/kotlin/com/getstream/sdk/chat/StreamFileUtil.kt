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

package com.getstream.sdk.chat

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.io.File
import java.io.IOException

private const val DEFAULT_BITMAP_QUALITY = 90
@InternalStreamChatApi
public object StreamFileUtil {

    private fun getFileProviderAuthority(context: Context): String {
        val compName = ComponentName(context, StreamFileProvider::class.java.name)
        val providerInfo = context.packageManager.getProviderInfo(compName, 0)
        return providerInfo.authority
    }

    public fun getUriForFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, getFileProviderAuthority(context), file)

    public fun writeImageToSharableFile(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir,
                "share_image_${System.currentTimeMillis()}.png"
            )
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, DEFAULT_BITMAP_QUALITY, out)
                out.flush()
            }
            getUriForFile(context, file)
        } catch (_: IOException) {
            null
        }
    }
}
