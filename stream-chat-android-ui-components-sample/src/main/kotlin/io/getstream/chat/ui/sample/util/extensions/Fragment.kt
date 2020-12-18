package io.getstream.chat.ui.sample.util.extensions

import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import androidx.fragment.app.Fragment

fun Fragment.useAdjustNothing() = setSoftInputMode(SOFT_INPUT_ADJUST_NOTHING)

fun Fragment.useAdjustPan() = setSoftInputMode(SOFT_INPUT_ADJUST_PAN)

fun Fragment.useAdjustResize() = setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)

private fun Fragment.setSoftInputMode(flags: Int) {
    (activity ?: return).window.setSoftInputMode(flags)
}
