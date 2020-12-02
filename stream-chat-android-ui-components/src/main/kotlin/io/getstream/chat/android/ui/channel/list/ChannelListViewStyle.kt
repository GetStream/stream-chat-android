package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.avatar.AvatarStyle

public class ChannelListViewStyle internal constructor(context: Context, attrs: AttributeSet?) {
    @LayoutRes
    public val channelPreviewLayout: Int
    public val channelTitleText: TextStyle
    public val channelTitleUnreadText: TextStyle
    public val lastMessage: TextStyle
    public val lastMessageUnread: TextStyle
    public val lastMessageDateText: TextStyle
    public val lastMessageDateUnreadText: TextStyle
    public val avatarStyle: AvatarStyle
    public val readStateStyle: ReadStateStyle
    private val resources = context.resources

    private var channelWithoutNameText = ""

    public fun getChannelWithoutNameText(): String =
        channelWithoutNameText.takeIf(String::isNotBlank)
            ?: resources.getString(R.string.stream_channel_unknown_title)

    public var avatarBorderColor: Int
        get() = avatarStyle.avatarBorderColor
        set(@ColorRes value: Int) {
            avatarStyle.avatarBorderColor = value
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
                R.styleable.ChannelListView_streamLastMessageDateTextColor,
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
                io.getstream.chat.android.ui.R.layout.stream_channel_list_item_view
            )
            // Avatar
            avatarStyle = AvatarStyle(context, attrs)

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
    }
}
