package io.getstream.chat.android.ui.channel.list

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.R
import kotlin.math.roundToInt

internal class SimpleVerticalListDivider : RecyclerView.ItemDecoration() {

    @DrawableRes
    var drawableResource: Int = R.drawable.stream_divider

    /**
     * Drawable height in pixels
     */
    var drawableHeight: Int? = null

    private val drawOnLastItem: Boolean = false

    private lateinit var drawable: Drawable

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        parent.context
            .let { ContextCompat.getDrawable(it, drawableResource) }
            ?.also { drawable = it }
            ?.let { outRect.set(0, 0, 0, determineHeight()) }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        canvas.save()

        val left = when {
            parent.clipToPadding -> parent.paddingLeft
            else -> 0
        }

        val right = when {
            parent.clipToPadding -> parent.right - parent.paddingRight
            else -> parent.right
        }

        val drawRange = when {
            drawOnLastItem -> 0 until parent.children.count()
            else -> 0 until parent.children.count() - 1
        }

        for (index in drawRange) {
            parent.getChildAt(index).let { item ->
                val bounds = Rect()
                parent.getDecoratedBoundsWithMargins(item, bounds)
                val bottom = bounds.bottom + item.translationY.roundToInt()
                val top = bottom - determineHeight()
                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }
        }

        canvas.restore()
    }

    private fun determineHeight(): Int {
        return drawableHeight ?: drawable.intrinsicHeight
    }
}
