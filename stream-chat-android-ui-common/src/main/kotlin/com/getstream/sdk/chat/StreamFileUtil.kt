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
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.io.File
import java.io.FileOutputStream
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

    /**
     * Hashes the links of given attachments and then tries to create a new file
     * under that hash. If the file already exists checks that the full file
     * has been written and shares it if it has, in other cases downloads the file
     * and writes it.
     *
     * @param context The Android [Context] used for path resolving and [Uri] fetching.
     * @param attachment the attachment to be downloaded.
     *
     * @return The [Uri] that represents the path to the downloaded file.
     */
    public suspend fun writeFileToShareableFile(context: Context, attachment: Attachment): Uri? {
        val result = runCatching {
            val attachmentName = (attachment.url ?: attachment.assetUrl)?.hashCode()
            val fileName = attachmentName.toString() + attachment.name

            val file = File(context.cacheDir, fileName)

            // When File.createNewFile returns false it means that the file already exists.
            // We then check the hash name equality to confirm it's the same file and check file size
            // equality to make sure we've completed the download successfully.
            if (!file.createNewFile() &&
                // TODO reported size is not the same as downloaded size
                // TODO check why
                attachmentName != null &&
                // once this is functional
                file.length() == attachment.fileSize.toLong()
            ) {
                getUriForFile(context, file)
            } else {
                val fileUrl = attachment.assetUrl ?: attachment.url ?: return null
                val response = ChatClient.instance().downloadFile(fileUrl).await()

                if (response.isSuccess) {
                    // write the response to a file
                    val byteArray = response.data().byteStream().readBytes()
                    val fileOutputStream = FileOutputStream(file)
                    fileOutputStream.write(byteArray)
                    fileOutputStream.close()

                    getUriForFile(context, file)
                } else {
                    null
                }
            }
        }

        return result.getOrNull()
    }
}
