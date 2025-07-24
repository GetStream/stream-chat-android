/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.audio.WaveformExtractor
import io.getstream.chat.android.client.audio.audioHash
import io.getstream.chat.android.client.extensions.duration
import io.getstream.chat.android.client.extensions.durationInMs
import io.getstream.chat.android.client.extensions.waveformData
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.feature.messages.common.AudioRecordPlayerViewStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.log.taggedLogger

private const val NULL_DURATION = 0.0f

/**
 * A LinearLayoutCompat that present the list of audio messages.
 */
@Suppress("MagicNumber")
internal class AudioRecordingAttachmentsGroupView : LinearLayoutCompat {

    public constructor(context: Context) : super(context.createStreamThemeWrapper())
    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)
    public constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context.createStreamThemeWrapper(), attrs, defStyleAttr)

    init {
        setPadding(2.dpToPx(), 0.dpToPx(), 2.dpToPx(), 2.dpToPx())
    }

    var attachmentClickListener: AttachmentClickListener? = null
    var attachmentLongClickListener: AttachmentLongClickListener? = null

    private val pauseAudioPlayerListener = object : DefaultLifecycleObserver {
        override fun onPause(owner: LifecycleOwner) {
            ChatClient.instance().audioPlayer.pause()
        }
    }

    private val logger by taggedLogger("AudioRecAttachGroupView")

    private var audioAttachments: List<Attachment>? = null

    private val extractor by lazy(LazyThreadSafetyMode.NONE) {
        WaveformExtractor(context, "key", 100) { extractor, progress ->
            if (progress >= 1.0f) {
                logger.v { "[onProgress] progress: $progress, sampleData: ${extractor.sampleData}" }
                if (childCount > 0) {
                    val playerView = getChildAt(0) as AudioRecordPlayerView
                    playerView.setWaveBars(extractor.sampleData)
                    playerView.invalidate()
                    playerView.requestLayout()
                }
            }
        }
    }

    private var style: AudioRecordPlayerViewStyle? = null

    fun setStyle(style: AudioRecordPlayerViewStyle) {
        this.style = style
        children.forEach {
            if (it is AudioRecordPlayerView) {
                it.setStyle(style)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        logger.d { "[onAttachedToWindow] audioAttachments.size: ${audioAttachments?.size}" }
        val audioPlayer = ChatClient.instance().audioPlayer
        val audioHashes = audioAttachments?.map { it.audioHash }?.toHashSet() ?: return
        for (child in children) {
            if (child !is AudioRecordPlayerView) continue
            val audioHash = child.audioHash ?: continue
            if (audioHash !in audioHashes) continue
            audioPlayer.registerStateChange(child, audioHash)
            logger.v { "[onAttachedToWindow] restored (audioHash: $audioHash)" }
        }
        findViewTreeLifecycleOwner()?.lifecycle?.addObserver(pauseAudioPlayerListener)
    }

    /**
     * Shows audio track.
     *
     * @param attachments attachments of type "audio_recording".
     */
    public fun showAudioAttachments(attachments: List<Attachment>) {
        logger.d { "[showAudioAttachments] attachments.size: ${attachments.size}" }
        resetCurrentAttachments()
        removeAllViews()

        val audiosAttachment = attachments.filter { attachment -> attachment.isAudioRecording() }
        this.audioAttachments = audiosAttachment

        audiosAttachment.forEachIndexed(::addAttachmentPlayerView)
    }

    private fun addAttachmentPlayerView(index: Int, attachment: Attachment) {
        logger.d { "[addAttachmentPlayerView] index: $index" }

        AudioRecordPlayerView(context).apply {
            attachment.durationInMs
                ?.let(ChatUI.durationFormatter::format)
                ?.let(::setTotalDuration)

            logger.i { "[addAttachmentPlayerView] waveformData: ${attachment.waveformData}" }
            attachment.waveformData?.let(::setWaveBars)
        }.let { playerView ->
            setOnClickListener { attachmentClickListener?.onAttachmentClick(attachment) }
            setOnLongClickListener {
                attachmentLongClickListener?.onAttachmentLongClick()
                true
            }

            addView(playerView)

            if (index > 0) {
                playerView.updateLayoutParams<MarginLayoutParams> {
                    topMargin = 2.dpToPx()
                }
            }

            val audioPlayer = ChatClient.instance().audioPlayer
            val audioHash = attachment.audioHash
            audioPlayer.registerStateChange(playerView, audioHash)
            playerView.registerButtonsListeners(audioPlayer, attachment, audioHash)
            playerView.audioHash = audioHash

            style?.also { playerView.setStyle(it) }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        logger.d { "[onDetachedFromWindow] no args" }
        findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(pauseAudioPlayerListener)
    }

    private fun resetCurrentAttachments() {
        val audioAttachments = audioAttachments ?: return
        logger.d { "[resetCurrentAttachments] no args" }
        val audioPlayer = ChatClient.instance().audioPlayer
        audioAttachments.forEach { attachment ->
            val audioHash = attachment.audioHash
            logger.v { "[resetCurrentAttachments] audioHash: $audioHash" }
            audioPlayer.resetAudio(audioHash)
        }
    }

    private fun AudioPlayer.registerStateChange(playerView: AudioRecordPlayerView, audioHash: Int) {
        logger.d { "[registerStateChange] audioHash: $audioHash" }
        registerOnAudioStateChange(audioHash) { audioState ->
            logger.d { "[onAudioStateChange] audioHash: $audioHash, audioState: $audioState" }
            when (audioState) {
                AudioState.LOADING -> playerView.setLoading()
                AudioState.PAUSE -> playerView.setPaused()
                AudioState.UNSET, AudioState.IDLE -> playerView.setIdle()
                AudioState.PLAYING -> playerView.setPlaying()
            }
        }
        registerOnProgressStateChange(audioHash) { (durationInMs, progress) ->
            playerView.setDuration(ChatUI.durationFormatter.format(durationInMs))
            playerView.setProgress(progress.toDouble())
        }
        registerOnSpeedChange(audioHash, playerView::setSpeedText)
    }

    private fun AudioRecordPlayerView.registerButtonsListeners(
        audioPlayer: AudioPlayer,
        attachment: Attachment,
        audioHash: Int,
    ) {
        logger.d { "[registerButtonsListeners] audioHash: $audioHash" }
        setOnPlayButtonClickListener {
            logger.v { "[onPlayButtonClick] audioHash: $audioHash" }
            audioPlayer.clearTracks()
            audioAttachments?.forEachIndexed { index, attachment ->
                attachment.assetUrl?.also {
                    val curAudioHash = attachment.audioHash
                    audioPlayer.registerTrack(it, curAudioHash, index)
                }
            }

            val assetUrl = attachment.assetUrl
            if (assetUrl != null) {
                audioPlayer.play(assetUrl, audioHash)
            } else {
                setLoading()
            }
        }

        setOnSpeedButtonClickListener {
            logger.v { "[onSpeedButtonClick] audioHash: $audioHash" }
            audioPlayer.changeSpeed()
        }

        setOnSeekbarMoveListeners({
            logger.v { "[onSeekBarStart] audioHash: $audioHash" }
            audioPlayer.startSeek(attachment.audioHash)
        }, { progress ->
            val durationInSeconds = attachment.duration ?: NULL_DURATION
            val positionInMs = progressToMillis(progress, durationInSeconds)
            logger.v { "[onSeekBarStop] audioHash: $audioHash, progress: $progress, duration: $durationInSeconds" }
            audioPlayer.seekTo(positionInMs, attachment.audioHash)
        })
    }

    private fun progressToMillis(progress: Int, durationInSeconds: Float): Int {
        val durationInMs = durationInSeconds * 1000
        return (progress * durationInMs / 100).toInt()
    }

    /**
     * Unbinds the view.
     */
    public fun unbind() {
        // extractor.stop()
        audioAttachments?.map { attachment -> attachment.audioHash }
            ?.let(ChatClient.instance().audioPlayer::removeAudios)
    }
}
