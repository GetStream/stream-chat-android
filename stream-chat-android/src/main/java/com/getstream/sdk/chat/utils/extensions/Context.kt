package com.getstream.sdk.chat.utils.extensions

import android.content.Context
import android.view.LayoutInflater

internal inline val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)
