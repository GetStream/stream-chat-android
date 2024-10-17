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

package io.getstream.chat.android.compose.uiautomator

import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.test.uiautomator.UiDevice

/**
 * Grant app permissions.
 */
public fun UiDevice.grantPermission(permission: String): String =
    exec("pm grant $packageName $permission")

/**
 * Revoke app permissions.
 */
public fun UiDevice.revokePermission(permission: String): String =
    exec("pm revoke $packageName $permission")

/**
 * Checking whether the permission is allowed.
 */
public fun UiDevice.isPermissionAllowed(permission: () -> String): Boolean {
    return ContextCompat.checkSelfPermission(appContext, permission()) == PermissionChecker.PERMISSION_GRANTED
}
