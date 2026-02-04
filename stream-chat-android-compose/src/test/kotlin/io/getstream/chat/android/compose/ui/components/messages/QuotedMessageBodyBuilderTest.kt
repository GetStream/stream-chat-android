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

package io.getstream.chat.android.compose.ui.components.messages

import android.content.res.Resources
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.attachments.files.FileIconData
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.ui.common.helper.DurationFormatter
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing.Companion.defaultStreamCdnImageResizing
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class QuotedMessageBodyBuilderTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("attachmentTestCases")
    fun testBuild(
        name: String,
        message: Message,
        currentUser: User?,
        autoTranslationEnabled: Boolean,
        expected: QuotedMessageBody,
    ) {
        val resources = mockResources()
        val builder = QuotedMessageBodyBuilder(
            resources = resources,
            autoTranslationEnabled = autoTranslationEnabled,
            durationFormatter = mockDurationFormatter(),
            streamCdnImageResizing = defaultStreamCdnImageResizing(),
        )

        builder.build(message, currentUser) `should be equal to` expected
    }

    @Suppress("LargeClass")
    companion object {
        private const val MOCK_MESSAGE_DELETED = "Message deleted"
        private const val MOCK_LOCATION_STATIC = "Static Location"
        private const val MOCK_LOCATION_LIVE = "Live Location"
        private const val MOCK_GIPHY_TAG = "GIF"
        private const val MOCK_AUDIO_RECORDING = "Voice message 00:05"

        @JvmStatic
        @Suppress("LongMethod")
        fun attachmentTestCases() = listOf(
            Arguments.of(
                "Empty attachments with text",
                Message(text = "Hello world"),
                null,
                false,
                QuotedMessageBody(text = "Hello world"),
            ),
            Arguments.of(
                "Empty attachments without text",
                Message(text = ""),
                null,
                false,
                QuotedMessageBody(text = ""),
            ),
            Arguments.of(
                "Deleted message",
                Message(text = "Some text", deletedAt = Date()),
                null,
                false,
                QuotedMessageBody(text = MOCK_MESSAGE_DELETED),
            ),
            Arguments.of(
                "Message with poll",
                Message(text = "What's your favorite?", poll = randomPoll(name = "Test Poll")),
                null,
                false,
                QuotedMessageBody(
                    text = "Test Poll",
                    iconId = R.drawable.stream_compose_ic_chart,
                ),
            ),
            Arguments.of(
                "Message with static location",
                Message(text = "I'm here", sharedLocation = randomLocation(endAt = null)),
                null,
                false,
                QuotedMessageBody(
                    text = MOCK_LOCATION_STATIC,
                    iconId = R.drawable.stream_compose_ic_map_pin,
                ),
            ),
            Arguments.of(
                "Message with live location",
                Message(text = "I'm here", sharedLocation = randomLocation(endAt = Date())),
                null,
                false,
                QuotedMessageBody(
                    text = MOCK_LOCATION_LIVE,
                    iconId = R.drawable.stream_compose_ic_map_pin,
                ),
            ),
            Arguments.of(
                "Link attachment with text",
                Message(
                    text = "Check this out",
                    attachments = listOf(
                        Attachment(
                            type = null,
                            ogUrl = "https://example.com",
                            titleLink = "Example Site",
                            imageUrl = "https://example.com/image.jpg",
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Check this out",
                    iconId = R.drawable.stream_compose_ic_link,
                    imagePreviewData = "https://example.com/image.jpg",
                ),
            ),
            Arguments.of(
                "Link attachment without text uses titleLink",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(
                            type = null,
                            ogUrl = "https://example.com",
                            titleLink = "Example Site",
                            imageUrl = "https://example.com/image.jpg",
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Example Site",
                    iconId = R.drawable.stream_compose_ic_link,
                    imagePreviewData = "https://example.com/image.jpg",
                ),
            ),
            Arguments.of(
                "Link attachment without text uses ogUrl",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(
                            type = null,
                            ogUrl = "https://example.com",
                            titleLink = null,
                            imageUrl = null,
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "https://example.com",
                    iconId = R.drawable.stream_compose_ic_link,
                ),
            ),

            Arguments.of(
                "Giphy with text",
                Message(
                    text = "Feeling great!",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.GIPHY,
                            name = "Happy Dance",
                            imageUrl = "https://giphy.com/image.gif",
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Feeling great!",
                    imagePreviewData = "https://giphy.com/image.gif",
                ),
            ),
            Arguments.of(
                "Giphy without text uses name",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.GIPHY,
                            name = "Happy Dance",
                            text = null,
                            title = null,
                            imageUrl = "https://giphy.com/image.gif",
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Happy Dance",
                    imagePreviewData = "https://giphy.com/image.gif",
                ),
            ),
            Arguments.of(
                "Giphy without text uses fallback",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.GIPHY,
                            name = null,
                            text = null,
                            title = null,
                            imageUrl = "https://giphy.com/image.gif",
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = MOCK_GIPHY_TAG,
                    imagePreviewData = "https://giphy.com/image.gif",
                ),
            ),

            Arguments.of(
                "Single image with text",
                Message(
                    text = "Beautiful photo",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.IMAGE,
                            imageUrl = "https://example.com/image.jpg",
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Beautiful photo",
                    iconId = R.drawable.stream_compose_ic_camera,
                    imagePreviewData = "https://example.com/image.jpg",
                ),
            ),
            Arguments.of(
                "Single image without text",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.IMAGE,
                            imageUrl = "https://example.com/image.jpg",
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Photos: 1",
                    iconId = R.drawable.stream_compose_ic_camera,
                    imagePreviewData = "https://example.com/image.jpg",
                ),
            ),
            Arguments.of(
                "Multiple images with text",
                Message(
                    text = "Check these out",
                    attachments = listOf(
                        Attachment(type = AttachmentType.IMAGE, imageUrl = "https://example.com/1.jpg"),
                        Attachment(type = AttachmentType.IMAGE, imageUrl = "https://example.com/2.jpg"),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Check these out",
                    iconId = R.drawable.stream_compose_ic_camera,
                ),
            ),
            Arguments.of(
                "Multiple images without text",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(type = AttachmentType.IMAGE, imageUrl = "https://example.com/1.jpg"),
                        Attachment(type = AttachmentType.IMAGE, imageUrl = "https://example.com/2.jpg"),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Photos: 2",
                    iconId = R.drawable.stream_compose_ic_camera,
                ),
            ),
            Arguments.of(
                "Single video with text",
                Message(
                    text = "Watch this",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.VIDEO,
                            assetUrl = "https://example.com/video.mp4",
                            imageUrl = "https://example.com/thumb.jpg",
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Watch this",
                    iconId = R.drawable.stream_compose_ic_video_outline,
                    videoPreviewData = "https://example.com/thumb.jpg",
                ),
            ),
            Arguments.of(
                "Single video without text",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.VIDEO,
                            assetUrl = "https://example.com/video.mp4",
                            imageUrl = "https://example.com/thumb.jpg",
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Videos: 1",
                    iconId = R.drawable.stream_compose_ic_video_outline,
                    videoPreviewData = "https://example.com/thumb.jpg",
                ),
            ),
            Arguments.of(
                "Multiple videos",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(type = AttachmentType.VIDEO, assetUrl = "https://example.com/1.mp4"),
                        Attachment(type = AttachmentType.VIDEO, assetUrl = "https://example.com/2.mp4"),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Videos: 2",
                    iconId = R.drawable.stream_compose_ic_video_outline,
                ),
            ),
            Arguments.of(
                "Mixed images and videos",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(type = AttachmentType.IMAGE),
                        Attachment(type = AttachmentType.VIDEO),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Media: 2",
                    iconId = R.drawable.stream_compose_ic_camera,
                ),
            ),
            Arguments.of(
                "Single file with title and mimeType",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.FILE,
                            title = "document.pdf",
                            mimeType = "application/pdf",
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "document.pdf",
                    iconId = R.drawable.stream_compose_ic_file,
                    previewIcon = FileIconData.Pdf,
                ),
            ),
            Arguments.of(
                "Single file with name",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(type = AttachmentType.FILE, title = null, name = "report.docx"),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "report.docx",
                    iconId = R.drawable.stream_compose_ic_file,
                    previewIcon = FileIconData.Generic,
                ),
            ),
            Arguments.of(
                "Single file without title or name",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(type = AttachmentType.FILE, title = null, name = null),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Files: 1",
                    iconId = R.drawable.stream_compose_ic_file,
                    previewIcon = FileIconData.Generic,
                ),
            ),
            Arguments.of(
                "Multiple files",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(type = AttachmentType.FILE),
                        Attachment(type = AttachmentType.FILE),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Files: 2",
                    iconId = R.drawable.stream_compose_ic_file,
                ),
            ),
            Arguments.of(
                "Audio recording",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.AUDIO_RECORDING,
                            extraData = mutableMapOf("duration" to 5000),
                        ),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = MOCK_AUDIO_RECORDING,
                    iconId = R.drawable.stream_compose_ic_microphone,
                ),
            ),
            Arguments.of(
                "Audio file",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(type = AttachmentType.AUDIO, title = "song.mp3"),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "song.mp3",
                    iconId = R.drawable.stream_compose_ic_file,
                    previewIcon = FileIconData.Generic,
                ),
            ),
            Arguments.of(
                "Mixed file types (AUDIO + FILE) without text",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(type = AttachmentType.AUDIO, title = "song.mp3"),
                        Attachment(type = AttachmentType.FILE, title = "document.pdf"),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Files: 2",
                    iconId = R.drawable.stream_compose_ic_file,
                ),
            ),
            Arguments.of(
                "Link with file",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(type = null, ogUrl = "https://example.com", titleLink = "Example"),
                        Attachment(type = AttachmentType.FILE),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Files: 2",
                    iconId = R.drawable.stream_compose_ic_file,
                ),
            ),
            Arguments.of(
                "Giphy with file (no file name)",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.GIPHY,
                            name = "Funny GIF",
                            imageUrl = "https://giphy.com/image.gif",
                        ),
                        Attachment(type = AttachmentType.FILE),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Files: 2",
                    iconId = R.drawable.stream_compose_ic_file,
                ),
            ),
            Arguments.of(
                "Giphy with file with title",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(
                            type = AttachmentType.GIPHY,
                            name = "Funny GIF",
                            imageUrl = "https://giphy.com/image.gif",
                        ),
                        Attachment(type = AttachmentType.FILE, title = "document.pdf"),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Files: 2",
                    iconId = R.drawable.stream_compose_ic_file,
                ),
            ),
            Arguments.of(
                "Image with file",
                Message(
                    text = "Mixed content",
                    attachments = listOf(
                        Attachment(type = AttachmentType.IMAGE),
                        Attachment(type = AttachmentType.FILE),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Mixed content",
                    iconId = R.drawable.stream_compose_ic_file,
                ),
            ),
            Arguments.of(
                "Auto-translation enabled with matching language",
                Message(text = "Hello", i18n = mapOf("pt_text" to "Olá")),
                randomUser(language = "pt"),
                true,
                QuotedMessageBody(text = "Olá"),
            ),
            Arguments.of(
                "Auto-translation enabled without matching language",
                Message(text = "Hello", i18n = mapOf("pt_text" to "Olá")),
                randomUser(language = "fr"),
                true,
                QuotedMessageBody(text = "Hello"),
            ),
            Arguments.of(
                "Auto-translation disabled",
                Message(text = "Hello", i18n = mapOf("pt_text" to "Olá")),
                randomUser(language = "pt"),
                false,
                QuotedMessageBody(text = "Hello"),
            ),
            Arguments.of(
                "Unknown attachment type with text",
                Message(
                    text = "Custom content",
                    attachments = listOf(
                        Attachment(type = "custom_type"),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Custom content",
                    iconId = R.drawable.stream_compose_ic_file,
                ),
            ),
            Arguments.of(
                "Unknown attachment type without text",
                Message(
                    text = "",
                    attachments = listOf(
                        Attachment(type = "custom_type"),
                    ),
                ),
                null,
                false,
                QuotedMessageBody(
                    text = "Files: 1",
                    iconId = R.drawable.stream_compose_ic_file,
                ),
            ),
        )

        private fun mockResources(): Resources {
            val resources: Resources = mock()

            whenever(resources.getString(R.string.stream_ui_message_list_message_deleted)) doReturn MOCK_MESSAGE_DELETED
            whenever(resources.getString(R.string.stream_ui_location_static_message_text)) doReturn MOCK_LOCATION_STATIC
            whenever(resources.getString(R.string.stream_ui_location_live_message_text)) doReturn MOCK_LOCATION_LIVE
            whenever(resources.getString(R.string.stream_compose_quoted_message_giphy_tag)) doReturn MOCK_GIPHY_TAG
            whenever(
                resources.getString(
                    eq(R.string.stream_compose_quoted_message_audio_recording),
                    any(),
                ),
            ) doReturn MOCK_AUDIO_RECORDING
            whenever(
                resources.getString(
                    eq(R.string.stream_compose_quoted_message_media),
                    any(),
                ),
            ) doAnswer { invocation ->
                "Media: ${invocation.arguments[1]}"
            }

            whenever(resources.getQuantityString(any(), any(), any())) doAnswer { invocation ->
                val resId = invocation.arguments[0] as Int
                val count = invocation.arguments[2]
                when (resId) {
                    R.plurals.stream_compose_quoted_message_images -> "Photos: $count"
                    R.plurals.stream_compose_quoted_message_videos -> "Videos: $count"
                    R.plurals.stream_compose_quoted_message_files -> "Files: $count"
                    else -> throw IllegalArgumentException("Unknown plural resource ID")
                }
            }

            return resources
        }

        private fun mockDurationFormatter(): DurationFormatter {
            val formatter: DurationFormatter = mock()
            whenever(formatter.format(any())) doReturn "00:05"
            return formatter
        }
    }
}
