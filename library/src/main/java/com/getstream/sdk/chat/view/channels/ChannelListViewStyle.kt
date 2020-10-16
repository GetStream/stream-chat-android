package com.getstream.sdk.chat.view.channels

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.style.TextStyle
import com.getstream.sdk.chat.view.BaseStyle

class ChannelListViewStyle(context: Context, attrs: AttributeSet?) : BaseStyle() {

    @JvmField
    @LayoutRes
    var channelPreviewLayout: Int

    @JvmField
    val channelTitleText: TextStyle

    @JvmField
    val channelTitleUnreadText: TextStyle

    @JvmField
    val lastMessage: TextStyle

    @JvmField
    val lastMessageUnread: TextStyle

    @JvmField
    val lastMessageDateText: TextStyle

    @JvmField
    val lastMessageDateUnreadText: TextStyle

    private var channelWithoutNameText = ""

    fun getChannelWithoutNameText(): String = channelWithoutNameText.takeIf(String::isNotBlank) ?: context.getString(R.string.stream_channel_unknown_title)

    fun setAvatarBorderColor(@ColorRes color: Int) {
        avatarBorderColor = color
    }

    init {
        setContext(context)
        val attributes = context.obtainStyledAttributes(
            attrs,
            R.styleable.ChannelListView,
            0,
            0
        )

        channelTitleText = TextStyle.Builder(attributes).apply {
            size(R.styleable.ChannelListView_streamChannelTitleTextSize, getDimension(R.dimen.stream_channel_item_title))
            color(R.styleable.ChannelListView_streamChannelTitleTextColor, getColor(R.color.stream_channel_item_text_color))
            font(R.styleable.ChannelListView_streamChannelTitleTextFontAssets, R.styleable.ChannelListView_streamChannelTitleTextFont)
            style(R.styleable.ChannelListView_streamChannelTitleTextStyle, Typeface.BOLD)
        }.build()

        channelTitleUnreadText = TextStyle.Builder(attributes).apply {
            size(R.styleable.ChannelListView_streamChannelTitleTextSize, getDimension(R.dimen.stream_channel_item_title))
            color(R.styleable.ChannelListView_streamChannelTitleUnreadTextColor, getColor(R.color.stream_channel_item_text_color))
            font(R.styleable.ChannelListView_streamChannelTitleTextFontAssets, R.styleable.ChannelListView_streamChannelTitleTextFont)
            style(R.styleable.ChannelListView_streamChannelTitleUnreadTextStyle, Typeface.BOLD)
        }.build()

        attributes.getString(R.styleable.ChannelListView_streamChannelWithOutNameTitleText)?.let { channelWithoutNameText = it }

        lastMessage = TextStyle.Builder(attributes).apply {
            size(R.styleable.ChannelListView_streamLastMessageTextSize, getDimension(R.dimen.stream_channel_item_message))
            color(R.styleable.ChannelListView_streamLastMessageTextColor, getColor(R.color.stream_gray_dark))
            font(R.styleable.ChannelListView_streamLastMessageTextFontAssets, R.styleable.ChannelListView_streamLastMessageTextFont)
            style(R.styleable.ChannelListView_streamLastMessageTextStyle, Typeface.NORMAL)
        }.build()

        lastMessageUnread = TextStyle.Builder(attributes).apply {
            size(R.styleable.ChannelListView_streamLastMessageTextSize, getDimension(R.dimen.stream_channel_item_message))
            color(R.styleable.ChannelListView_streamLastMessageUnreadTextColor, getColor(R.color.stream_channel_item_text_color))
            font(R.styleable.ChannelListView_streamLastMessageTextFontAssets, R.styleable.ChannelListView_streamLastMessageTextFont)
            style(R.styleable.ChannelListView_streamLastMessageUnreadTextStyle, Typeface.BOLD)
        }.build()

        lastMessageDateText = TextStyle.Builder(attributes).apply {
            size(R.styleable.ChannelListView_streamLastMessageDateTextSize, getDimension(R.dimen.stream_channel_item_message_date))
            color(R.styleable.ChannelListView_streamLastMessageDateTextColor, getColor(R.color.stream_gray_dark))
            font(R.styleable.ChannelListView_streamLastMessageDateTextFontAssets, R.styleable.ChannelListView_streamLastMessageDateTextFont)
            style(R.styleable.ChannelListView_streamLastMessageDateTextStyle, Typeface.NORMAL)
        }.build()

        lastMessageDateUnreadText = TextStyle.Builder(attributes).apply {
            size(R.styleable.ChannelListView_streamLastMessageDateTextSize, getDimension(R.dimen.stream_channel_item_message_date))
            color(R.styleable.ChannelListView_streamLastMessageDateTextColor, getColor(R.color.stream_channel_item_text_color))
            font(R.styleable.ChannelListView_streamLastMessageDateTextFontAssets, R.styleable.ChannelListView_streamLastMessageDateTextFont)
            style(R.styleable.ChannelListView_streamLastMessageDateUnreadTextStyle, Typeface.BOLD)
        }.build()

        with(attributes) {
            channelPreviewLayout = getResourceId(R.styleable.ChannelListView_streamChannelPreviewLayout, R.layout.stream_item_channel)
            // Avatar
            avatarWidth = getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarWidth, getDimension(R.dimen.stream_channel_avatar_width))
            avatarHeight = getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarHeight, getDimension(R.dimen.stream_channel_avatar_height))
            avatarBorderWidth = getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarBorderWidth, getDimension(R.dimen.stream_channel_avatar_border_width))
            avatarBorderColor = getColor(R.styleable.ChannelListView_streamAvatarBorderColor, Color.WHITE)
            avatarBackGroundColor = getColor(R.styleable.ChannelListView_streamAvatarBackGroundColor, getColor(R.color.stream_gray_dark))
            avatarInitialText = TextStyle.Builder(attributes).apply {
                size(R.styleable.ChannelListView_streamAvatarTextSize, getDimension(R.dimen.stream_channel_initials))
                color(R.styleable.ChannelListView_streamAvatarTextColor, Color.WHITE)
                font(R.styleable.ChannelListView_streamAvatarTextFontAssets, R.styleable.ChannelListView_streamAvatarTextFont)
                style(R.styleable.ChannelListView_streamAvatarTextStyle, Typeface.BOLD)
            }.build()

            // Read State
            showReadState = getBoolean(R.styleable.ChannelListView_streamShowReadState, true)
            readStateAvatarWidth = getDimensionPixelSize(R.styleable.ChannelListView_streamReadStateAvatarWidth, getDimension(R.dimen.stream_read_state_avatar_width))
            readStateAvatarHeight = getDimensionPixelSize(R.styleable.ChannelListView_streamReadStateAvatarHeight, getDimension(R.dimen.stream_read_state_avatar_height))
            readStateText = TextStyle.Builder(attributes).apply {
                size(R.styleable.ChannelListView_streamReadStateTextSize, getDimension(R.dimen.stream_read_state_text_size))
                color(R.styleable.ChannelListView_streamReadStateTextColor, Color.BLACK)
                font(R.styleable.ChannelListView_streamReadStateTextFontAssets, R.styleable.ChannelListView_streamReadStateTextFont)
                style(R.styleable.ChannelListView_streamReadStateTextStyle, Typeface.BOLD)
            }.build()

            recycle()
        }
    }
}
