package io.getstream.chat.android.ui.common.extensions.internal

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup

internal val ViewGroup.streamThemeInflater: LayoutInflater
    get() = LayoutInflater.from(context.createStreamThemeWrapper())

internal val Activity.streamThemeInflater: LayoutInflater
    get() = LayoutInflater.from(this.createStreamThemeWrapper())
