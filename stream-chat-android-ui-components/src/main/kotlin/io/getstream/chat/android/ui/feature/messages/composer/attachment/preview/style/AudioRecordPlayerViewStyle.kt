package io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.messages.list.background.ShapeAppearanceModelFactory
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.ViewPadding
import io.getstream.chat.android.ui.helper.ViewSize
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

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

    internal companion object {

        operator fun invoke(context: Context, attrs: AttributeSet?): AudioRecordPlayerViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AudioRecordPlayerView,
                R.attr.streamUiAudioRecordPlayerViewStyle,
                R.style.StreamUi_AudioRecordPlayerView,
            ).use {
                return invoke(context, it)
            }
        }

        operator fun invoke(context: Context, attributes: TypedArray?): AudioRecordPlayerViewStyle {

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