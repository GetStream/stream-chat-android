package io.getstream.chat.sample.common

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import io.getstream.chat.android.client.models.User

var User.image: String
    get() = extraData["image"] as String
    set(value) {
        extraData["image"] = value
    }

var User.name: String
    get() = extraData["name"] as String
    set(value) {
        extraData["name"] = value
    }

fun Activity.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(text: String) {
    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
}

fun EditText.hideKeyboard() {
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun EditText.showKeyboard() {
    requestFocus()
    val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
