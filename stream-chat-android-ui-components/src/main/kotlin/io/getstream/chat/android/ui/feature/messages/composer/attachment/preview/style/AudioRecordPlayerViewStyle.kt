package io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.style

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.messages.list.background.ShapeAppearanceModelFactory
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.ViewPadding
import io.getstream.chat.android.ui.helper.ViewSize
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.dpToPx

public data class AudioRecordPlayerViewStyle(
    @Px public val height: Int,
    public val padding: ViewPadding,
    public val backgroundBackground: Drawable?,
    @ColorInt public val backgroundBackgroundTint: Int?,
    public val playbackProgressContainerSize: ViewSize,
    public val playbackButtonSize: ViewSize,
    public val playbackButtonBackground: Drawable?,
    public val progressBarDrawable: Drawable?,
    public val progressBarSize: ViewSize,
    public val playIconDrawable: Drawable?,
    @ColorInt public val playIconDrawableTint: Int?,
    public val pauseIconDrawable: Drawable?,
    @ColorInt public val pauseIconDrawableTint: Int?,
    public val speedButtonTextStyle: TextStyle,
    public val speedButtonBackground: Drawable?,
    public val speedButtonSize: ViewSize,
    public val audioIconDrawable: Drawable?,
    public val durationTextViewSize: ViewSize,
    @Px public val durationTextMarginStart: Int,
    public val durationTextStyle: TextStyle,
    @Px public val waveBarHeight: Int,
    @Px public val waveBarMarginStart: Int,
    @ColorInt public val playedWaveBarColor: Int,
    @ColorInt public val futureWaveBarColor: Int,
    public val scrubberDrawable: Drawable?,
    @ColorInt public val scrubberDrawableTint: Int?,
    @Px public val defaultScrubberWidth: Int,
    @Px public val pressedScrubberWidth: Int,
    public val isFileIconContainerVisible: Boolean,
) : ViewStyle {

    val tintedBackgroundDrawable: Drawable?
        get() = backgroundBackground?.applyTint(backgroundBackgroundTint)

    val tintedPlayIconDrawable: Drawable?
        get() = playIconDrawable?.applyTint(playIconDrawableTint)

    val tintedPauseIconDrawable: Drawable?
        get() = pauseIconDrawable?.applyTint(pauseIconDrawableTint)

    val tintedScrubberDrawable: Drawable?
        get() = scrubberDrawable?.applyTint(scrubberDrawableTint)

    internal companion object {
        operator fun invoke(context: Context, attributes: TypedArray?): AudioRecordPlayerViewStyle {

            return AudioRecordPlayerViewStyle(
                height = 60.dpToPx(),
                padding = ViewPadding(
                    start = 8.dpToPx(),
                    top = 2.dpToPx(),
                    end = 8.dpToPx(),
                    bottom = 2.dpToPx(),
                ),
                backgroundBackground = ShapeAppearanceModelFactory.audioBackground(context),
                backgroundBackgroundTint = Color.YELLOW,
                playbackProgressContainerSize = ViewSize(44.dpToPx(), 44.dpToPx()),
                playbackButtonSize = ViewSize(36.dpToPx(), 36.dpToPx()),
                playbackButtonBackground = ContextCompat.getDrawable(
                    context,
                    R.drawable.stream_ui_white_shape_circular
                ),
                progressBarDrawable = ContextCompat.getDrawable(
                    context,
                    R.drawable.stream_ui_rotating_indeterminate_progress_gradient
                ),
                progressBarSize = ViewSize(36.dpToPx(), 36.dpToPx()),
                playIconDrawable = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_play),
                playIconDrawableTint = Color.RED,
                pauseIconDrawable = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_pause),
                pauseIconDrawableTint = Color.BLUE,
                speedButtonTextStyle = TextStyle(
                    size = 14.dpToPx(),
                    color = 0xFF080707.toInt(),
                ),
                speedButtonBackground = ContextCompat.getDrawable(
                    context,
                    R.drawable.stream_ui_literal_white_shape_16dp_corners
                ),
                speedButtonSize = ViewSize(36.dpToPx(), 32.dpToPx()),
                audioIconDrawable = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_file_aac),
                durationTextViewSize = ViewSize(48.dpToPx(), ViewGroup.LayoutParams.MATCH_PARENT),
                durationTextMarginStart = 0.dpToPx(),
                durationTextStyle = TextStyle(
                    size = 14.dpToPx(),
                    color = 0xFF747881.toInt(),
                ),
                waveBarHeight = 36.dpToPx(),
                waveBarMarginStart = 0.dpToPx(),
                playedWaveBarColor = ContextCompat.getColor(context, R.color.stream_ui_accent_blue),
                futureWaveBarColor = ContextCompat.getColor(context, R.color.stream_ui_grey),
                scrubberDrawable = ContextCompat.getDrawable(context, R.drawable.stream_ui_share_rectangle),
                scrubberDrawableTint = Color.MAGENTA,
                defaultScrubberWidth = 7.dpToPx(),
                pressedScrubberWidth = 10.dpToPx(),
                isFileIconContainerVisible = false,
            )
        }
    }
}