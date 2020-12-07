package io.getstream.chat.android.ui.utils.extensions

import android.view.LayoutInflater
import android.view.ViewGroup

internal val ViewGroup.inflater: LayoutInflater
    get() = LayoutInflater.from(context)
