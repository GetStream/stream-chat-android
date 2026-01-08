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

package io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.doOnAttach
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.extensions.isInt
import io.getstream.chat.android.ui.databinding.StreamUiAudioRecordPlayerBinding
import io.getstream.chat.android.ui.feature.messages.common.AudioRecordPlayerViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.setPaddingCompat
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.log.taggedLogger

private const val PERCENTAGE = 100

/**
 * Embedded player of audio messages.
 */
internal class AudioRecordPlayerView : LinearLayoutCompat {

    public constructor(context: Context) : this(context, null)
    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    public constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context.createStreamThemeWrapper(), attrs, defStyleAttr) {
        init(context, attrs)
    }

    private lateinit var binding: StreamUiAudioRecordPlayerBinding
    private lateinit var style: AudioRecordPlayerViewStyle

    private fun init(context: Context, attrs: AttributeSet?) {
        binding = StreamUiAudioRecordPlayerBinding.inflate(streamThemeInflater, this)
        setStyle(AudioRecordPlayerViewStyle(context, attrs))
    }

    private val logger by taggedLogger("Chat:PlayerView")

    private var totalDuration: String? = null
    internal var audioHash: Int? = null

    public fun setStyle(style: AudioRecordPlayerViewStyle) {
        this.style = style

        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = style.tintedBackgroundDrawable

        setPaddingCompat(style.padding)

        doOnAttach {
            it.updateLayoutParams { height = style.height }
        }
        with(binding) {
            playbackProgressContainer.updateLayoutParams {
                width = style.playbackProgressContainerSize.width
                height = style.playbackProgressContainerSize.height
            }

            progressBar.indeterminateDrawable = style.tintedProgressBarDrawable
            progressBar.updateLayoutParams {
                width = style.progressBarSize.width
                height = style.progressBarSize.height
            }

            playButton.setPaddingCompat(style.playbackButtonPadding)
            playButton.setImageDrawable(style.tintedPlayIconDrawable)
            playButton.setBackgroundDrawable(style.tintedPlaybackButtonBackground)
            playButton.elevation = style.playbackButtonElevation.toFloat()
            playButton.updateLayoutParams {
                width = style.playbackButtonSize.width
                height = style.playbackButtonSize.height
            }

            duration.setTextStyle(style.durationTextStyle)
            duration.updateLayoutParams<MarginLayoutParams> {
                width = style.durationTextViewSize.width
                height = style.durationTextViewSize.height
                marginStart = style.durationTextMarginStart
            }

            audioSeekBar.setPlayedWaveBarColor(style.waveBarColorPlayed)
            audioSeekBar.setFutureWaveBarColor(style.waveBarColorFuture)
            audioSeekBar.setScrubberDrawable(style.tintedScrubberDrawable)
            audioSeekBar.setScrubberWidth(style.scrubberWidthDefault, style.scrubberWidthPressed)
            audioSeekBar.updateLayoutParams<MarginLayoutParams> {
                height = style.waveBarHeight
                marginStart = style.waveBarMarginStart
            }

            audioFileIconContainer.updateLayoutParams { width = style.fileIconContainerWidth }
            audioFileIconContainer.isVisible = style.isFileIconContainerVisible

            audioFileIcon.setImageDrawable(style.audioFileIconDrawable)

            audioSpeedButton.setTextStyle(style.speedButtonTextStyle)
            audioSpeedButton.background = style.tintedSpeedButtonBackground
            audioSpeedButton.elevation = style.speedButtonElevation.toFloat()
            audioSpeedButton.updateLayoutParams {
                width = style.speedButtonSize.width
                height = style.speedButtonSize.height
            }
        }
    }

    /**
     * Sets total duration of audio tracker as a String. When the view goes to idle state, this is the duration show
     * to the user.
     *
     * @param duration
     */
    public fun setTotalDuration(duration: String) {
        logger.i { "[setTotalDuration] duration: $duration" }
        totalDuration = duration
        setDuration(duration)
    }

    /**
     * Sets the wave bars of the seekbar inside the view.
     *
     * @param waveBars each from 0 to 1.
     */
    public fun setWaveBars(waveBars: List<Float>) {
        logger.i { "[setWaveBars] value: $waveBars" }
        binding.audioSeekBar.waveBars = waveBars
    }

    /**
     * Sets the current duration of the audio.
     *
     * @param duration
     */
    public fun setDuration(duration: String) {
        binding.duration.run {
            text = duration
            visibility = View.VISIBLE
        }
    }

    /**
     * Sets the progress of the seekbar.
     *
     * @param progress
     */
    public fun setProgress(progress: Double) {
        binding.audioSeekBar.setProgress((progress * PERCENTAGE).toFloat())
    }

    /**
     * Sets the view into loading state.
     */
    public fun setLoading() {
        binding.progressBar.isVisible = true
        binding.playButton.isVisible = false
    }

    /**
     * Set the view into playing state.
     */
    public fun setPlaying() {
        with(binding) {
            progressBar.isVisible = false
            playButton.isVisible = true
            playButton.setImageDrawable(style.tintedPauseIconDrawable)
            audioSpeedButton.isVisible = true
            audioFileIcon.isVisible = false
        }
    }

    /**
     * Sets the view into idle state.
     */
    public fun setIdle() {
        totalDuration?.let(::setDuration)
        setProgress(0.0)
        with(binding) {
            progressBar.isVisible = false
            playButton.isVisible = true
            playButton.setImageDrawable(style.tintedPlayIconDrawable)
            audioSpeedButton.isVisible = false
            audioFileIcon.isVisible = true
        }
    }

    /**
     * Set sthe view into paused state.
     */
    public fun setPaused() {
        with(binding) {
            progressBar.isVisible = false
            playButton.isVisible = true
            playButton.setImageDrawable(style.tintedPlayIconDrawable)
            audioSpeedButton.isVisible = true
            audioFileIcon.isVisible = false
        }
    }

    /**
     * The the text of the speed button.
     *
     * @param speed
     */
    public fun setSpeedText(speed: Float) {
        logger.d { "[setSpeedText] speed: $speed" }
        binding.audioSpeedButton.text = when (speed.isInt()) {
            true -> "x${speed.toInt()}"
            else -> "x$speed"
        }
    }

    /**
     * Register a [listener] for the play button
     *
     * @param listener
     */
    public fun setOnPlayButtonClickListener(listener: () -> Unit) {
        binding.playButton.setOnClickListener { listener() }
    }

    /**
     * Register a [listener] for the speed button
     *
     * @param listener
     */
    public fun setOnSpeedButtonClickListener(listener: () -> Unit) {
        binding.audioSpeedButton.setOnClickListener { listener() }
    }

    /**
     * Register a callback for the seekbar movement.
     *
     * @param startDrag Triggered when the drag of the seekbar starts
     * @param stopDrag Triggered when the drag of the seekbar stops
     */
    public fun setOnSeekbarMoveListeners(startDrag: () -> Unit, stopDrag: (Int) -> Unit) {
        binding.audioSeekBar.run {
            setOnStartDragListener(startDrag)
            setOnEndDragListener(stopDrag)
        }
    }
}
