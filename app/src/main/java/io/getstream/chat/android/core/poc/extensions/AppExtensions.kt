package io.getstream.chat.android.core.poc.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.view.View
import org.jetbrains.anko.toast
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.widget.Toast
import io.getstream.chat.android.core.poc.library.Result
import org.jetbrains.anko.runOnUiThread


fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, T6 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    p5: T5?,
    p6: T6?,
    block: (T1, T2, T3, T4, T5, T6) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null && p6 != null) block(
        p1,
        p2,
        p3,
        p4,
        p5,
        p6
    ) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    p5: T5?,
    block: (T1, T2, T3, T4, T5) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(
        p1,
        p2,
        p3,
        p4,
        p5
    ) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    block: (T1, T2, T3, T4) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null
}

fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    block: (T1, T2, T3) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

fun Context.IN_DEVELOPMENT() {
    toast("This feature in development process")
}

fun <T: Enum<T>> T.next(): T? {
    val values = declaringClass.enumConstants
    val nextOrdinal = (ordinal + 1) % (values?.size ?: 0)
    return values?.get(nextOrdinal)
}

fun Context.echoResult(
    result: Result<*>,
    success: String = "Success",
    error: String = "Error"
) {
    runOnUiThread {
        if (result.isSuccess) {
            toast(success)
        } else {
            toast( "$error : ${result.error().message}")
        }
    }
}