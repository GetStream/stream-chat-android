package com.getstream.sdk.chat.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.style.TextStyle
import com.getstream.sdk.chat.view.messages.AvatarStyle

internal class ChannelHeaderViewStyle(context: Context, attrs: AttributeSet?) {
    var channelTitleText: TextStyle
    var lastActiveText: TextStyle
    val isBackButtonShow: Boolean
    val isLastActiveShow: Boolean
    val isAvatarGroupShow: Boolean
    val isOptionsButtonShow: Boolean
    val optionsButtonTextSize: Int
    val optionsButtonWidth: Int
    val optionsButtonHeight: Int
    val isActiveBadgeShow: Boolean
    val backButtonBackground: Drawable?
    val optionsButtonBackground: Drawable?
    val offlineText: String
    val channelWithoutNameText: String
    val avatarStyle: AvatarStyle = AvatarStyle()

    private val res = context.resources

    init {
        val attributes = context.obtainStyledAttributes(
            attrs,
            R.styleable.ChannelHeaderView,
            0,
            0
        )

        channelTitleText = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextSize,
                res.getDimensionPixelSize(R.dimen.stream_channel_header_initials)
            )
            color(
                R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_channel_initials)
            )
            style(R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextStyle, Typeface.BOLD)
            font(
                R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextFontAssets,
                R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextFont
            )
        }.build()

        lastActiveText = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextSize,
                res.getDimensionPixelSize(R.dimen.stream_channel_preview_date)
            )
            color(
                R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextColor,
                ContextCompat.getColor(context, R.color.stream_gray_dark)
            )
            font(
                R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextFontAssets,
                R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextFont
            )
            style(
                R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextStyle,
                Typeface.NORMAL
            )
        }.build()

        with(attributes) {
            channelWithoutNameText =
                getString(R.styleable.ChannelHeaderView_streamChannelWithOutNameTitleText)?.takeIf(
                    String::isNotBlank
                ) ?: context.getString(R.string.stream_channel_unknown_title)
            offlineText =
                getString(R.styleable.ChannelHeaderView_streamChannelHeaderOfflineText)?.takeIf(
                    String::isNotBlank
                ) ?: context.getString(R.string.stream_channel_offlineText)

            // Avatar
            avatarStyle.avatarWidth = getDimensionPixelSize(
                R.styleable.ChannelHeaderView_streamAvatarWidth,
                res.getDimensionPixelSize(R.dimen.stream_channel_avatar_width)
            )
            avatarStyle.avatarHeight = getDimensionPixelSize(
                R.styleable.ChannelHeaderView_streamAvatarHeight,
                res.getDimensionPixelSize(R.dimen.stream_channel_avatar_height)
            )
            avatarStyle.avatarBorderWidth = getDimensionPixelSize(
                R.styleable.ChannelHeaderView_streamAvatarBorderWidth,
                res.getDimensionPixelSize(R.dimen.stream_channel_avatar_border_width)
            )
            avatarStyle.avatarBorderColor =
                getColor(R.styleable.ChannelHeaderView_streamAvatarBorderColor, Color.WHITE)
            avatarStyle.avatarBackGroundColor = getColor(
                R.styleable.ChannelHeaderView_streamAvatarBackGroundColor,
                ContextCompat.getColor(context, R.color.stream_gray_dark)
            )

            avatarStyle.avatarInitialText = TextStyle.Builder(attributes).apply {
                size(
                    R.styleable.ChannelHeaderView_streamAvatarTextSize,
                    res.getDimensionPixelSize(R.dimen.stream_channel_initials)
                )
                color(R.styleable.ChannelHeaderView_streamAvatarTextColor, Color.WHITE)
                font(
                    R.styleable.ChannelHeaderView_streamAvatarTextFontAssets,
                    R.styleable.ChannelHeaderView_streamAvatarTextFont
                )
                style(R.styleable.ChannelHeaderView_streamAvatarTextStyle, Typeface.BOLD)
            }.build()

            isLastActiveShow =
                getBoolean(R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveShow, true)

            // Back Button
            isBackButtonShow =
                getBoolean(R.styleable.ChannelHeaderView_streamChannelHeaderBackButtonShow, true)
            backButtonBackground =
                getDrawable(R.styleable.ChannelHeaderView_streamChannelHeaderBackButtonBackground)
                    ?: ContextCompat.getDrawable(context, R.drawable.stream_arrow_left)

            // Avatar
            isAvatarGroupShow =
                getBoolean(R.styleable.ChannelHeaderView_streamChannelHeaderAvatarShow, true)

            // Badge
            isActiveBadgeShow =
                getBoolean(R.styleable.ChannelHeaderView_streamChannelHeaderActiveBadgeShow, true)

            // Options
            isOptionsButtonShow = getBoolean(
                R.styleable.ChannelHeaderView_streamChannelHeaderOptionsButtonShow,
                false
            )
            optionsButtonBackground =
                getDrawable(R.styleable.ChannelHeaderView_streamChannelHeaderOptionsButtonBackground)
                    ?: ContextCompat.getDrawable(context, R.drawable.stream_ic_settings)
            optionsButtonTextSize = getDimension(
                R.styleable.ChannelHeaderView_streamChannelHeaderOptionsButtonTextSize,
                res.getDimensionPixelSize(R.dimen.stream_channel_header_initials).toFloat()
            ).toInt()
            optionsButtonWidth = getDimensionPixelSize(
                R.styleable.ChannelHeaderView_streamChannelHeaderOptionsButtonWidth,
                res.getDimensionPixelSize(R.dimen.stream_channel_avatar_width)
            )
            optionsButtonHeight = getDimensionPixelSize(
                R.styleable.ChannelHeaderView_streamChannelHeaderOptionsButtonHeight,
                res.getDimensionPixelSize(R.dimen.stream_channel_avatar_height)
            )
            recycle()
        }
    }
}
