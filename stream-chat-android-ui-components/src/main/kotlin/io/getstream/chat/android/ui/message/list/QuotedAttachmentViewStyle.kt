package io.getstream.chat.android.ui.message.list

import android.content.Context
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getDimension

/**
 * Style to be applied to [QuotedAttachmentView]. Use [TransformStyle] to change the style programmatically.
 *
 * @param height The height of the quoted attachment.
 * @param width The width of the quoted attachment.
 * @param radius The radius of the quoted attachment corners.
 */
public class QuotedAttachmentViewStyle(
    @Px public val height: Int,
    @Px public val width: Int,
    @Px public val radius: Int,
) {

    internal companion object {
        operator fun invoke(context: Context): QuotedAttachmentViewStyle {
            val height: Int = context.getDimension(R.dimen.streamUiQuotedAttachmentViewHeight)
            val width: Int = context.getDimension(R.dimen.streamUiQuotedAttachmentViewWidth)
            val radius: Int = context.getDimension(R.dimen.streamUiQuotedAttachmentImageRadius)

            return QuotedAttachmentViewStyle(
                height = height,
                width = width,
                radius = radius
            ).let(TransformStyle.quotedAttachmentViewStyleTransformer::transform)
        }
    }
}