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
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.use

public data class PollViewStyle(
    public val pollTitleTextStyle: TextStyle,
    public val pollSubtitleTextStyle: TextStyle,
    public val pollOptionCheckDrawable: Drawable,
    public val pollOptionTextStyle: TextStyle,
    public val pollOptionVotesTextStyle: TextStyle,
    public val pollCloseTextStyle: TextStyle,
    public val pollResultsTextStyle: TextStyle,
    public val pollShowAllOptionsTextStyle: TextStyle,
) : ViewStyle {

    internal companion object {
        @Suppress("LongMethod")
        internal operator fun invoke(context: Context, attrs: AttributeSet?): PollViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.PollView,
                R.attr.streamUiMessageListPollStyle,
                R.style.StreamUi_MessageList_Poll,
            ).use { a ->
                val pollTitleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.PollView_streamUiPollTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large),
                    )
                    .color(
                        R.styleable.PollView_streamUiPollTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.PollView_streamUiPollTitleFontAssets,
                        R.styleable.PollView_streamUiPollTitleTextFont,
                    )
                    .style(
                        R.styleable.PollView_streamUiPollTitleTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val pollOptionCheckDrawable = a.getDrawable(R.styleable.PollView_streamUiPollOptionCheckDrawable)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_poll_option_selector)!!

                val pollSubtitleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.PollView_streamUiPollSubtitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.PollView_streamUiPollSubtitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary),
                    )
                    .font(
                        R.styleable.PollView_streamUiPollSubtitleFontAssets,
                        R.styleable.PollView_streamUiPollSubtitleTextFont,
                    )
                    .style(
                        R.styleable.PollView_streamUiPollSubtitleTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val pollOptionTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.PollView_streamUiPollOptionTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.PollView_streamUiPollOptionTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.PollView_streamUiPollOptionFontAssets,
                        R.styleable.PollView_streamUiPollOptionTextFont,
                    )
                    .style(
                        R.styleable.PollView_streamUiPollOptionTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val pollVotesTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.PollView_streamUiPollVotesTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium),
                    )
                    .color(
                        R.styleable.PollView_streamUiPollVotesTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary),
                    )
                    .font(
                        R.styleable.PollView_streamUiPollVotesFontAssets,
                        R.styleable.PollView_streamUiPollVotesTextFont,
                    )
                    .style(
                        R.styleable.PollView_streamUiPollVotesTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val pollCloseTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.PollView_streamUiPollCloseTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large),
                    )
                    .color(
                        R.styleable.PollView_streamUiPollCloseTextColor,
                        context.getColorCompat(R.color.stream_ui_accent_blue),
                    )
                    .font(
                        R.styleable.PollView_streamUiPollCloseFontAssets,
                        R.styleable.PollView_streamUiPollCloseTextFont,
                    )
                    .style(
                        R.styleable.PollView_streamUiPollCloseTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val pollResultsTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.PollView_streamUiPollResultsTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large),
                    )
                    .color(
                        R.styleable.PollView_streamUiPollResultsTextColor,
                        context.getColorCompat(R.color.stream_ui_accent_blue),
                    )
                    .font(
                        R.styleable.PollView_streamUiPollResultsFontAssets,
                        R.styleable.PollView_streamUiPollResultsTextFont,
                    )
                    .style(
                        R.styleable.PollView_streamUiPollResultsTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()

                val pollShowAllOptionsTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.PollView_streamUiPollShowAllOptionsTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large),
                    )
                    .color(
                        R.styleable.PollView_streamUiPollShowAllOptionsTextColor,
                        context.getColorCompat(R.color.stream_ui_accent_blue),
                    )
                    .font(
                        R.styleable.PollView_streamUiPollShowAllOptionsFontAssets,
                        R.styleable.PollView_streamUiPollShowAllOptionsTextFont,
                    )
                    .style(
                        R.styleable.PollView_streamUiPollShowAllOptionsTextStyle,
                        Typeface.NORMAL,
                    )
                    .build()
                return PollViewStyle(
                    pollTitleTextStyle = pollTitleTextStyle,
                    pollSubtitleTextStyle = pollSubtitleTextStyle,
                    pollOptionCheckDrawable = pollOptionCheckDrawable,
                    pollOptionTextStyle = pollOptionTextStyle,
                    pollOptionVotesTextStyle = pollVotesTextStyle,
                    pollCloseTextStyle = pollCloseTextStyle,
                    pollResultsTextStyle = pollResultsTextStyle,
                    pollShowAllOptionsTextStyle = pollShowAllOptionsTextStyle,
                ).let(TransformStyle.pollViewStyleTransformer::transform)
            }
        }
    }
}
