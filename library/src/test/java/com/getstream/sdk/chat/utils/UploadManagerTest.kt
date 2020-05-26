package com.getstream.sdk.chat.utils

import com.getstream.sdk.chat.createAttachmentMetaDataWithFile
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.randomLong
import com.getstream.sdk.chat.randomString
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.ProgressCallback
import org.amshove.kluent.Verify
import org.amshove.kluent.When
import org.amshove.kluent.`Verify no further interactions`
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.any
import org.amshove.kluent.called
import org.amshove.kluent.calling
import org.amshove.kluent.on
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito

internal class UploadManagerTest {
	private val chatClient: ChatClient = mock()
	private lateinit var uploadManager: UploadManager
	private val progressCallback: ProgressCallback = spy()

	@BeforeEach
	fun setup() {
		Mockito.reset(progressCallback, chatClient)
		uploadManager = UploadManager(chatClient)
	}

	@Test
	fun `Initial state shouldn't be uploading any file`() {
		uploadManager.isUploadingFile.`should be false`()
	}

	@ParameterizedTest
	@MethodSource("com.getstream.sdk.chat.utils.UploadManagerTest#createAttachmentMetaDataInput")
	fun `After start an upload should be a file uploading`(attachmentMetaData: AttachmentMetaData) {
		uploadManager.uploadFile(randomString(), randomString(), attachmentMetaData, progressCallback)

		uploadManager.isUploadingFile.`should be true`()
	}

	@ParameterizedTest
	@MethodSource("com.getstream.sdk.chat.utils.UploadManagerTest#createAttachmentMetaDataInput")
	fun `Should notify about an error`(attachmentMetaData: AttachmentMetaData) {
		val error: ChatError = mock()
		val channelId = randomString()
		val channelType = randomString()
		When calling chatClient.sendImage(
				eq(channelType),
				eq(channelId),
				eq(attachmentMetaData.file),
				any()) doAnswer {
			(it.arguments[3] as ProgressCallback).onError(error)
		}
		When calling chatClient.sendFile(
				eq(channelType),
				eq(channelId),
				eq(attachmentMetaData.file),
				any()) doAnswer {
			(it.arguments[3] as ProgressCallback).onError(error)
		}
		uploadManager.uploadFile(channelType, channelId, attachmentMetaData, progressCallback)

		uploadManager.isUploadingFile.`should be false`()
		Verify on progressCallback that progressCallback.onError(eq(error)) was called
		`Verify no further interactions` on progressCallback
	}

	@ParameterizedTest
	@MethodSource("com.getstream.sdk.chat.utils.UploadManagerTest#createAttachmentMetaDataInput")
	fun `Should notify about some progress`(attachmentMetaData: AttachmentMetaData) {
		val progress = randomLong()
		val channelId = randomString()
		val channelType = randomString()
		When calling chatClient.sendImage(
				eq(channelType),
				eq(channelId),
				eq(attachmentMetaData.file),
				any()) doAnswer {
			(it.arguments[3] as ProgressCallback).onProgress(progress)
		}
		When calling chatClient.sendFile(
				eq(channelType),
				eq(channelId),
				eq(attachmentMetaData.file),
				any()) doAnswer {
			(it.arguments[3] as ProgressCallback).onProgress(progress)
		}
		uploadManager.uploadFile(channelType, channelId, attachmentMetaData, progressCallback)

		uploadManager.isUploadingFile.`should be true`()
		Verify on progressCallback that progressCallback.onProgress(eq(progress)) was called
		`Verify no further interactions` on progressCallback
	}

	@ParameterizedTest
	@MethodSource("com.getstream.sdk.chat.utils.UploadManagerTest#createAttachmentMetaDataInput")
	fun `Should notify about on success`(attachmentMetaData: AttachmentMetaData) {
		val path = randomString()
		val channelId = randomString()
		val channelType = randomString()
		When calling chatClient.sendImage(
				eq(channelType),
				eq(channelId),
				eq(attachmentMetaData.file),
				any()) doAnswer {
			(it.arguments[3] as ProgressCallback).onSuccess(path)
		}
		When calling chatClient.sendFile(
				eq(channelType),
				eq(channelId),
				eq(attachmentMetaData.file),
				any()) doAnswer {
			(it.arguments[3] as ProgressCallback).onSuccess(path)
		}
		uploadManager.uploadFile(channelType, channelId, attachmentMetaData, progressCallback)

		uploadManager.isUploadingFile.`should be false`()
		Verify on progressCallback that progressCallback.onSuccess(eq(path)) was called
		`Verify no further interactions` on progressCallback
	}

	companion object {
		@JvmStatic
		fun createAttachmentMetaDataInput() = listOf(
				Arguments.of(createAttachmentMetaDataWithFile()),
				Arguments.of(createAttachmentMetaDataWithFile(forceMimeType = "image/jpeg"))
		)
	}
}