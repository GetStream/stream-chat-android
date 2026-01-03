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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomFile
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUploadedFile
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Tests for the files functionalities of the [ChatClient].
 */
internal class ChatClientChannelFileUploaderTests : BaseChatClientTest() {

    @Test
    fun sendFileSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val file = randomFile()
        val callback = mock<ProgressCallback>()
        val uploadedFile = randomUploadedFile()
        whenever(api.sendFile(any(), any(), any(), any()))
            .thenReturn(RetroSuccess(uploadedFile).toRetrofitCall())
        // when
        val result = chatClient.sendFile(channelType, channelId, file, callback).await()
        // then
        verifySuccess(result, uploadedFile)
    }

    @Test
    fun sendFileError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val file = randomFile()
        val callback = mock<ProgressCallback>()
        val errorCode = positiveRandomInt()
        whenever(api.sendFile(any(), any(), any(), any()))
            .thenReturn(RetroError<UploadedFile>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.sendFile(channelType, channelId, file, callback).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun sendImageSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val file = randomFile()
        val callback = mock<ProgressCallback>()
        val uploadedFile = randomUploadedFile()
        whenever(api.sendImage(any(), any(), any(), any()))
            .thenReturn(RetroSuccess(uploadedFile).toRetrofitCall())
        // when
        val result = chatClient.sendImage(channelType, channelId, file, callback).await()
        // then
        verifySuccess(result, uploadedFile)
    }

    @Test
    fun sendImageError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val file = randomFile()
        val callback = mock<ProgressCallback>()
        val errorCode = positiveRandomInt()
        whenever(api.sendImage(any(), any(), any(), any()))
            .thenReturn(RetroError<UploadedFile>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.sendImage(channelType, channelId, file, callback).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun deleteFileSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val url = randomString()
        whenever(api.deleteFile(any(), any(), any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.deleteFile(channelType, channelId, url).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun deleteFileError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val url = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.deleteFile(any(), any(), any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.deleteFile(channelType, channelId, url).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun deleteImageSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val url = randomString()
        whenever(api.deleteImage(any(), any(), any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.deleteImage(channelType, channelId, url).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun deleteImageError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val url = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.deleteImage(any(), any(), any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.deleteImage(channelType, channelId, url).await()
        // then
        verifyNetworkError(result, errorCode)
    }
}
