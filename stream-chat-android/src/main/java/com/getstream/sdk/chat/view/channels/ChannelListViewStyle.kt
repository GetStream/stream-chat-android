package com.getstream.sdk.chat.view.channels

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.style.TextStyle
import com.getstream.sdk.chat.view.ReadStateStyle
import com.getstream.sdk.chat.view.messages.AvatarStyle

public class ChannelListViewStyle(context: Context, attrs: AttributeSet?) {

    @LayoutRes
    public val channelPreviewLayout: Int
    public val channelTitleText: TextStyle
    public val channelTitleUnreadText: TextStyle
    public val lastMessage: TextStyle
    public val lastMessageUnread: TextStyle
    public val lastMessageDateText: TextStyle
    public val lastMessageDateUnreadText: TextStyle
    public var avatarStyle: AvatarStyle
    public val readStateStyle: ReadStateStyle
    public val itemSeparatorDrawable: Drawable?

    private val resources = context.resources

    private var channelWithoutNameText = ""

    public fun getChannelWithoutNameText(): String =
        channelWithoutNameText.takeIf(String::isNotBlank)
            ?: resources.getString(R.string.stream_channel_unknown_title)

    public var avatarBorderColor: Int
        get() = avatarStyle.avatarBorderColor
        set(@ColorRes value: Int) {
            avatarStyle = avatarStyle.copy(avatarBorderColor = value)
        }

    init {
        val attributes = context.obtainStyledAttributes(
            attrs,
            R.styleable.ChannelListView,
            0,
            0
        )

        channelTitleText = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelListView_streamChannelTitleTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_channel_item_title)
            )
            color(
                R.styleable.ChannelListView_streamChannelTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_channel_item_text_color)
            )
            font(
                R.styleable.ChannelListView_streamChannelTitleTextFontAssets,
                R.styleable.ChannelListView_streamChannelTitleTextFont
            )
            style(R.styleable.ChannelListView_streamChannelTitleTextStyle, Typeface.BOLD)
        }.build()

        channelTitleUnreadText = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelListView_streamChannelTitleTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_channel_item_title)
            )
            color(
                R.styleable.ChannelListView_streamChannelTitleUnreadTextColor,
                ContextCompat.getColor(context, R.color.stream_channel_item_text_color)
            )
            font(
                R.styleable.ChannelListView_streamChannelTitleTextFontAssets,
                R.styleable.ChannelListView_streamChannelTitleTextFont
            )
            style(R.styleable.ChannelListView_streamChannelTitleUnreadTextStyle, Typeface.BOLD)
        }.build()

        attributes.getString(R.styleable.ChannelListView_streamChannelWithOutNameTitleText)
            ?.let { channelWithoutNameText = it }

        lastMessage = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelListView_streamLastMessageTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_channel_item_message)
            )
            color(
                R.styleable.ChannelListView_streamLastMessageTextColor,
                ContextCompat.getColor(context, R.color.stream_gray_dark)
            )
            font(
                R.styleable.ChannelListView_streamLastMessageTextFontAssets,
                R.styleable.ChannelListView_streamLastMessageTextFont
            )
            style(R.styleable.ChannelListView_streamLastMessageTextStyle, Typeface.NORMAL)
        }.build()

        lastMessageUnread = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelListView_streamLastMessageTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_channel_item_message)
            )
            color(
                R.styleable.ChannelListView_streamLastMessageUnreadTextColor,
                ContextCompat.getColor(context, R.color.stream_channel_item_text_color)
            )
            font(
                R.styleable.ChannelListView_streamLastMessageTextFontAssets,
                R.styleable.ChannelListView_streamLastMessageTextFont
            )
            style(R.styleable.ChannelListView_streamLastMessageUnreadTextStyle, Typeface.BOLD)
        }.build()

        lastMessageDateText = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelListView_streamLastMessageDateTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_channel_item_message_date)
            )
            color(
                R.styleable.ChannelListView_streamLastMessageDateTextColor,
                ContextCompat.getColor(context, R.color.stream_gray_dark)
            )
            font(
                R.styleable.ChannelListView_streamLastMessageDateTextFontAssets,
                R.styleable.ChannelListView_streamLastMessageDateTextFont
            )
            style(R.styleable.ChannelListView_streamLastMessageDateTextStyle, Typeface.NORMAL)
        }.build()

        lastMessageDateUnreadText = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelListView_streamLastMessageDateTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_channel_item_message_date)
            )
            color(
                R.styleable.ChannelListView_streamLastMessageDateUnreadTextColor,
                ContextCompat.getColor(context, R.color.stream_channel_item_text_color)
            )
            font(
                R.styleable.ChannelListView_streamLastMessageDateTextFontAssets,
                R.styleable.ChannelListView_streamLastMessageDateTextFont
            )
            style(R.styleable.ChannelListView_streamLastMessageDateUnreadTextStyle, Typeface.BOLD)
        }.build()

        with(attributes) {
            channelPreviewLayout = getResourceId(
                R.styleable.ChannelListView_streamChannelPreviewLayout,
                R.layout.stream_item_channel
            )
            // Avatar
            avatarStyle = AvatarStyle.Builder(this, context)
                .avatarWidth(
                    R.styleable.ChannelListView_streamAvatarWidth,
                    R.dimen.stream_channel_avatar_width
                )
                .avatarHeight(
                    R.styleable.ChannelListView_streamAvatarHeight,
                    R.dimen.stream_channel_avatar_height
                )
                .avatarBorderWidth(
                    R.styleable.ChannelListView_streamAvatarBorderWidth,
                    R.dimen.stream_channel_avatar_border_width
                )
                .avatarBorderColor(
                    R.styleable.ChannelListView_streamAvatarBorderColor,
                    Color.WHITE
                )
                .avatarBackgroundColor(
                    R.styleable.ChannelListView_streamAvatarBackGroundColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                )
                .avatarInitialText(
                    avatarTextSizeStyleableId = R.styleable.ChannelListView_streamAvatarTextSize,
                    avatarTextSizeDefaultValue = R.dimen.stream_channel_initials,
                    avatarTextColorStyleableId = R.styleable.ChannelListView_streamAvatarTextColor,
                    avatarTextColorDefaultValue = Color.WHITE,
                    avatarTextFontAssetsStyleableId = R.styleable.ChannelListView_streamAvatarTextFontAssets,
                    avatarTextFontStyleableId = R.styleable.ChannelListView_streamAvatarTextFont,
                    avatarTextStyleStyleableId = R.styleable.ChannelListView_streamAvatarTextStyle
                )
                .build()

            // Read State

            readStateStyle = ReadStateStyle.Builder(this, context)
                .isReadStateEnabled(R.styleable.ChannelListView_streamShowReadState, true)
                .readStateAvatarWidth(
                    R.styleable.ChannelListView_streamReadStateAvatarWidth,
                    resources.getDimensionPixelSize(R.dimen.stream_read_state_avatar_width)
                )
                .readStateAvatarHeight(
                    R.styleable.ChannelListView_streamReadStateAvatarHeight,
                    resources.getDimensionPixelSize(R.dimen.stream_read_state_avatar_height)
                )
                .readStateText(
                    textSize = R.styleable.ChannelListView_streamReadStateTextSize,
                    defaultTextSize = R.dimen.stream_read_state_text_size,
                    textColor = R.styleable.ChannelListView_streamReadStateTextColor,
                    defaultTextColor = Color.BLACK,
                    textFontAssetsStyleableId = R.styleable.ChannelListView_streamReadStateTextFontAssets,
                    textFontStyleableId = R.styleable.ChannelListView_streamReadStateTextFont,
                    textStyleStyleableId = R.styleable.ChannelListView_streamReadStateTextStyle,
                    textStyleDefault = Typeface.BOLD
                )
                .build()

            recycle()
        }

        val parentAttrs = context.obtainStyledAttributes(
            attrs,
            R.styleable.ChannelsView,
            0,
            0
        )
        itemSeparatorDrawable = parentAttrs.getDrawable(R.styleable.ChannelsView_streamChannelsItemSeparatorDrawable)
        parentAttrs.recycle()
    }
}
