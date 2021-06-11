package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorInt

public data class FileAttachmentsViewStyle(
    @ColorInt val backgroundColor: Int,
) {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): FileAttachmentsViewStyle {
            return FileAttachmentsViewStyle(Color.BLUE)
        }
    }
}
