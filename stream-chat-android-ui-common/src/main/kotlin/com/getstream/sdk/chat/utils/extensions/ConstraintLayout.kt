package com.getstream.sdk.chat.utils.extensions

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public fun ConstraintLayout.updateConstraints(actions: ConstraintSet.() -> Unit) {
    val set = ConstraintSet()
    set.clone(this)
    set.actions()
    set.applyTo(this)
}

@InternalStreamChatApi
public fun ConstraintSet.constrainViewToParentBySide(view: View, side: Int) {
    connect(view.id, side, ConstraintSet.PARENT_ID, side)
}

@InternalStreamChatApi
public fun ConstraintSet.constrainViewStartToEndOfView(startView: View, endView: View) {
    connect(startView.id, ConstraintSet.START, endView.id, ConstraintSet.END)
}

@InternalStreamChatApi
public fun ConstraintSet.constrainViewEndToEndOfView(startView: View, endView: View) {
    connect(startView.id, ConstraintSet.END, endView.id, ConstraintSet.END)
}

@InternalStreamChatApi
public fun ConstraintSet.horizontalChainInParent(vararg views: View) {
    createHorizontalChain(
        ConstraintSet.PARENT_ID,
        ConstraintSet.LEFT,
        ConstraintSet.PARENT_ID,
        ConstraintSet.RIGHT,
        views.map(View::getId).toIntArray(),
        null,
        ConstraintSet.CHAIN_SPREAD
    )
}

@InternalStreamChatApi
public fun ConstraintSet.verticalChainInParent(vararg views: View) {
    createVerticalChain(
        ConstraintSet.PARENT_ID,
        ConstraintSet.TOP,
        ConstraintSet.PARENT_ID,
        ConstraintSet.BOTTOM,
        views.map(View::getId).toIntArray(),
        null,
        ConstraintSet.CHAIN_SPREAD
    )
}
