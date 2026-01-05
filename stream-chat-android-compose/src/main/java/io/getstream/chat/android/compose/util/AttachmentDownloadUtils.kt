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

package io.getstream.chat.android.compose.util

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.state.extensions.downloadAttachment
import io.getstream.chat.android.ui.common.utils.extensions.onPermissionRequested
import io.getstream.chat.android.ui.common.utils.extensions.wasPermissionRequested
import io.getstream.chat.android.uiutils.util.openSystemSettings

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun attachmentDownloadState(): Pair<PermissionState, MutableState<Attachment?>> {
    var writePermissionRequested by rememberSaveable { mutableStateOf(false) }
    val writePermissionState = rememberPermissionState(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    ) {
        writePermissionRequested = true
    }

    val downloadPayload = remember { mutableStateOf<Attachment?>(null) }

    val context = LocalContext.current
    val downloadAttachmentUriGenerator = ChatTheme.streamDownloadAttachmentUriGenerator
    val downloadRequestInterceptor = ChatTheme.streamDownloadRequestInterceptor

    LaunchedEffect(writePermissionState.status.isGranted) {
        if (writePermissionState.status.isGranted) {
            downloadPayload.value?.let {
                onDownloadPermissionGranted(
                    context,
                    it,
                    downloadAttachmentUriGenerator::generateDownloadUri,
                    downloadRequestInterceptor::intercept,
                )
                downloadPayload.value = null
            }
        }
    }

    return writePermissionState to downloadPayload
}

@OptIn(ExperimentalPermissionsApi::class)
@Suppress("LongParameterList")
internal fun onDownloadHandleRequest(
    context: Context,
    payload: Attachment,
    permissionState: PermissionState,
    downloadPayload: MutableState<Attachment?>,
    generateDownloadUri: (Attachment) -> Uri,
    interceptRequest: DownloadManager.Request.() -> Unit,
) {
    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy()) ||
        permissionState.status.isGranted
    ) {
        onDownloadPermissionGranted(
            context,
            payload,
            generateDownloadUri,
            interceptRequest,
        )
        downloadPayload.value = null
    } else {
        downloadPayload.value = payload
        onDownloadPermissionRequired(context, permissionState)
        context.onPermissionRequested(permissionState.permission)
    }
}

internal fun onDownloadPermissionGranted(
    context: Context,
    payload: Attachment,
    generateDownloadUri: (Attachment) -> Uri,
    interceptRequest: DownloadManager.Request.() -> Unit,
) {
    payload.let {
        ChatClient
            .instance()
            .downloadAttachment(
                context,
                it,
                generateDownloadUri,
                interceptRequest,
            )
            .enqueue()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
internal fun onDownloadPermissionRequired(context: Context, permissionState: PermissionState) {
    if (!context.wasPermissionRequested(permissionState.permission) || permissionState.status.shouldShowRationale) {
        permissionState.launchPermissionRequest()
    } else {
        context.openSystemSettings()
    }
}
