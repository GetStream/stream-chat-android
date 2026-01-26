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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.getstream.chat.android.client.extensions.durationInMs
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider
import io.getstream.chat.android.compose.ui.util.getMessageTextResId
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.helper.DurationFormatter
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.extensions.hasLink
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl

internal class QuotedMessageBodyBuilder(
    private val resources: Resources,
    private val autoTranslationEnabled: Boolean,
    private val durationFormatter: DurationFormatter,
    private val streamCdnImageResizing: StreamCdnImageResizing,
) {
    fun build(message: Message, currentUser: User?): QuotedMessageBody {
        val messageText = when {
            autoTranslationEnabled -> {
                currentUser?.language
                    ?.let(message::getTranslation)
                    ?.takeIf(String::isNotBlank)
                    ?: message.text
            }

            else -> {
                message.text
            }
        }

        val poll = message.poll
        val sharedLocation = message.sharedLocation

        return when {
            message.isDeleted() -> {
                QuotedMessageBody(
                    text = resources.getString(R.string.stream_ui_message_list_message_deleted),
                )
            }

            poll != null -> {
                QuotedMessageBody(
                    iconId = R.drawable.stream_compose_ic_chart,
                    text = poll.name,
                )
            }

            sharedLocation != null -> {
                QuotedMessageBody(
                    iconId = R.drawable.stream_compose_ic_map_pin,
                    text = resources.getString(sharedLocation.getMessageTextResId()),
                )
            }

            else -> {
                bodyForAttachment(message.attachments, messageText) ?: QuotedMessageBody(messageText)
            }
        }
    }

    @Suppress("LongMethod")
    private fun bodyForAttachment(attachments: List<Attachment>, messageText: String): QuotedMessageBody? {
        if (attachments.isEmpty()) return null

        val summary = attachmentsSummary(attachments)
        val size = attachments.size

        return when {
            // Link attachment is shown only if there are no files attached
            summary.linkAttachment != null && summary.fileCount == 0 -> {
                QuotedMessageBody(
                    text = messageText.ifBlank { summary.linkAttachment.run { titleLink ?: ogUrl } }.orEmpty(),
                    iconId = R.drawable.stream_compose_ic_link,
                    imagePreviewData = summary.linkAttachment.imagePreviewUrl,
                )
            }

            // Giphy attachment is shown as single image only if there are no files attached
            summary.giphyAttachment != null && summary.fileCount == 0 -> {
                QuotedMessageBody(
                    text = messageText.ifBlank { textForGiphy(summary.giphyAttachment) },
                    imagePreviewData = summary.giphyAttachment.imagePreviewUrl,
                )
            }

            summary.isOnlyImages -> {
                QuotedMessageBody(
                    text = messageText.ifBlank {
                        resources.getQuantityString(R.plurals.stream_compose_quoted_message_images, size, size)
                    },
                    iconId = R.drawable.stream_compose_ic_camera,
                    imagePreviewData = summary.mediaPreviewData?.takeIf { summary.fileCount == 1 },
                )
            }

            summary.isOnlyVideos -> {
                QuotedMessageBody(
                    text = messageText.ifBlank {
                        resources.getQuantityString(R.plurals.stream_compose_quoted_message_videos, size, size)
                    },
                    iconId = R.drawable.stream_compose_ic_video_outline,
                    videoPreviewData = summary.mediaPreviewData?.takeIf { summary.fileCount == 1 },
                )
            }

            summary.isOnlyMedia -> {
                QuotedMessageBody(
                    text = messageText.ifBlank {
                        resources.getString(R.string.stream_compose_quoted_message_media, size)
                    },
                    iconId = R.drawable.stream_compose_ic_camera,
                )
            }

            summary.audioRecordingAttachment != null && size == 1 -> {
                QuotedMessageBody(
                    text = messageText.ifBlank { textForAudioRecording(summary.audioRecordingAttachment) },
                    iconId = R.drawable.stream_compose_ic_microphone,
                )
            }

            summary.fileAttachment != null && size == 1 -> {
                QuotedMessageBody(
                    text = messageText.ifBlank { textForFile(summary.fileAttachment) },
                    iconId = R.drawable.stream_compose_ic_file,
                    previewIcon = MimeTypeIconProvider.getIcon(summary.fileAttachment.mimeType),
                )
            }

            else -> {
                QuotedMessageBody(
                    text = messageText.ifBlank {
                        resources.getQuantityString(R.plurals.stream_compose_quoted_message_files, size, size)
                    },
                    iconId = R.drawable.stream_compose_ic_file,
                )
            }
        }
    }

    private fun textForGiphy(giphyAttachment: Attachment): String = giphyAttachment.run { name ?: text ?: title }
        ?: resources.getString(R.string.stream_compose_quoted_message_giphy_tag)

    private fun textForAudioRecording(audioRecordingAttachment: Attachment): String {
        val duration = durationFormatter.format(audioRecordingAttachment.durationInMs ?: 0)
        return resources.getString(R.string.stream_compose_quoted_message_audio_recording, duration)
    }

    private fun textForFile(fileAttachment: Attachment): String = fileAttachment.run { title ?: name }
        ?: resources.getQuantityString(R.plurals.stream_compose_quoted_message_files, 1, 1)

    // Implemented as a loop with mutable variables for performance reasons since this is called at rendering time
    private fun attachmentsSummary(attachments: List<Attachment>): AttachmentSummary {
        var imageCount = 0
        var videoCount = 0
        var fileCount = 0
        var linkAttachment: Attachment? = null
        var giphyAttachment: Attachment? = null
        var fileAttachment: Attachment? = null
        var audioRecordingAttachment: Attachment? = null
        var mediaPreviewData: Any? = null

        for (attachment in attachments) {
            val type = attachment.type

            when {
                attachment.hasLink() && !attachment.isGiphy() -> linkAttachment = attachment

                type == AttachmentType.GIPHY -> giphyAttachment = attachment

                type == AttachmentType.IMAGE || type == AttachmentType.IMGUR -> {
                    imageCount++
                    fileCount++
                    mediaPreviewData = attachment.upload ?: attachment.imagePreviewUrl
                        ?.applyStreamCdnImageResizingIfEnabled(streamCdnImageResizing)
                }

                type == AttachmentType.VIDEO -> {
                    videoCount++
                    fileCount++
                    mediaPreviewData = attachment.upload ?: attachment.imagePreviewUrl
                }

                type == AttachmentType.AUDIO_RECORDING -> {
                    fileCount++
                    audioRecordingAttachment = attachment
                }

                type == AttachmentType.AUDIO || type == AttachmentType.FILE -> {
                    fileCount++
                    fileAttachment = attachment
                }
            }
        }

        val total = attachments.size
        return AttachmentSummary(
            isOnlyImages = imageCount == total && total > 0,
            isOnlyVideos = videoCount == total && total > 0,
            isOnlyMedia = (imageCount + videoCount) == total && total > 0,
            linkAttachment = linkAttachment,
            giphyAttachment = giphyAttachment,
            fileAttachment = fileAttachment,
            audioRecordingAttachment = audioRecordingAttachment,
            mediaPreviewData = mediaPreviewData,
            fileCount = fileCount,
        )
    }

    @Suppress("LongParameterList")
    private class AttachmentSummary(
        val isOnlyImages: Boolean,
        val isOnlyVideos: Boolean,
        val isOnlyMedia: Boolean,
        val linkAttachment: Attachment?,
        val giphyAttachment: Attachment?,
        val fileAttachment: Attachment?,
        val audioRecordingAttachment: Attachment?,
        val mediaPreviewData: Any?,
        val fileCount: Int,
    )
}

@Composable
internal fun rememberBodyBuilder(): QuotedMessageBodyBuilder {
    val resources = LocalContext.current.resources
    val autoTranslationEnabled = ChatTheme.autoTranslationEnabled
    val durationFormatter = ChatTheme.durationFormatter
    val streamCdnImageResizing: StreamCdnImageResizing = ChatTheme.streamCdnImageResizing

    return remember(resources, autoTranslationEnabled, durationFormatter, streamCdnImageResizing) {
        QuotedMessageBodyBuilder(
            resources = resources,
            autoTranslationEnabled = autoTranslationEnabled,
            durationFormatter = durationFormatter,
            streamCdnImageResizing = streamCdnImageResizing,
        )
    }
}
