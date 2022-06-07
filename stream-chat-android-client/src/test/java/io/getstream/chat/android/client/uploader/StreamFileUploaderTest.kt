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

package io.getstream.chat.android.client.uploader

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.api.RetrofitCdnApi
import io.getstream.chat.android.client.api.models.CompletableResponse
import io.getstream.chat.android.client.api.models.UploadFileResponse
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
internal class StreamFileUploaderTest {

    private val channelType = "channelType"
    private val channelId = "channelId"
    private val userId = "userId"
    private val connectionId = "connectionId"
    private val url = "url"

    private val progressCallback = object : ProgressCallback {
        override fun onSuccess(url: String?) = Unit

        override fun onError(error: ChatError) = Unit

        override fun onProgress(bytesUploaded: Long, totalBytes: Long) = Unit
    }

    private lateinit var retrofitCdnApi: RetrofitCdnApi
    private lateinit var streamFileUploader: StreamFileUploader

    @Before
    fun before() {
        retrofitCdnApi = mock()
        shadowOf(MimeTypeMap.getSingleton())
        streamFileUploader = StreamFileUploader(retrofitCdnApi)
    }

    @Test
    fun `Should send file to api when sending file without progress callback`() {
        whenever(retrofitCdnApi.sendFile(any(), any(), any(), any(), anyOrNull())).thenReturn(
            RetroSuccess(UploadFileResponse("file")).toRetrofitCall()
        )

        streamFileUploader.sendFile(channelType, channelId, userId, connectionId, File(""))

        verify(retrofitCdnApi).sendFile(
            eq(channelType),
            eq(channelId),
            file = any(),
            eq(connectionId),
            eq(null),
        )
    }

    @Test
    fun `Should return result containing file when successfully sent file without progress callback`() {
        val file = "file"
        whenever(retrofitCdnApi.sendFile(any(), any(), any(), any(), anyOrNull())).thenReturn(
            RetroSuccess(UploadFileResponse(file)).toRetrofitCall()
        )

        val result = streamFileUploader.sendFile(channelType, channelId, userId, connectionId, File(""))

        result.data() shouldBeEqualTo file
    }

    @Test
    fun `Should return result containing error when sending file without progress callback failed`() {
        whenever(retrofitCdnApi.sendFile(any(), any(), any(), any(), anyOrNull())).thenReturn(
            RetroError<UploadFileResponse>(500).toRetrofitCall()
        )

        val result = streamFileUploader.sendFile(channelType, channelId, userId, connectionId, File(""))

        result.isError.shouldBeTrue()
    }

    @Test
    fun `Should send file to api when sending file with progress callback`() {
        whenever(retrofitCdnApi.sendFile(any(), any(), any(), any(), anyOrNull())).thenReturn(
            RetroSuccess(UploadFileResponse("file")).toRetrofitCall()
        )

        streamFileUploader.sendFile(
            channelType,
            channelId,
            userId,
            connectionId,
            File(""),
            progressCallback
        )

        verify(retrofitCdnApi).sendFile(
            eq(channelType),
            eq(channelId),
            file = any(),
            eq(connectionId),
            eq(progressCallback),
        )
    }

    @Test
    fun `Should send image to api when sending image without progress callback`() {
        whenever(retrofitCdnApi.sendImage(any(), any(), any(), any(), anyOrNull())).thenReturn(
            RetroSuccess(UploadFileResponse("file")).toRetrofitCall()
        )

        streamFileUploader.sendImage(channelType, channelId, userId, connectionId, File(""))

        verify(retrofitCdnApi).sendImage(
            eq(channelType),
            eq(channelId),
            file = any(),
            eq(connectionId),
            eq(null),
        )
    }

    @Test
    fun `Should return result containing file when successfully sent image without progress callback`() {
        val file = "file"
        whenever(retrofitCdnApi.sendImage(any(), any(), any(), any(), anyOrNull())).thenReturn(
            RetroSuccess(UploadFileResponse(file)).toRetrofitCall()
        )

        val result = streamFileUploader.sendImage(channelType, channelId, userId, connectionId, File(""))

        result.data() shouldBeEqualTo file
    }

    @Test
    fun `Should return containing error when sending image without progress callback failed`() {
        whenever(retrofitCdnApi.sendImage(any(), any(), any(), any(), anyOrNull())).thenReturn(
            RetroError<UploadFileResponse>(500).toRetrofitCall()
        )

        val result = streamFileUploader.sendImage(channelType, channelId, userId, connectionId, File(""))

        result.isError.shouldBeTrue()
    }

    @Test
    fun `Should send image to api when sending image with progress callback`() {
        whenever(retrofitCdnApi.sendImage(any(), any(), any(), any(), anyOrNull())).thenReturn(
            RetroSuccess(UploadFileResponse("file")).toRetrofitCall()
        )

        streamFileUploader.sendImage(
            channelType,
            channelId,
            userId,
            connectionId,
            File(""),
            progressCallback
        )

        verify(retrofitCdnApi).sendImage(
            eq(channelType),
            eq(channelId),
            file = any(),
            eq(connectionId),
            eq(progressCallback),
        )
    }

    @Test
    fun `Should call api delete file when deleting file`() {
        whenever(retrofitCdnApi.deleteFile(any(), any(), any(), any())).thenReturn(
            RetroSuccess(CompletableResponse()).toRetrofitCall()
        )

        streamFileUploader.deleteFile(channelType, channelId, userId, connectionId, url)

        verify(retrofitCdnApi).deleteFile(
            eq(channelType),
            eq(channelId),
            eq(connectionId),
            eq(url),
        )
    }

    @Test
    fun `Should call api delete image when deleting image`() {
        whenever(retrofitCdnApi.deleteImage(any(), any(), any(), any())).thenReturn(
            RetroSuccess(CompletableResponse()).toRetrofitCall()
        )

        streamFileUploader.deleteImage(channelType, channelId, userId, connectionId, url)

        verify(retrofitCdnApi).deleteImage(
            eq(channelType),
            eq(channelId),
            eq(connectionId),
            eq(url),
        )
    }
}
