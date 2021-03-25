package io.getstream.chat.android.client.uploader

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.api.RetrofitCdnApi
import io.getstream.chat.android.client.api.models.CompletableResponse
import io.getstream.chat.android.client.api.models.UploadFileResponse
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
        override fun onSuccess(file: String) = Unit

        override fun onError(error: ChatError) = Unit

        override fun onProgress(progress: Long) = Unit
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
        whenever(retrofitCdnApi.sendFile(any(), any(), any(), any())).thenReturn(
            RetroSuccess(UploadFileResponse("file")).toRetrofitCall()
        )

        streamFileUploader.sendFile(channelType, channelId, userId, connectionId, File(""))

        verify(retrofitCdnApi).sendFile(
            eq(channelType),
            eq(channelId),
            file = any(),
            eq(connectionId)
        )
    }

    @Test
    fun `Should return file when successfully sent file without progress callback`() {
        val file = "file"
        whenever(retrofitCdnApi.sendFile(any(), any(), any(), any())).thenReturn(
            RetroSuccess(UploadFileResponse(file)).toRetrofitCall()
        )

        val result =
            streamFileUploader.sendFile(channelType, channelId, userId, connectionId, File(""))

        result shouldBeEqualTo file
    }

    @Test
    fun `Should return null when sending file without progress callback failed`() {
        whenever(retrofitCdnApi.sendFile(any(), any(), any(), any())).thenReturn(
            RetroError<UploadFileResponse>(500).toRetrofitCall()
        )

        val result =
            streamFileUploader.sendFile(channelType, channelId, userId, connectionId, File(""))

        result.shouldBeNull()
    }

    @Test
    fun `Should send file to api when sending file with progress callback`() {
        whenever(retrofitCdnApi.sendFile(any(), any(), any(), any())).thenReturn(
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
            eq(connectionId)
        )
    }

    @Test
    fun `Should send image to api when sending image without progress callback`() {
        whenever(retrofitCdnApi.sendImage(any(), any(), any(), any())).thenReturn(
            RetroSuccess(UploadFileResponse("file")).toRetrofitCall()
        )

        streamFileUploader.sendImage(channelType, channelId, userId, connectionId, File(""))

        verify(retrofitCdnApi).sendImage(
            eq(channelType),
            eq(channelId),
            file = any(),
            eq(connectionId)
        )
    }

    @Test
    fun `Should return file when successfully sent image without progress callback`() {
        val file = "file"
        whenever(retrofitCdnApi.sendImage(any(), any(), any(), any())).thenReturn(
            RetroSuccess(UploadFileResponse(file)).toRetrofitCall()
        )

        val result =
            streamFileUploader.sendImage(channelType, channelId, userId, connectionId, File(""))

        result shouldBeEqualTo file
    }

    @Test
    fun `Should return null when sending image without progress callback failed`() {
        whenever(retrofitCdnApi.sendImage(any(), any(), any(), any())).thenReturn(
            RetroError<UploadFileResponse>(500).toRetrofitCall()
        )

        val result =
            streamFileUploader.sendImage(channelType, channelId, userId, connectionId, File(""))

        result.shouldBeNull()
    }

    @Test
    fun `Should send image to api when sending image with progress callback`() {
        whenever(retrofitCdnApi.sendImage(any(), any(), any(), any())).thenReturn(
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
            eq(connectionId)
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
