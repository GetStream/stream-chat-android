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

package io.getstream.chat.android.ui.common.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Returns if we need to check for the given permission or not.
 *
 * @param permission The permission to check.
 * @return If the given permission is declared in the manifest or not.
 */
@InternalStreamChatApi
public fun Context.isPermissionDeclared(permission: String): Boolean {
    return packageManager
        .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        ?.requestedPermissions
        ?.contains(permission) == true
}

/**
 * Share a local file.
 *
 * @param uri The local file [Uri] to share.
 * @param mimeType The mime type of the local file to share. If null, the system will try to infer it.
 * @param text An optional text to share along with the file.
 */
@InternalStreamChatApi
public fun Context.shareLocalFile(
    uri: Uri,
    mimeType: String? = null,
    text: String? = null,
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType ?: "*/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        text?.let { putExtra(Intent.EXTRA_TEXT, it) }
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(intent, null))
}
