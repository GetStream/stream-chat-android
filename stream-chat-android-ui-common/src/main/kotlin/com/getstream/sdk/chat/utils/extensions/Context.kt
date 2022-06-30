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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public inline val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)

@InternalStreamChatApi
public fun Context.openSystemSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        val uri: Uri = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        data = uri
    }
    startActivity(intent)
}

/**
 * @param permission The permission we want to check if it was requested before.
 *
 * @return If the permission was requested before or not.
 */
public fun Context.wasPermissionRequested(permission: String): Boolean {
    return getSharedPreferences(PERMISSIONS_PREFS, Context.MODE_PRIVATE).getBoolean(permission, false)
}

/**
 * Saves to shared prefs that a permission has been requested.
 *
 * @param permission The permission in question.
 */
public fun Context.onPermissionRequested(permission: String) {
    return getSharedPreferences(PERMISSIONS_PREFS, Context.MODE_PRIVATE).edit().putBoolean(permission, true).apply()
}

private const val PERMISSIONS_PREFS = "stream_permissions"
