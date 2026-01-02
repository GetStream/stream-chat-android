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

package io.getstream.chat.android.ui.feature.messages.common

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.messages.list.background.ShapeAppearanceModelFactory
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewPadding
import io.getstream.chat.android.ui.helper.ViewSize
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getColorOrNull
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Style for [io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal.AudioRecordPlayerView].
 *
 * @param height Height of the view.
 * @param padding Padding of the view.
 * @param backgroundDrawable Background drawable of the view.
 * @param backgroundDrawableTint Background drawable tint of the view.
 * @param playbackProgressContainerSize Size of the playback progress container.
 * @param playbackButtonSize Size of the playback button.
 * @param playbackButtonPadding Padding of the playback button.
 * @param playbackButtonElevation Elevation of the playback button.
 * @param playbackButtonBackground Background of the playback button.
 * @param playbackButtonBackgroundTint Background tint of the playback button.
 * @param playIconDrawable Play icon drawable.
 * @param playIconDrawableTint Play icon drawable tint.
 * @param pauseIconDrawable Pause icon drawable.
 * @param pauseIconDrawableTint Pause icon drawable tint.
 * @param progressBarDrawable Progress bar drawable.
 * @param progressBarDrawableTint Progress bar drawable tint.
 * @param progressBarSize Size of the progress bar.
 * @param durationTextViewSize Size of the duration text view.
 * @param durationTextMarginStart Margin start of the duration text view.
 * @param durationTextStyle Style of the duration text.
 * @param waveBarHeight Height of the wave bar.
 * @param waveBarMarginStart Margin start of the wave bar.
 * @param waveBarColorPlayed Color of the played wave bar.
 * @param waveBarColorFuture Color of the future wave bar.
 * @param scrubberDrawable Scrubber drawable.
 * @param scrubberDrawableTint Scrubber drawable tint.
 * @param scrubberWidthDefault Default width of the scrubber.
 * @param scrubberWidthPressed Pressed width of the scrubber.
 * @param isFileIconContainerVisible Is file icon container visible.
 * @param fileIconContainerWidth Width of the file icon container.
 * @param audioFileIconDrawable Audio file icon drawable.
 * @param speedButtonSize Size of the speed button.
 * @param speedButtonElevation Elevation of the speed button.
 * @param speedButtonBackground Background of the speed button.
 * @param speedButtonBackgroundTint Background tint of the speed button.
 * @param speedButtonTextStyle Style of the speed button text.
 */
public data class AudioRecordPlayerViewStyle(
    @Px public val height: Int,
    public val padding: ViewPadding,
    public val backgroundDrawable: Drawable?,
    @ColorInt public val backgroundDrawableTint: Int?,
    public val playbackProgressContainerSize: ViewSize,
    public val playbackButtonSize: ViewSize,
    public val playbackButtonPadding: ViewPadding,
    @Px public val playbackButtonElevation: Int,
    public val playbackButtonBackground: Drawable?,
    @ColorInt public val playbackButtonBackgroundTint: Int?,
    public val progressBarDrawable: Drawable?,
    @ColorInt public val progressBarDrawableTint: Int?,
    public val progressBarSize: ViewSize,
    public val playIconDrawable: Drawable?,
    @ColorInt public val playIconDrawableTint: Int?,
    public val pauseIconDrawable: Drawable?,
    @ColorInt public val pauseIconDrawableTint: Int?,
    public val durationTextViewSize: ViewSize,
    @Px public val durationTextMarginStart: Int,
    public val durationTextStyle: TextStyle,
    @Px public val waveBarHeight: Int,
    @Px public val waveBarMarginStart: Int,
    @ColorInt public val waveBarColorPlayed: Int,
    @ColorInt public val waveBarColorFuture: Int,
    public val scrubberDrawable: Drawable?,
    @ColorInt public val scrubberDrawableTint: Int?,
    @Px public val scrubberWidthDefault: Int,
    @Px public val scrubberWidthPressed: Int,
    public val isFileIconContainerVisible: Boolean,
    @Px public val fileIconContainerWidth: Int,
    public val audioFileIconDrawable: Drawable?,
    public val speedButtonTextStyle: TextStyle,
    public val speedButtonBackground: Drawable?,
    @ColorInt public val speedButtonBackgroundTint: Int?,
    public val speedButtonSize: ViewSize,
    @Px public val speedButtonElevation: Int,
) : ViewStyle {

    val tintedBackgroundDrawable: Drawable?
        get() = backgroundDrawable?.applyTint(backgroundDrawableTint)

    val tintedPlaybackButtonBackground: Drawable?
        get() = playbackButtonBackground?.applyTint(playbackButtonBackgroundTint)

    val tintedProgressBarDrawable: Drawable?
        get() = progressBarDrawable?.applyTint(progressBarDrawableTint)

    val tintedPlayIconDrawable: Drawable?
        get() = playIconDrawable?.applyTint(playIconDrawableTint)

    val tintedPauseIconDrawable: Drawable?
        get() = pauseIconDrawable?.applyTint(pauseIconDrawableTint)

    val tintedScrubberDrawable: Drawable?
        get() = scrubberDrawable?.applyTint(scrubberDrawableTint)

    val tintedSpeedButtonBackground: Drawable?
        get() = speedButtonBackground?.applyTint(speedButtonBackgroundTint)

    public companion object {

        internal operator fun invoke(context: Context, attrs: AttributeSet?): AudioRecordPlayerViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AudioRecordPlayerView,
                R.attr.streamUiAudioRecordPlayerViewStyle,
                R.style.StreamUi_AudioRecordPlayerView,
            ).use {
                return invoke(context, it)
                    .let(TransformStyle.audioRecordPlayerViewStyle::transform)
            }
        }

        internal operator fun invoke(context: Context, attributes: TypedArray): AudioRecordPlayerViewStyle {
            val height = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerHeight,
                context.getDimension(R.dimen.stream_ui_audio_record_player_height),
            )

            val paddingStart = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPaddingStart,
                context.getDimension(R.dimen.stream_ui_audio_record_player_padding_start),
            )
            val paddingTop = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPaddingTop,
                context.getDimension(R.dimen.stream_ui_audio_record_player_padding_top),
            )
            val paddingEnd = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPaddingEnd,
                context.getDimension(R.dimen.stream_ui_audio_record_player_padding_end),
            )
            val paddingBottom = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPaddingBottom,
                context.getDimension(R.dimen.stream_ui_audio_record_player_padding_bottom),
            )

            val backgroundDrawable = ShapeAppearanceModelFactory.audioBackground(context)
            val backgroundDrawableTint: Int? = null

            val playbackProgressContainerWidth = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPlaybackProgressContainerWidth,
                context.getDimension(R.dimen.stream_ui_audio_record_player_playback_progress_container_width),
            )
            val playbackProgressContainerHeight = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPlaybackProgressContainerHeight,
                context.getDimension(R.dimen.stream_ui_audio_record_player_playback_progress_container_height),
            )

            val playbackButtonWidth = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPlaybackButtonWidth,
                context.getDimension(R.dimen.stream_ui_audio_record_player_playback_button_width),
            )
            val playbackButtonHeight = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPlaybackButtonHeight,
                context.getDimension(R.dimen.stream_ui_audio_record_player_playback_button_height),
            )
            val playbackButtonElevation = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPlaybackButtonElevation,
                context.getDimension(R.dimen.stream_ui_audio_record_player_playback_button_elevation),
            )
            val playbackButtonPadding = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPlaybackButtonPadding,
                context.getDimension(R.dimen.stream_ui_audio_record_player_playback_button_padding),
            )
            val playbackButtonBackground: Drawable = attributes.getDrawable(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPlaybackButtonBackground,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_white_shape_circular)!!
            val playbackButtonBackgroundTint = attributes.getColorOrNull(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPlaybackButtonBackgroundTint,
            )

            val playIconDrawable = attributes.getDrawable(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPlayIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_play)!!
            val playIconDrawableTint: Int? = attributes.getColorOrNull(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPlayIconDrawableTint,
            )

            val pauseIconDrawable = attributes.getDrawable(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPauseIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_pause)!!
            val pauseIconDrawableTint: Int? = attributes.getColorOrNull(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerPauseIconDrawableTint,
            )

            val progressBarDrawable = attributes.getDrawable(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerProgressBarDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)!!
            val progressBarDrawableTint: Int? = attributes.getColorOrNull(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerProgressBarDrawableTint,
            )
            val progressBarWidth = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerProgressBarWidth,
                context.getDimension(R.dimen.stream_ui_audio_record_player_progress_bar_width),
            )
            val progressBarHeight = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerProgressBarHeight,
                context.getDimension(R.dimen.stream_ui_audio_record_player_progress_bar_height),
            )

            val durationTextViewWidth = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerDurationTextViewWidth,
                context.getDimension(R.dimen.stream_ui_audio_record_player_duration_text_view_width),
            )
            val durationTextViewHeight = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerDurationTextViewHeight,
                context.getDimension(R.dimen.stream_ui_audio_record_player_duration_text_view_height),
            )
            val durationTextViewMarginStart = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerDurationTextViewMarginStart,
                context.getDimension(R.dimen.stream_ui_audio_record_player_duration_text_view_margin_start),
            )
            val durationTextStyle = TextStyle.Builder(attributes)
                .size(
                    R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerDurationTextSize,
                    context.getDimension(R.dimen.stream_ui_audio_record_player_duration_text_size),
                )
                .color(
                    R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerDurationTextColor,
                    context.getColorCompat(R.color.stream_ui_audio_record_player_duration_text_color),
                )
                .font(
                    R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerDurationTextFontAssets,
                    R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerDurationTextFont,
                )
                .style(
                    R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerDurationTextStyle,
                    Typeface.NORMAL,
                )
                .build()

            val waveBarHeight = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerWaveBarHeight,
                context.getDimension(R.dimen.stream_ui_audio_record_player_wave_bar_height),
            )
            val waveBarMarginStart = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerWaveBarMarginStart,
                context.getDimension(R.dimen.stream_ui_audio_record_player_wave_bar_margin_start),
            )
            val waveBarColorPlayed = context.getColorCompat(R.color.stream_ui_accent_blue)
            val waveBarColorFuture = context.getColorCompat(R.color.stream_ui_grey)

            val scrubberWidthDefault = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerScrubberWidthDefault,
                context.getDimension(R.dimen.stream_ui_audio_record_player_scrubber_width_default),
            )
            val scrubberWidthPressed = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerScrubberWidthPressed,
                context.getDimension(R.dimen.stream_ui_audio_record_player_scrubber_width_pressed),
            )
            val scrubberDrawable = attributes.getDrawable(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerScrubberDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_share_rectangle)!!
            val scrubberDrawableTint: Int? = attributes.getColorOrNull(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerScrubberDrawableTint,
            )

            val fileIconContainerWidth = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerFileIconContainerWidth,
                context.getDimension(R.dimen.stream_ui_audio_record_player_file_icon_container_width),
            )
            val fileIconContainerVisible = attributes.getBoolean(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerFileIconContainerVisible,
                context.resources.getBoolean(R.bool.stream_ui_audio_record_player_file_icon_container_visible),
            )

            val audioFileIconDrawable = attributes.getDrawable(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerAudioFileIconDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_file_aac)!!

            val speedButtonWidth = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerSpeedButtonWidth,
                context.getDimension(R.dimen.stream_ui_audio_record_player_speed_button_width),
            )
            val speedButtonHeight = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerSpeedButtonHeight,
                context.getDimension(R.dimen.stream_ui_audio_record_player_speed_button_height),
            )
            val speedButtonElevation = attributes.getDimensionPixelSize(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerSpeedButtonElevation,
                context.getDimension(R.dimen.stream_ui_audio_record_player_speed_button_elevation),
            )
            val speedButtonBackground = attributes.getDrawable(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerSpeedButtonBackgroundDrawable,
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_literal_white_shape_16dp_corners)!!
            val speedButtonBackgroundTint: Int? = attributes.getColorOrNull(
                R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerSpeedButtonBackgroundDrawableTint,
            )
            val speedButtonTextStyle = TextStyle.Builder(attributes)
                .size(
                    R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerSpeedButtonTextSize,
                    context.getDimension(R.dimen.stream_ui_audio_record_player_speed_text_size),
                )
                .color(
                    R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerSpeedButtonTextColor,
                    context.getColorCompat(R.color.stream_ui_audio_record_player_speed_text_color),
                )
                .font(
                    R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerSpeedButtonTextFontAssets,
                    R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerSpeedButtonTextFont,
                )
                .style(
                    R.styleable.AudioRecordPlayerView_streamUiAudioRecordPlayerSpeedButtonTextStyle,
                    Typeface.NORMAL,
                )
                .build()

            return AudioRecordPlayerViewStyle(
                height = height,
                padding = ViewPadding(
                    start = paddingStart,
                    top = paddingTop,
                    end = paddingEnd,
                    bottom = paddingBottom,
                ),
                backgroundDrawable = backgroundDrawable,
                backgroundDrawableTint = backgroundDrawableTint,
                // Playback Progress Container
                playbackProgressContainerSize = ViewSize(
                    width = playbackProgressContainerWidth,
                    height = playbackProgressContainerHeight,
                ),
                // Playback Button
                playbackButtonSize = ViewSize(
                    width = playbackButtonWidth,
                    height = playbackButtonHeight,
                ),
                playbackButtonPadding = ViewPadding(
                    start = playbackButtonPadding,
                    top = playbackButtonPadding,
                    end = playbackButtonPadding,
                    bottom = playbackButtonPadding,
                ),
                playbackButtonElevation = playbackButtonElevation,
                playbackButtonBackground = playbackButtonBackground,
                playbackButtonBackgroundTint = playbackButtonBackgroundTint,
                playIconDrawable = playIconDrawable,
                playIconDrawableTint = playIconDrawableTint,
                pauseIconDrawable = pauseIconDrawable,
                pauseIconDrawableTint = pauseIconDrawableTint,
                // Progress Bar
                progressBarDrawable = progressBarDrawable,
                progressBarDrawableTint = progressBarDrawableTint,
                progressBarSize = ViewSize(
                    width = progressBarWidth,
                    height = progressBarHeight,
                ),
                // Duration Text
                durationTextViewSize = ViewSize(
                    width = durationTextViewWidth,
                    height = durationTextViewHeight,
                ),
                durationTextMarginStart = durationTextViewMarginStart,
                durationTextStyle = durationTextStyle,
                // Wave Bar
                waveBarHeight = waveBarHeight,
                waveBarMarginStart = waveBarMarginStart,
                waveBarColorPlayed = waveBarColorPlayed,
                waveBarColorFuture = waveBarColorFuture,
                // Scrubber
                scrubberDrawable = scrubberDrawable,
                scrubberDrawableTint = scrubberDrawableTint,
                scrubberWidthDefault = scrubberWidthDefault,
                scrubberWidthPressed = scrubberWidthPressed,
                // File Icon Container
                fileIconContainerWidth = fileIconContainerWidth,
                isFileIconContainerVisible = fileIconContainerVisible,
                audioFileIconDrawable = audioFileIconDrawable,
                // Speed Button
                speedButtonSize = ViewSize(
                    width = speedButtonWidth,
                    height = speedButtonHeight,
                ),
                speedButtonElevation = speedButtonElevation,
                speedButtonBackground = speedButtonBackground,
                speedButtonBackgroundTint = speedButtonBackgroundTint,
                speedButtonTextStyle = speedButtonTextStyle,
            )
        }

        public fun default(context: Context): AudioRecordPlayerViewStyle {
            val height = context.getDimension(R.dimen.stream_ui_audio_record_player_height)

            val paddingStart = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_start)
            val paddingTop = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_top)
            val paddingEnd = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_end)
            val paddingBottom = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_bottom)

            val backgroundDrawable = ShapeAppearanceModelFactory.audioBackground(context)
            val backgroundDrawableTint: Int? = null

            val playbackProgressContainerWidth = context.getDimension(R.dimen.stream_ui_audio_record_player_playback_progress_container_width)
            val playbackProgressContainerHeight = context.getDimension(R.dimen.stream_ui_audio_record_player_playback_progress_container_height)

            val playbackButtonWidth = context.getDimension(R.dimen.stream_ui_audio_record_player_playback_button_width)
            val playbackButtonHeight = context.getDimension(R.dimen.stream_ui_audio_record_player_playback_button_height)
            val playbackButtonElevation = context.getDimension(R.dimen.stream_ui_audio_record_player_playback_button_elevation)
            val playbackButtonPadding = context.getDimension(R.dimen.stream_ui_audio_record_player_playback_button_padding)
            val playbackButtonBackground = context.getDrawableCompat(R.drawable.stream_ui_white_shape_circular)
            val playbackButtonBackgroundTint: Int? = null

            val playIconDrawable = context.getDrawableCompat(R.drawable.stream_ui_ic_play)
            val playIconDrawableTint: Int? = null

            val pauseIconDrawable = context.getDrawableCompat(R.drawable.stream_ui_ic_pause)
            val pauseIconDrawableTint: Int? = null

            val progressBarDrawable = context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)
            val progressBarDrawableTint: Int? = null
            val progressBarWidth = context.getDimension(R.dimen.stream_ui_audio_record_player_progress_bar_width)
            val progressBarHeight = context.getDimension(R.dimen.stream_ui_audio_record_player_progress_bar_height)

            val durationTextViewWidth = context.getDimension(R.dimen.stream_ui_audio_record_player_duration_text_view_width)
            val durationTextViewHeight = context.getDimension(R.dimen.stream_ui_audio_record_player_duration_text_view_height)
            val durationTextViewMarginStart = context.getDimension(R.dimen.stream_ui_audio_record_player_duration_text_view_margin_start)
            val durationTextStyle = TextStyle(
                size = context.getDimension(R.dimen.stream_ui_audio_record_player_duration_text_size),
                color = context.getColorCompat(R.color.stream_ui_audio_record_player_duration_text_color),
            )

            val waveBarHeight = context.getDimension(R.dimen.stream_ui_audio_record_player_wave_bar_height)
            val waveBarMarginStart = context.getDimension(R.dimen.stream_ui_audio_record_player_wave_bar_margin_start)
            val waveBarColorPlayed = context.getColorCompat(R.color.stream_ui_accent_blue)
            val waveBarColorFuture = context.getColorCompat(R.color.stream_ui_grey)

            val scrubberWidthDefault = context.getDimension(R.dimen.stream_ui_audio_record_player_scrubber_width_default)
            val scrubberWidthPressed = context.getDimension(R.dimen.stream_ui_audio_record_player_scrubber_width_pressed)
            val scrubberDrawable = context.getDrawableCompat(R.drawable.stream_ui_share_rectangle)
            val scrubberDrawableTint: Int? = null

            val fileIconContainerWidth = context.getDimension(R.dimen.stream_ui_audio_record_player_file_icon_container_width)
            val fileIconContainerVisible = context.resources.getBoolean(R.bool.stream_ui_audio_record_player_file_icon_container_visible)

            val audioFileIconDrawable = context.getDrawableCompat(R.drawable.stream_ui_ic_file_aac)

            val speedButtonWidth = context.getDimension(R.dimen.stream_ui_audio_record_player_speed_button_width)
            val speedButtonHeight = context.getDimension(R.dimen.stream_ui_audio_record_player_speed_button_height)
            val speedButtonElevation = context.getDimension(R.dimen.stream_ui_audio_record_player_speed_button_elevation)
            val speedButtonBackground = context.getDrawableCompat(R.drawable.stream_ui_literal_white_shape_16dp_corners)
            val speedButtonBackgroundTint: Int? = null
            val speedButtonTextStyle = TextStyle(
                size = context.getDimension(R.dimen.stream_ui_audio_record_player_speed_text_size),
                color = context.getColorCompat(R.color.stream_ui_audio_record_player_speed_text_color),
            )

            return AudioRecordPlayerViewStyle(
                height = height,
                padding = ViewPadding(
                    start = paddingStart,
                    top = paddingTop,
                    end = paddingEnd,
                    bottom = paddingBottom,
                ),
                backgroundDrawable = backgroundDrawable,
                backgroundDrawableTint = backgroundDrawableTint,
                // Playback Progress Container
                playbackProgressContainerSize = ViewSize(
                    width = playbackProgressContainerWidth,
                    height = playbackProgressContainerHeight,
                ),
                // Playback Button
                playbackButtonSize = ViewSize(
                    width = playbackButtonWidth,
                    height = playbackButtonHeight,
                ),
                playbackButtonPadding = ViewPadding(
                    start = playbackButtonPadding,
                    top = playbackButtonPadding,
                    end = playbackButtonPadding,
                    bottom = playbackButtonPadding,
                ),
                playbackButtonElevation = playbackButtonElevation,
                playbackButtonBackground = playbackButtonBackground,
                playbackButtonBackgroundTint = playbackButtonBackgroundTint,
                playIconDrawable = playIconDrawable,
                playIconDrawableTint = playIconDrawableTint,
                pauseIconDrawable = pauseIconDrawable,
                pauseIconDrawableTint = pauseIconDrawableTint,
                // Progress Bar
                progressBarDrawable = progressBarDrawable,
                progressBarDrawableTint = progressBarDrawableTint,
                progressBarSize = ViewSize(
                    width = progressBarWidth,
                    height = progressBarHeight,
                ),
                // Duration Text
                durationTextViewSize = ViewSize(
                    width = durationTextViewWidth,
                    height = durationTextViewHeight,
                ),
                durationTextMarginStart = durationTextViewMarginStart,
                durationTextStyle = durationTextStyle,
                // Wave Bar
                waveBarHeight = waveBarHeight,
                waveBarMarginStart = waveBarMarginStart,
                waveBarColorPlayed = waveBarColorPlayed,
                waveBarColorFuture = waveBarColorFuture,
                // Scrubber
                scrubberDrawable = scrubberDrawable,
                scrubberDrawableTint = scrubberDrawableTint,
                scrubberWidthDefault = scrubberWidthDefault,
                scrubberWidthPressed = scrubberWidthPressed,
                // File Icon Container
                fileIconContainerWidth = fileIconContainerWidth,
                isFileIconContainerVisible = fileIconContainerVisible,
                audioFileIconDrawable = audioFileIconDrawable,
                // Speed Button
                speedButtonSize = ViewSize(
                    width = speedButtonWidth,
                    height = speedButtonHeight,
                ),
                speedButtonElevation = speedButtonElevation,
                speedButtonBackground = speedButtonBackground,
                speedButtonBackgroundTint = speedButtonBackgroundTint,
                speedButtonTextStyle = speedButtonTextStyle,
            )
        }
    }
}
