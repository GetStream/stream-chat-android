package io.getstream.chat.android.ui.message.list.background

import android.content.Context
import android.view.View
import com.google.android.material.shape.ShapeAppearanceModel

internal object ShapeAppearanceModelFactory {

    fun create(
        context: Context,
        radius: Float,
        bottomEndCorner: Float,
        isMine: Boolean,
        isBottomPosition: Boolean
    ): ShapeAppearanceModel {
        return ShapeAppearanceModel.builder()
            .setAllCornerSizes(radius)
            .apply {
                if (isBottomPosition) {
                    val isRtl = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

                    when {
                        !isRtl && isMine -> setBottomRightCornerSize(bottomEndCorner)

                        !isRtl && !isMine -> setBottomLeftCornerSize(bottomEndCorner)

                        isRtl && isMine -> setBottomLeftCornerSize(bottomEndCorner)

                        isRtl && !isMine -> setBottomRightCornerSize(bottomEndCorner)
                    }
                }
            }
            .build()
    }

}
