package io.getstream.chat.android.ui.message.list.adapter.view.internal

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.reactions.edit.EditReactionsViewStyle
import io.getstream.chat.android.ui.message.list.reactions.view.ViewReactionsViewStyle

/**
 * Style for view holders used inside [MessageListView].
 * Use this class together with [TransformStyle.messageListItemStyleTransformer] to change styles programmatically.
 *
 */
public class MessageReplyViewStyle(
    @ColorInt
    public val messageBackgroundColorMine: Int?,
    @ColorInt
    public val messageBackgroundColorTheirs: Int?,
    @ColorInt public val messageTextColorTheirs: Int?,
    @ColorInt public val messageLinkTextColorMine: Int?,
    @ColorInt public val messageLinkTextColorTheirs: Int?,
    @ColorInt public val messageLinkBackgroundColorMine: Int,
    @ColorInt public val messageLinkBackgroundColorTheirs: Int,
    public val reactionsEnabled: Boolean,
    public val textStyleMine: TextStyle,
    public val textStyleTheirs: TextStyle,
    public val textStyleMessageDate: TextStyle,
    public val reactionsViewStyle: ViewReactionsViewStyle,
    public val editReactionsViewStyle: EditReactionsViewStyle,
    public val iconIndicatorSent: Drawable,
    public val iconIndicatorRead: Drawable,
    public val iconIndicatorPendingSync: Drawable,
    public val iconOnlyVisibleToYou: Drawable,
    public val textStyleMessageDeleted: TextStyle,
    @ColorInt public val messageDeletedBackground: Int,
    @ColorInt public val messageStrokeColorMine: Int,
    @Px public val messageStrokeWidthMine: Float,
    @ColorInt public val messageStrokeColorTheirs: Int,
    @Px public val messageStrokeWidthTheirs: Float,
    public val textStyleErrorMessage: TextStyle,
) {
    internal class Builder(private val attributes: TypedArray, private val context: Context)
}
