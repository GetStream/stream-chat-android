package com.getstream.sdk.chat.utils.extensions

import android.view.LayoutInflater
import android.view.ViewGroup
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public inline val ViewGroup.inflater: LayoutInflater
    get() = LayoutInflater.from(context)
