package io.getstream.chat.android.ui.message.list.background

import android.content.Context
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.ui.utils.isRtlLayout

/**
 * Class that creates the default version of ShapeAppearanceModel used in the background of messages, attachments, pictures...
 */
internal object ShapeAppearanceModelFactory {

    /**
     * Creates the ShapeAppearanceModel.
     *
     * @param context [Context].
     * @param defaultCornerRadius The corner radius of all corners with the exception of one.
     * @param differentCornerRadius The corner radius of one of the corners accordingly with the logic on the method.
     * @param isMine Whether the message is from the current user or not. Used to position the differentCorner.
     * @param isBottomPosition Whether the message the bottom position or not. Used to position the differentCorner.
     */
    fun create(
        context: Context,
        defaultCornerRadius: Float,
        differentCornerRadius: Float,
        isMine: Boolean,
        isBottomPosition: Boolean
    ): ShapeAppearanceModel {
        return ShapeAppearanceModel.builder()
            .setAllCornerSizes(defaultCornerRadius)
            .apply {
                if (isBottomPosition) {
                    val isRtl = context.isRtlLayout

                    when {
                        !isRtl && isMine -> setBottomRightCornerSize(differentCornerRadius)

                        !isRtl && !isMine -> setBottomLeftCornerSize(differentCornerRadius)

                        isRtl && isMine -> setBottomLeftCornerSize(differentCornerRadius)

                        isRtl && !isMine -> setBottomRightCornerSize(differentCornerRadius)
                    }
                }
            }
            .build()
    }
}
