package io.getstream.chat.android.ui.message.list.background

import android.content.Context
import android.view.View
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.ui.utils.isRtlLayout

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
                    val isRtl = context.isRtlLayout

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
