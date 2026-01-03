/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.permissions

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Defines the possible states in which the visual media storage access can be.
 *
 * - [VisualMediaAccess.FULL] when the app has full access to the storage.
 * - [VisualMediaAccess.PARTIAL] when the app has partial access to the storage.
 * - [VisualMediaAccess.DENIED] when the app has no access to the storage.
 */
@InternalStreamChatApi
public enum class VisualMediaAccess {
    FULL,
    PARTIAL,
    DENIED,
}

/**
 * Resolves the current [VisualMediaAccess] state based on the permissions granted by the user.
 *
 * @param context The context to use to check the permission grants.
 */
@InternalStreamChatApi
public fun resolveVisualMediaAccessState(context: Context): VisualMediaAccess {
    val isPermissionGranted = { permission: String ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        isPermissionGranted(READ_MEDIA_IMAGES) &&
        isPermissionGranted(READ_MEDIA_VIDEO)
    ) {
        // Full access on Android 13 (API level 33) or higher
        VisualMediaAccess.FULL
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        isPermissionGranted(READ_MEDIA_VISUAL_USER_SELECTED)
    ) {
        // Partial access on Android 14 (API level 34) or higher
        VisualMediaAccess.PARTIAL
    } else if (isPermissionGranted(READ_EXTERNAL_STORAGE)) {
        // Full access up to Android 12 (API level 32)
        VisualMediaAccess.FULL
    } else {
        // Access denied
        VisualMediaAccess.DENIED
    }
}
