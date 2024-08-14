package io.getstream.chat.docs.kotlin.ui.guides

import android.content.Context
import android.graphics.Color
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.utils.Utils
import io.getstream.chat.android.ui.feature.messages.common.AudioRecordPlayerViewStyle
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.StyleTransformer
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewPadding
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat

@OptIn(InternalStreamChatApi::class)
class AddingRecordingSupport {

    fun customizeUiInMessageComposer(context: Context) {
        TransformStyle.messageComposerStyleTransformer = StyleTransformer { defaultStyle ->
            defaultStyle.copy(
                audioRecordingHoldToRecordText = context.getString(R.string.stream_ui_message_composer_hold_to_record),
                audioRecordingHoldToRecordTextStyle = TextStyle(
                    size = context.getDimension(R.dimen.stream_ui_text_medium),
                    color = context.getColorCompat(R.color.stream_ui_text_color_secondary),
                ),
                audioRecordingHoldToRecordBackgroundDrawable = context.getDrawableCompat(R.drawable.stream_ui_message_composer_audio_record_hold_background)!!,
                audioRecordingHoldToRecordBackgroundDrawableTint = null,
                audioRecordingSlideToCancelText = context.getString(R.string.stream_ui_message_composer_slide_to_cancel),
                audioRecordingSlideToCancelTextStyle = TextStyle(
                    size = context.getDimension(R.dimen.stream_ui_text_medium),
                    color = context.getColorCompat(R.color.stream_ui_text_color_secondary),
                ),
                audioRecordingSlideToCancelStartDrawable = context.getDrawableCompat(R.drawable.stream_ui_arrow_left)!!,
                audioRecordingSlideToCancelStartDrawableTint = null,
                audioRecordingFloatingButtonIconDrawable = context.getDrawableCompat(R.drawable.stream_ui_ic_mic)!!,
                audioRecordingFloatingButtonIconDrawableTint = null,
                audioRecordingFloatingButtonBackgroundDrawable = context.getDrawableCompat(R.drawable.stream_ui_message_composer_audio_record_mic_background)!!,
                audioRecordingFloatingButtonBackgroundDrawableTint = null,
                audioRecordingFloatingLockIconDrawable = context.getDrawableCompat(R.drawable.stream_ui_ic_mic_lock)!!,
                audioRecordingFloatingLockIconDrawableTint = null,
                audioRecordingFloatingLockedIconDrawable = context.getDrawableCompat(R.drawable.stream_ui_ic_mic_locked)!!,
                audioRecordingFloatingLockedIconDrawableTint = null,
                audioRecordingWaveformColor = context.getColorCompat(R.color.stream_ui_accent_blue),
                audioRecordingButtonIconDrawable = context.getDrawableCompat(R.drawable.stream_ui_ic_mic)!!,
                audioRecordingButtonIconTintList = null,
                audioRecordingButtonWidth = context.getDimension(R.dimen.stream_ui_message_composer_trailing_content_button_record_audio_width),
                audioRecordingButtonHeight = context.getDimension(R.dimen.stream_ui_message_composer_trailing_content_button_record_audio_height),
                audioRecordingButtonPadding = context.getDimension(R.dimen.stream_ui_message_composer_trailing_content_button_record_audio_padding),
            )
        }
    }

    fun customizePlayerUiInApp(context: Context) {
        TransformStyle.audioRecordPlayerViewStyle = StyleTransformer { defaultStyle ->
            defaultStyle.copy(
                backgroundDrawableTint = Color.YELLOW,
                playIconDrawableTint = Color.BLACK,
                waveBarColorPlayed = Color.BLACK,
                waveBarColorFuture = Color.LTGRAY,
                scrubberDrawableTint = Color.BLACK,
                durationTextStyle = TextStyle(
                    size = context.getDimension(R.dimen.stream_ui_text_medium),
                    color = Color.BLACK,
                ),
                isFileIconContainerVisible = false,
                padding = ViewPadding(
                    start = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_start),
                    top = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_top),
                    end = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_end),
                    bottom = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_bottom)
                ),
                progressBarDrawableTint = Color.RED
            )
        }
    }

    fun customizePlayerUiInMessageComposer(context: Context) {
        TransformStyle.messageComposerStyleTransformer = StyleTransformer { defaultStyle ->
            defaultStyle.copy(
                audioRecordPlayerViewStyle = AudioRecordPlayerViewStyle.default(context).copy(
                    backgroundDrawableTint = Color.YELLOW,
                    playIconDrawableTint = Color.BLACK,
                    waveBarColorPlayed = Color.BLACK,
                    waveBarColorFuture = Color.LTGRAY,
                    scrubberDrawableTint = Color.BLACK,
                    durationTextStyle = TextStyle(
                        size = context.getDimension(R.dimen.stream_ui_text_medium),
                        color = Color.BLACK,
                    ),
                    isFileIconContainerVisible = false,
                    padding = ViewPadding(
                        start = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_start),
                        top = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_top),
                        end = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_end),
                        bottom = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_bottom)
                    ),
                    progressBarDrawableTint = Color.RED
                )
            )
        }
    }

    fun customizePlayerUiInMessageList(context: Context) {
        TransformStyle.messageListStyleTransformer = StyleTransformer { defaultStyle ->
            defaultStyle.copy(
                audioRecordPlayerViewStyle = defaultStyle.audioRecordPlayerViewStyle.copy(
                    own = AudioRecordPlayerViewStyle.default(context).copy(
                        backgroundDrawableTint = Color.YELLOW,
                        playIconDrawableTint = Color.BLACK,
                        waveBarColorPlayed = Color.BLACK,
                        waveBarColorFuture = Color.LTGRAY,
                        scrubberDrawableTint = Color.BLACK,
                        durationTextStyle = TextStyle(
                            size = context.getDimension(R.dimen.stream_ui_text_medium),
                            color = Color.BLACK,
                        ),
                        isFileIconContainerVisible = false,
                        padding = ViewPadding(
                            start = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_start),
                            top = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_top),
                            end = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_end),
                            bottom = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_bottom)
                        ),
                        progressBarDrawableTint = Color.RED
                    ),
                    theirs = AudioRecordPlayerViewStyle.default(context).copy(
                        backgroundDrawableTint = Color.BLACK,
                        playIconDrawableTint = Color.BLACK,
                        waveBarColorPlayed = Color.WHITE,
                        waveBarColorFuture = Color.GRAY,
                        scrubberDrawableTint = Color.WHITE,
                        durationTextStyle = TextStyle(
                            size = context.getDimension(R.dimen.stream_ui_text_medium),
                            color = Color.WHITE,
                        ),
                        isFileIconContainerVisible = false,
                        padding = ViewPadding(
                            start = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_start),
                            top = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_top),
                            end = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_end),
                            bottom = context.getDimension(R.dimen.stream_ui_audio_record_player_padding_bottom)
                        ),
                        progressBarDrawableTint = Color.RED
                    ),
                )
            )
        }
    }

}