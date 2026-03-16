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

package io.getstream.chat.android.compose.ui.messages.composer.internal.attachments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageComposerAttachmentAudioRecordItemParams
import io.getstream.chat.android.compose.ui.theme.MessageComposerAttachmentFileItemParams
import io.getstream.chat.android.compose.ui.theme.MessageComposerAttachmentMediaItemParams
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.extensions.internal.stableKey
import io.getstream.chat.android.compose.ui.util.rememberAutoScrollLazyListState
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModelFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.previewdata.PreviewAttachmentData
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState

/**
 * Renders all selected attachments in the message composer as a single horizontal scrolling row.
 */
@Composable
internal fun MessageComposerAttachments(
    attachments: List<Attachment>,
    modifier: Modifier = Modifier,
    onAttachmentRemoved: (Attachment) -> Unit = {},
) {
    if (LocalInspectionMode.current) {
        MessageComposerAttachmentsContent(
            modifier = modifier,
            attachments = attachments,
            playerState = AudioPlayerState(getRecordingUri = Attachment::assetUrl),
            onAttachmentRemoved = onAttachmentRemoved,
        )
    } else {
        val viewModelFactory = remember {
            AudioPlayerViewModelFactory(
                getAudioPlayer = { ChatClient.instance().audioPlayer },
                getRecordingUri = { it.assetUrl ?: it.upload?.toUri()?.toString() },
            )
        }
        val audioPlayerViewModel = viewModel(AudioPlayerViewModel::class.java, factory = viewModelFactory)
        val playerState by audioPlayerViewModel.state.collectAsStateWithLifecycle()

        LifecycleEventEffect(event = Lifecycle.Event.ON_PAUSE) {
            audioPlayerViewModel.pause()
        }

        MessageComposerAttachmentsContent(
            modifier = modifier,
            attachments = attachments,
            playerState = playerState,
            onPlayToggleClick = audioPlayerViewModel::playOrPause,
            onPlaySpeedClick = audioPlayerViewModel::changeSpeed,
            onThumbDragStart = audioPlayerViewModel::startSeek,
            onThumbDragStop = audioPlayerViewModel::seekTo,
            onAttachmentRemoved = {
                audioPlayerViewModel.reset(it)
                onAttachmentRemoved(it)
            },
        )
    }
}

@Composable
private fun MessageComposerAttachmentsContent(
    attachments: List<Attachment>,
    playerState: AudioPlayerState,
    modifier: Modifier = Modifier,
    onPlayToggleClick: (Attachment) -> Unit = {},
    onPlaySpeedClick: (Attachment) -> Unit = {},
    onThumbDragStart: (Attachment) -> Unit = {},
    onThumbDragStop: (Attachment, Float) -> Unit = { _, _ -> },
    onAttachmentRemoved: (Attachment) -> Unit = {},
) {
    LazyRow(
        state = rememberAutoScrollLazyListState(attachments.size),
        modifier = modifier.testTag("Stream_MessageComposerAttachments"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs, Alignment.Start),
        contentPadding = PaddingValues(horizontal = StreamTokens.spacingSm),
    ) {
        items(
            items = attachments,
            key = Attachment::stableKey,
        ) { attachment ->
            when {
                attachment.isAudioRecording() ->
                    ChatTheme.componentFactory.MessageComposerAttachmentAudioRecordItem(
                        params = MessageComposerAttachmentAudioRecordItemParams(
                            modifier = Modifier.animateItem(),
                            attachment = attachment,
                            playerState = playerState,
                            onPlayToggleClick = onPlayToggleClick,
                            onPlaySpeedClick = onPlaySpeedClick,
                            onThumbDragStart = onThumbDragStart,
                            onThumbDragStop = onThumbDragStop,
                            onAttachmentRemoved = onAttachmentRemoved,
                        ),
                    )

                attachment.isImage() || attachment.isVideo() ->
                    ChatTheme.componentFactory.MessageComposerAttachmentMediaItem(
                        params = MessageComposerAttachmentMediaItemParams(
                            modifier = Modifier.animateItem(),
                            attachment = attachment,
                            onAttachmentRemoved = onAttachmentRemoved,
                        ),
                    )

                else -> ChatTheme.componentFactory.MessageComposerAttachmentFileItem(
                    params = MessageComposerAttachmentFileItemParams(
                        modifier = Modifier.animateItem(),
                        attachment = attachment,
                        onAttachmentRemoved = onAttachmentRemoved,
                    ),
                )
            }
        }
    }
}

@Preview(widthDp = 640)
@Composable
private fun MessageComposerAttachmentsPreview() {
    ChatTheme {
        MessageComposerAttachments()
    }
}

@Composable
internal fun MessageComposerAttachments() {
    MessageComposerAttachments(
        attachments = listOf(
            PreviewAttachmentData.attachmentImage1,
            PreviewAttachmentData.attachmentVideo1,
            PreviewAttachmentData.attachmentFile1,
            PreviewAttachmentData.attachmentAudioRecording1,
        ),
    )
}
