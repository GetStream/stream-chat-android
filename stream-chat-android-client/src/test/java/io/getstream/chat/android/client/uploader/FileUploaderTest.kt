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

package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.randomFile
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.io.File

/**
 * Verifies that the default bodies of the [FileUploadContext]-aware overloads delegate to the legacy
 * overloads, so existing [FileUploader] implementations keep working unchanged.
 */
internal class FileUploaderTest {

    private val channelType = randomString()
    private val channelId = randomString()
    private val userId = randomString()
    private val file = randomFile()
    private val uploadContext = FileUploadContext(
        channelType = channelType,
        channelId = channelId,
        userId = userId,
        messageId = randomString(),
    )

    @Test
    fun `sendFile with upload context and callback delegates to the legacy callback overload`() {
        val uploader = LegacyFileUploader()
        val callback = mock<ProgressCallback>()

        val result = uploader.sendFile(uploadContext, file, callback)

        (result as Result.Success).value.file shouldBeEqualTo "sendFile-with-callback"
        uploader.recordedArgs shouldBeEqualTo listOf(channelType, channelId, userId, file, callback)
    }

    @Test
    fun `sendFile with upload context and no callback delegates to the legacy overload`() {
        val uploader = LegacyFileUploader()

        val result = uploader.sendFile(uploadContext, file)

        (result as Result.Success).value.file shouldBeEqualTo "sendFile-without-callback"
        uploader.recordedArgs shouldBeEqualTo listOf(channelType, channelId, userId, file)
    }

    @Test
    fun `sendImage with upload context and callback delegates to the legacy callback overload`() {
        val uploader = LegacyFileUploader()
        val callback = mock<ProgressCallback>()

        val result = uploader.sendImage(uploadContext, file, callback)

        (result as Result.Success).value.file shouldBeEqualTo "sendImage-with-callback"
        uploader.recordedArgs shouldBeEqualTo listOf(channelType, channelId, userId, file, callback)
    }

    @Test
    fun `sendImage with upload context and no callback delegates to the legacy overload`() {
        val uploader = LegacyFileUploader()

        val result = uploader.sendImage(uploadContext, file)

        (result as Result.Success).value.file shouldBeEqualTo "sendImage-without-callback"
        uploader.recordedArgs shouldBeEqualTo listOf(channelType, channelId, userId, file)
    }

    /**
     * A [FileUploader] implementing only the legacy overloads, recording which one is invoked and with
     * which arguments.
     */
    private class LegacyFileUploader : FileUploader {

        var recordedArgs: List<Any?>? = null

        override fun sendFile(
            channelType: String,
            channelId: String,
            userId: String,
            file: File,
            callback: ProgressCallback,
        ): Result<UploadedFile> {
            recordedArgs = listOf(channelType, channelId, userId, file, callback)
            return Result.Success(UploadedFile(file = "sendFile-with-callback"))
        }

        override fun sendFile(
            channelType: String,
            channelId: String,
            userId: String,
            file: File,
        ): Result<UploadedFile> {
            recordedArgs = listOf(channelType, channelId, userId, file)
            return Result.Success(UploadedFile(file = "sendFile-without-callback"))
        }

        override fun sendImage(
            channelType: String,
            channelId: String,
            userId: String,
            file: File,
            callback: ProgressCallback,
        ): Result<UploadedFile> {
            recordedArgs = listOf(channelType, channelId, userId, file, callback)
            return Result.Success(UploadedFile(file = "sendImage-with-callback"))
        }

        override fun sendImage(
            channelType: String,
            channelId: String,
            userId: String,
            file: File,
        ): Result<UploadedFile> {
            recordedArgs = listOf(channelType, channelId, userId, file)
            return Result.Success(UploadedFile(file = "sendImage-without-callback"))
        }

        override fun deleteFile(
            channelType: String,
            channelId: String,
            userId: String,
            url: String,
        ): Result<Unit> = Result.Success(Unit)

        override fun deleteImage(
            channelType: String,
            channelId: String,
            userId: String,
            url: String,
        ): Result<Unit> = Result.Success(Unit)
    }
}
