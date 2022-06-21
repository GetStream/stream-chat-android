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

package io.getstream.chat.android.compose.handlers

import android.Manifest
import android.content.Context
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState

/**
 * Provides default implementations of permission handlers.
 */
public object StreamPermissionHandlers {

    /**
     * @param context [Context]
     * @param permissionStates The [PermissionState] of the default permission handlers.
     *
     * @return default [PermissionHandler] implementations.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    public fun defaultHandlers(context: Context, permissionStates: List<PermissionState>): List<PermissionHandler> {
        return listOf(
            DownloadPermissionHandler(
                permissionStates.first { it.permission == Manifest.permission.WRITE_EXTERNAL_STORAGE },
                context
            )
        )
    }
}
