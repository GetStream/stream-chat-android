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
public fun ConstraintLayout.constraintViewToParentBySide(view: View, side: Int) {
    updateConstraints {
        connect(view.id, side, ConstraintSet.PARENT_ID, side)
    }
}
