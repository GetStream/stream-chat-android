package io.getstream.chat.android.ui.common.extensions.internal

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup

internal val ViewGroup.streamThemeInflater: LayoutInflater
    get() = LayoutInflater.from(context.createStreamThemeWrapper())

internal val Context.streamThemeInflater: LayoutInflater
    get() = LayoutInflater.from(this.createStreamThemeWrapper())
