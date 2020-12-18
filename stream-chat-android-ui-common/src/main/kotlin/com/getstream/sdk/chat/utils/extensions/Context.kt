package com.getstream.sdk.chat.utils.extensions

import android.content.Context
import android.view.LayoutInflater
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public inline val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)
