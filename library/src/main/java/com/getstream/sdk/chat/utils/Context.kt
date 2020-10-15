package com.getstream.sdk.chat.utils

import android.content.Context
import android.view.LayoutInflater

inline val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)
