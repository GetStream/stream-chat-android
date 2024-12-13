/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat

/**
 * Builds an [Array] of the required permissions for accessing visual media, based on the Android version.
 */
internal fun visualMediaPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        // Android 14+
        arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13
        arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
    } else {
        // Android 12 and below
        arrayOf(READ_EXTERNAL_STORAGE)
    }

/**
 * Builds an [Array] of the required permissions for accessing audio media, based on the Android version.
 */
internal fun audioPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+
        arrayOf(READ_MEDIA_AUDIO)
    } else {
        // Android 12 and below
        arrayOf(READ_EXTERNAL_STORAGE)
    }

/**
 * Builds an [Array] of the required permissions for accessing visual + audio media, based on the Android version.
 */
internal fun filesPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        // Android 14+
        arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED, READ_MEDIA_AUDIO)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13
        arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO)
    } else {
        // Android 12 and below
        arrayOf(READ_EXTERNAL_STORAGE)
    }

/**
 * Checks if the [grantResults] indicate that the permissions were permanently denied.
 *
 * @param context The calling [Context].
 * @param grantResults The results delivered by the callback of
 * [androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions].
 */
internal fun isPermanentlyDenied(context: Context, grantResults: Map<String, Boolean>): Boolean {
    val activity = context as? Activity ?: return false // should never fail
    return grantResults.all { (permission, granted) ->
        !granted && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
}
