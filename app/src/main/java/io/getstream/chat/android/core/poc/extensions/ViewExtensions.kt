package io.getstream.chat.android.core.poc.extensions

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.viewpager.widget.ViewPager

fun ViewPager.onPageSelected(listener: (position: Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            //unused
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            //unused
        }

        override fun onPageSelected(position: Int) {
            listener.invoke(position)
        }
    })
}

fun View.showSoftKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

fun View.hideSoftKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.sp: Int
    get() = this * Resources.getSystem().displayMetrics.scaledDensity.toInt()


fun View.makeGoneIf(predicate: Boolean) {
    if (predicate) makeGone() else makeVisible()
}

fun View.makeInvisibleIf(predicate: Boolean) {
    if (predicate) makeInvisible() else makeVisible()
}

fun View.makeVisibleIf(predicate: Boolean, or: Int) {
    if (predicate) makeVisible()
    else visibility = or
}

fun View.makeVisible() {
    visibility = View.VISIBLE
}

fun View.makeInvisible() {
    visibility = View.INVISIBLE
}

fun View.makeGone() {
    visibility = View.GONE
}