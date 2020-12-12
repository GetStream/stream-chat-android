package com.getstream.sdk.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import io.getstream.chat.android.core.internal.InternalStreamChatApi

internal inline val ViewGroup.inflater: LayoutInflater
    get() = LayoutInflater.from(context)

@InternalStreamChatApi
public fun ConstraintLayout.updateConstraints(actions: ConstraintSet.() -> Unit) {
    val set = ConstraintSet()
    set.clone(this)
    set.actions()
    set.applyTo(this)
}

@InternalStreamChatApi
public fun ConstraintLayout.constrainViewToParentBySide(view: View, side: Int) {
    updateConstraints {
        constrainViewToParentBySide(view, side)
    }
}

@InternalStreamChatApi
public fun ConstraintSet.constrainViewToParentBySide(view: View, side: Int) {
    connect(view.id, side, ConstraintSet.PARENT_ID, side)
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