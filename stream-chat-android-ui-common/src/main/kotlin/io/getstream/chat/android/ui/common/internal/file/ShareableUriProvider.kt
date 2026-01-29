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

package io.getstream.chat.android.ui.common.internal.file

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.StreamFileProvider
import java.io.File

/**
 * Provider class for generating shareable URIs for files using [FileProvider].
 */
@InternalStreamChatApi
public class ShareableUriProvider {

    /**
     * Gets a content URI for the given file using [FileProvider].
     *
     * @param context The Android context.
     * @param file The file to get the URI for.
     * @return The content [Uri] for the file.
     */
    public fun getUriForFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, getFileProviderAuthority(context), file)

    private fun getFileProviderAuthority(context: Context): String {
        val compName = ComponentName(context, StreamFileProvider::class.java.name)
        val providerInfo = context.packageManager.getProviderInfo(compName, 0)
        return providerInfo.authority
    }
}
