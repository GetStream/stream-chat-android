package io.getstream.chat.ui.sample.common

import android.app.Activity
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.getstream.chat.android.livedata.ChannelData
import io.getstream.chat.ui.sample.R

fun Activity.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(text: String) {
    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
}

fun Context.getColorFromRes(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

fun Context.getDimensionPixelSize(@DimenRes id: Int): Int {
    return resources.getDimensionPixelSize(id)
}

fun EditText.hideKeyboard() {
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun EditText.showKeyboard() {
    requestFocus()
    val imm: InputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

val EditText.trimmedText: String
    get() = text.trim().toString()

fun NavController.navigateSafely(directions: NavDirections) {
    currentDestination?.getAction(directions.actionId)?.let { navigate(directions) }
}

fun NavController.navigateSafely(@IdRes resId: Int) {
    if (currentDestination?.id != resId) {
        navigate(resId, null)
    }
}

fun Fragment.initToolbar(toolbar: Toolbar) {
    (requireActivity() as AppCompatActivity).run {
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayShowTitleEnabled(false)
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationIcon(R.drawable.ic_icon_left)
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }
}

fun BottomNavigationView.setBadgeNumber(@IdRes menuItemId: Int, badgeNumber: Int) {
    val badge = getBadge(menuItemId)
        ?: getOrCreateBadge(menuItemId).apply {
            horizontalOffset = -context.getDimensionPixelSize(R.dimen.badge_horizontal_offset)
            verticalOffset = context.getDimensionPixelSize(R.dimen.badge_vertical_offset)
            backgroundColor = context.getColorFromRes(R.color.bottom_nav_badge_color)
        }
    badge.isVisible = badgeNumber > 0
    badge.number = badgeNumber
}

fun Context?.getFragmentManager(): FragmentManager? {
    return when (this) {
        is AppCompatActivity -> supportFragmentManager
        is ContextThemeWrapper -> baseContext.getFragmentManager()
        else -> null
    }
}

val ChannelData.name: String
    get() = (extraData["name"] as? String) ?: ""
