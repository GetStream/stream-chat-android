/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.ui.sample.common

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.getstream.chat.android.models.Channel
import io.getstream.chat.ui.sample.R

fun Activity.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(@StringRes resId: Int) {
    showToast(getString(resId))
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

fun NavController.navigateSafely(@IdRes resId: Int, args: Bundle? = null) {
    if (currentDestination?.id != resId) {
        navigate(resId, args)
    }
}

fun Fragment.navigateSafely(@IdRes resId: Int, args: Bundle? = null) {
    findNavController().navigateSafely(resId, args)
}

fun Fragment.navigateSafely(directions: NavDirections) {
    findNavController().navigateSafely(directions)
}

fun Fragment.initToolbar(toolbar: Toolbar) {
    (requireActivity() as AppCompatActivity).run {
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayShowTitleEnabled(false)
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)

            ContextCompat.getDrawable(requireContext(), R.drawable.ic_icon_left)?.apply {
                setTint(ContextCompat.getColor(requireContext(), R.color.stream_ui_black))
            }?.let(toolbar::setNavigationIcon)

            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }
}

fun BottomNavigationView.setBadgeNumber(@IdRes menuItemId: Int, badgeNumber: Int) {
    getOrCreateBadge(menuItemId).apply {
        horizontalOffset = -context.getDimensionPixelSize(R.dimen.badge_horizontal_offset)
        verticalOffset = context.getDimensionPixelSize(R.dimen.badge_vertical_offset)
        backgroundColor = context.getColorFromRes(R.color.stream_ui_accent_red)
        isVisible = badgeNumber > 0
        number = badgeNumber
    }
}

const val CHANNEL_ARG_DRAFT = "draft"

val Channel.isDraft: Boolean
    get() = getExtraValue(CHANNEL_ARG_DRAFT, false)

val Context.appThemeContext: Context
    get() = ContextThemeWrapper(this, R.style.AppTheme)

/**
 * Group channels are channels with more than 2 members or channels that are not distinct.
 */
val Channel.isGroupChannel: Boolean
    get() = memberCount > 2 || !isDistinct

/**
 * A distinct channel is a channel created for a particular set of users, usually for one-to-one conversations.
 */
private val Channel.isDistinct: Boolean
    get() = id.startsWith("!members")
