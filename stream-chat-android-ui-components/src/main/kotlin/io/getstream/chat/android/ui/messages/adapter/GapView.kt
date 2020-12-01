package io.getstream.chat.android.ui.messages.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.Utils.dpToPx

internal class GapView : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    private val smallGap: View
    private val bigGap: View

    init {
        smallGap = Space(context).apply {
            layoutParams = createLayoutParams(dpToPx(SMALL_GAP_HEIGHT_DP))
            isVisible = false
        }
        bigGap = Space(context).apply {
            layoutParams = createLayoutParams(dpToPx(BIG_GAP_HEIGHT_DP))
            isVisible = false
        }
        addView(smallGap)
        addView(bigGap)
    }

    fun showSmallGap() {
        smallGap.isVisible = true
        bigGap.isVisible = false
    }

    fun showBigGap() {
        smallGap.isVisible = false
        bigGap.isVisible = true
    }

    companion object {
        private const val SMALL_GAP_HEIGHT_DP = 2
        private const val BIG_GAP_HEIGHT_DP = 8
        private fun createLayoutParams(height: Int): ViewGroup.LayoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
    }
}
