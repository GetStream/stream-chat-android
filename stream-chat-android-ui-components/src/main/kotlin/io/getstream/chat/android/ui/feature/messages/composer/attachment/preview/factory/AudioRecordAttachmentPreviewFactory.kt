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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory

import android.view.ViewGroup
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.DurationParser
import io.getstream.chat.android.ui.databinding.StreamUiAudioRecordPlayerPreviewBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal.AudioRecordPlayer
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

private const val NULL_DURATION = 0.0

/**
 * The default [AttachmentPreviewFactory] for file attachments.
 */
public class AudioRecordAttachmentPreviewFactory : AttachmentPreviewFactory {
    /**
     * Checks if the factory can create a preview ViewHolder for this attachment.
     *
     * @param attachment The attachment we want to show a preview for.
     * @return True if the factory is able to provide a preview for the given [Attachment].
     */
    public override fun canHandle(attachment: Attachment): Boolean {
        return attachment.isAudioRecording()
    }

    /**
     * Creates and instantiates a new instance of [AudioRecordAttachmentPreviewFactory].
     *
     * @param parentView The parent container.
     * @param attachmentRemovalListener Click listener for the remove attachment button.
     * @param style Used to style the factory. If null, the factory will retain
     * the default appearance.
     *
     * @return An instance of attachment preview ViewHolder.
     */
    override fun onCreateViewHolder(
        parentView: ViewGroup,
        attachmentRemovalListener: (Attachment) -> Unit,
        style: MessageComposerViewStyle?,
    ): AttachmentPreviewViewHolder =
        StreamUiAudioRecordPlayerPreviewBinding
            .inflate(parentView.context.streamThemeInflater, parentView, false)
            .let { AudioRecordAttachmentPreviewHandler(it, attachmentRemovalListener) }

    /**
     * A ViewHolder for file attachment preview.
     *
     * @param binding Binding generated for the layout.
     * @param attachmentRemovalListener Click listener for the remove attachment button.
     */
    private class AudioRecordAttachmentPreviewHandler(
        private val binding: StreamUiAudioRecordPlayerPreviewBinding,
        attachmentRemovalListener: (Attachment) -> Unit,
    ) : AttachmentPreviewViewHolder(binding.root) {

        private lateinit var attachment: Attachment

        init {
            binding.removeButton.setOnClickListener { attachmentRemovalListener(attachment) }
        }

        override fun bind(attachment: Attachment) {
            if (attachment.assetUrl == null) return

            val audioPlayer = ChatClient.instance().audioPlayer
            val playerView = binding.playerView

            this.attachment = attachment

            (attachment.extraData["duration"] as? Double)
                ?.toInt()
                ?.let(DurationParser::durationInMilliToReadableTime)
                ?.let { duration ->
                    playerView.setDuration(duration)
                }

            (attachment.extraData["waveList"] as? List<Float>)?.let { waveBars ->
                playerView.setWaveBars(waveBars)
            }

            audioPlayer.registerStateChange(playerView, attachment.hashCode())
            playerView.registerButtonsListeners(audioPlayer, attachment, attachment.hashCode())
        }

        override fun unbind() {
            ChatClient.instance().audioPlayer.removeAudios(listOf(attachment.hashCode()))
        }
    }
}

private fun AudioPlayer.registerStateChange(playerView: AudioRecordPlayer, hashCode: Int) {
    onAudioStateChange(hashCode) { audioState ->
        when (audioState) {
            AudioState.LOADING -> playerView.setLoading()
            AudioState.PAUSE -> playerView.setPaused()
            AudioState.UNSET, AudioState.IDLE -> playerView.setIdle()
            AudioState.PLAYING -> playerView.setPlaying()
        }
    }
    onProgressStateChange(hashCode) { (duration, progress) ->
        playerView.setDuration(DurationParser.durationInMilliToReadableTime(duration))
        playerView.setProgress(progress)
    }
    onSpeedChange(hashCode, playerView::setSpeedText)
}

private fun AudioRecordPlayer.registerButtonsListeners(
    audioPlayer: AudioPlayer,
    attachment: Attachment,
    hashCode: Int,
) {
    onPlayButtonPress {
        if (attachment.assetUrl != null) {
            audioPlayer.play(attachment.assetUrl!!, hashCode)
        } else {
            setLoading()
        }
    }

    onSpeedButtonPress {
        audioPlayer.changeSpeed()
    }

    onSeekbarMove({
        audioPlayer.startSeek(attachment.hashCode())
    }, { progress ->
        audioPlayer.seekTo(
            progressToDecimal(progress, attachment.extraData["duration"] as? Double),
            attachment.hashCode()
        )
    })
}

@Suppress("MagicNumber")
private fun progressToDecimal(progress: Int, totalDuration: Double?): Int =
    progress * (totalDuration ?: NULL_DURATION).toInt() / 100
