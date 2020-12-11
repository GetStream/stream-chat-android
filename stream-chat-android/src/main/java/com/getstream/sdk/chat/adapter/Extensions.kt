package com.getstream.sdk.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public inline val ViewGroup.inflater: LayoutInflater
    get() = LayoutInflater.from(context)

@InternalStreamChatApi
public inline fun ConstraintLayout.updateConstraints(actions: ConstraintSet.() -> Unit) {
    val set = ConstraintSet()
    set.clone(this)
    set.actions()
    set.applyTo(this)
}
