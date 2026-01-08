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

package io.getstream.chat.android.ui.feature.messages.list.adapter.view

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal.MediaAttachmentView
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getColorOrNull
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDimensionOrNull
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Style for [MediaAttachmentView].
 * Use this class together with [TransformStyle.mediaAttachmentStyleTransformer] to change styles programmatically.
 *
 * @param progressIcon Animated progress drawable. Default value is
 * [R.drawable.stream_ui_rotating_indeterminate_progress_gradient].
 * @param placeholderIcon Displayed while the media preview is Loading.
 * @param placeholderIconTint The tint applied to the placeholder icon displayed before a media
 * attachment preview was loaded or after loading had failed.
 * Default value is [R.drawable.stream_ui_picture_placeholder].
 * @param mediaPreviewBackgroundColor Controls the background color of image and video attachment previews.
 * Default value is [R.color.stream_ui_grey].
 * @param moreCountOverlayColor More count semi-transparent overlay color. Default value is [R.color.stream_ui_overlay].
 * @param moreCountTextStyle Appearance for "more count" text.
 * @param playVideoIcon The icon overlaid above previews of video attachments.
 * @param playVideoIconTint The tint of the play video icon.
 * Default value is [R.drawable.stream_ui_ic_play]
 * @param playVideoIconBackgroundColor Applies a background colour to the View hosting the play video icon.
 * Default value is [R.color.stream_ui_literal_white]
 * @param playVideoIconElevation Determines the elevation of the play video button.
 * @param playVideoIconPaddingTop Determines the padding set between the top of the play video icon and its
 * parent.
 * @param playVideoIconPaddingBottom Determines the padding set between the bottom of the play video icon and its
 * parent.
 * @param playVideoIconPaddingStart Determines the padding set between the start of the play video icon and its
 * parent.
 * @param playVideoIconPaddingEnd Determines the padding set between the end of the play video icon and its
 * parent.
 * @param playVideoIconCornerRadius Determines the corner radius of the play video icon.
 */
public data class MediaAttachmentViewStyle(
    public val progressIcon: Drawable,
    public val placeholderIcon: Drawable,
    @ColorInt public val placeholderIconTint: Int?,
    @ColorInt val mediaPreviewBackgroundColor: Int,
    @ColorInt val moreCountOverlayColor: Int,
    public val moreCountTextStyle: TextStyle,
    public val playVideoIcon: Drawable?,
    @ColorInt val playVideoIconTint: Int?,
    @ColorInt public val playVideoIconBackgroundColor: Int,
    public val playVideoIconElevation: Float,
    public val playVideoIconPaddingTop: Int,
    public val playVideoIconPaddingBottom: Int,
    public val playVideoIconPaddingStart: Int,
    public val playVideoIconPaddingEnd: Int,
    public val playVideoIconCornerRadius: Float,
) : ViewStyle {
    internal companion object {
        /**
         * Fetches styled attributes and returns them wrapped inside of [MediaAttachmentViewStyle].
         */
        @Suppress("LongMethod")
        operator fun invoke(context: Context, attrs: AttributeSet?): MediaAttachmentViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MediaAttachmentView,
                R.attr.streamUiMessageListMediaAttachmentStyle,
                R.style.StreamUi_MessageList_MediaAttachment,
            ).use { a ->
                val progressIcon = a.getDrawable(R.styleable.MediaAttachmentView_streamUiMediaAttachmentProgressIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_rotating_indeterminate_progress_gradient)!!

                val mediaPreviewBackgroundColor = a.getColor(
                    R.styleable.MediaAttachmentView_streamUiMediaAttachmentMediaPreviewBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_message_list_image_attachment_background),
                )

                val moreCountOverlayColor = a.getColor(
                    R.styleable.MediaAttachmentView_streamUiMediaAttachmentMoreCountOverlayColor,
                    context.getColorCompat(R.color.stream_ui_overlay),
                )

                val moreCountTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentMoreCountTextSize,
                        context.getDimension(R.dimen.stream_ui_message_image_attachment_more_count_text_size),
                    )
                    .color(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentMoreCountTextColor,
                        context.getColorCompat(R.color.stream_ui_literal_white),
                    )
                    .font(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentMoreCountFontAssets,
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentMoreCountTextFont,
                    )
                    .style(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentMoreCountTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val placeholderIcon =
                    a.getDrawable(R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlaceHolderIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_picture_placeholder)!!

                val placeholderIconTint = a.getColorOrNull(
                    R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlaceHolderIconTint,
                )

                val playVideoIcon = a.getDrawable(R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlayVideoIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_play)

                val playVideoIconTint = a.getColorOrNull(
                    R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlayVideoIconTint,
                )

                val playVideoIconBackgroundColor =
                    a.getColor(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlayVideoIconBackgroundColor,
                        context.getColorCompat(R.color.stream_ui_literal_white),
                    )

                val playVideoIconCornerRadius =
                    a.getDimension(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlayVideoIconCornerRadius,
                        0f,
                    )

                val playVideoIconElevation =
                    a.getDimension(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlayVideoIconElevation,
                        0f,
                    )

                val playVideoIconPaddingTop =
                    a.getDimensionPixelSize(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlayVideoIconPaddingTop,
                        0,
                    )

                val playVideoIconPaddingBottom =
                    a.getDimensionPixelSize(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlayVideoIconPaddingBottom,
                        0,
                    )

                val playVideoIconPaddingStart =
                    a.getDimensionPixelSize(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlayVideoIconPaddingStart,
                        0,
                    )

                val playVideoIconPaddingEnd =
                    a.getDimensionPixelSize(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlayVideoIconPaddingEnd,
                        0,
                    )

                val playVideoIconPadding =
                    a.getDimensionOrNull(
                        R.styleable.MediaAttachmentView_streamUiMediaAttachmentPlayVideoIconPadding,
                    )?.toInt()

                return MediaAttachmentViewStyle(
                    progressIcon = progressIcon,
                    mediaPreviewBackgroundColor = mediaPreviewBackgroundColor,
                    moreCountOverlayColor = moreCountOverlayColor,
                    moreCountTextStyle = moreCountTextStyle,
                    placeholderIcon = placeholderIcon,
                    placeholderIconTint = placeholderIconTint,
                    playVideoIcon = playVideoIcon,
                    playVideoIconTint = playVideoIconTint,
                    playVideoIconBackgroundColor = playVideoIconBackgroundColor,
                    playVideoIconElevation = playVideoIconElevation,
                    playVideoIconPaddingTop = playVideoIconPadding ?: playVideoIconPaddingTop,
                    playVideoIconPaddingBottom = playVideoIconPadding ?: playVideoIconPaddingBottom,
                    playVideoIconPaddingStart = playVideoIconPadding ?: playVideoIconPaddingStart,
                    playVideoIconPaddingEnd = playVideoIconPadding ?: playVideoIconPaddingEnd,
                    playVideoIconCornerRadius = playVideoIconCornerRadius,
                ).let(TransformStyle.mediaAttachmentStyleTransformer::transform)
            }
        }
    }
}
