/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.getstream.sdk.chat.utils.extensions

import android.content.ContextWrapper
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Ensures the context being accessed in a View can be cast to Activity.
 */
@InternalStreamChatApi
public val View.activity: AppCompatActivity?
    get() {
        var context = context
        while (context is ContextWrapper) {
            if (context is AppCompatActivity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

@InternalStreamChatApi
public fun View.showToast(@StringRes resId: Int) {
    Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
}
