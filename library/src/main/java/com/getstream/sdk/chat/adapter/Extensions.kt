package com.getstream.sdk.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup

internal inline val ViewGroup.inflater: LayoutInflater
    get() = LayoutInflater.from(context)
