package io.getstream.chat.android.ui.utils.extensions

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

internal inline fun ConstraintLayout.updateConstraints(actions: ConstraintSet.() -> Unit) {
    val set = ConstraintSet()
    set.clone(this)
    set.actions()
    set.applyTo(this)
}
