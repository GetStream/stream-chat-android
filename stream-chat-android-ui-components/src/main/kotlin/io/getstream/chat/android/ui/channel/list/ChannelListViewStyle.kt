package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.R
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
                R.styleable.ChannelListView_streamUiChannelTitleTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_ui_channel_item_title)
            )
            color(
                R.styleable.ChannelListView_streamUiChannelTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_channel_item_text_color)
            )
            font(
                R.styleable.ChannelListView_streamUiChannelTitleTextFontAssets,
                R.styleable.ChannelListView_streamUiChannelTitleTextFont
            )
            style(R.styleable.ChannelListView_streamUiChannelTitleTextStyle, Typeface.BOLD)
        }.build()

        channelTitleUnreadText = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelListView_streamUiChannelTitleTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_ui_channel_item_title)
            )
            color(
                R.styleable.ChannelListView_streamUiChannelTitleUnreadTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_channel_item_text_color)
            )
            font(
                R.styleable.ChannelListView_streamUiChannelTitleTextFontAssets,
                R.styleable.ChannelListView_streamUiChannelTitleTextFont
            )
            style(R.styleable.ChannelListView_streamUiChannelTitleUnreadTextStyle, Typeface.BOLD)
        }.build()

        attributes.getString(R.styleable.ChannelListView_streamUiChannelWithOutNameTitleText)
            ?.let { channelWithoutNameText = it }

        lastMessage = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelListView_streamUiLastMessageTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_ui_channel_item_message)
            )
            color(
                R.styleable.ChannelListView_streamUiLastMessageTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_gray_dark)
            )
            font(
                R.styleable.ChannelListView_streamUiLastMessageTextFontAssets,
                R.styleable.ChannelListView_streamUiLastMessageTextFont
            )
            style(R.styleable.ChannelListView_streamUiLastMessageTextStyle, Typeface.NORMAL)
        }.build()

        lastMessageUnread = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelListView_streamUiLastMessageTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_ui_channel_item_message)
            )
            color(
                R.styleable.ChannelListView_streamUiLastMessageUnreadTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_channel_item_text_color)
            )
            font(
                R.styleable.ChannelListView_streamUiLastMessageTextFontAssets,
                R.styleable.ChannelListView_streamUiLastMessageTextFont
            )
            style(R.styleable.ChannelListView_streamUiLastMessageUnreadTextStyle, Typeface.BOLD)
        }.build()

        lastMessageDateText = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelListView_streamUiLastMessageDateTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_ui_channel_item_message_date)
            )
            color(
                R.styleable.ChannelListView_streamUiLastMessageDateTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_gray_dark)
            )
            font(
                R.styleable.ChannelListView_streamUiLastMessageDateTextFontAssets,
                R.styleable.ChannelListView_streamUiLastMessageDateTextFont
            )
            style(R.styleable.ChannelListView_streamUiLastMessageDateTextStyle, Typeface.NORMAL)
        }.build()

        lastMessageDateUnreadText = TextStyle.Builder(attributes).apply {
            size(
                R.styleable.ChannelListView_streamUiLastMessageDateTextSize,
                resources.getDimensionPixelSize(R.dimen.stream_ui_channel_item_message_date)
            )
            color(
                R.styleable.ChannelListView_streamUiLastMessageDateTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_channel_item_text_color)
            )
            font(
                R.styleable.ChannelListView_streamUiLastMessageDateTextFontAssets,
                R.styleable.ChannelListView_streamUiLastMessageDateTextFont
            )
            style(R.styleable.ChannelListView_streamUiLastMessageDateUnreadTextStyle, Typeface.BOLD)
        }.build()

        with(attributes) {
            channelPreviewLayout = getResourceId(
                R.styleable.ChannelListView_streamUiChannelPreviewLayout,
                io.getstream.chat.android.ui.R.layout.stream_ui_channel_list_item_view
            )
            // Avatar
            avatarStyle = AvatarStyle(context, attrs)

            // Read State

            readStateStyle = ReadStateStyle.Builder(this, context)
                .isReadStateEnabled(R.styleable.ChannelListView_streamUiShowReadState, true)
                .readStateAvatarWidth(
                    R.styleable.ChannelListView_streamUiReadStateAvatarWidth,
                    resources.getDimensionPixelSize(R.dimen.stream_ui_read_state_avatar_width)
                )
                .readStateAvatarHeight(
                    R.styleable.ChannelListView_streamUiReadStateAvatarHeight,
                    resources.getDimensionPixelSize(R.dimen.stream_ui_read_state_avatar_height)
                )
                .readStateText(
                    textSize = R.styleable.ChannelListView_streamUiReadStateTextSize,
                    defaultTextSize = R.dimen.stream_ui_read_state_text_size,
                    textColor = R.styleable.ChannelListView_streamUiReadStateTextColor,
                    defaultTextColor = Color.BLACK,
                    textFontAssetsStyleableId = R.styleable.ChannelListView_streamUiReadStateTextFontAssets,
                    textFontStyleableId = R.styleable.ChannelListView_streamUiReadStateTextFont,
                    textStyleStyleableId = R.styleable.ChannelListView_streamUiReadStateTextStyle,
                    textStyleDefault = Typeface.BOLD
                )
                .build()

            recycle()
        }
    }
}
