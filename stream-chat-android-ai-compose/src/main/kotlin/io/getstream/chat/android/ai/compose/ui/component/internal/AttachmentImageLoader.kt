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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import io.getstream.chat.android.ai.compose.util.internal.AIImageLoader

/**
 * Loads the images of messages' attachments at the size they show at, through [AIImageLoader], so
 * a message scrolled back into view shows its images at once.
 *
 * Images on this device are read through the content resolver; images on the web are downloaded.
 */
internal object AttachmentImageLoader {

    /** The image at [uri] at [targetSizePx], if it is in memory. */
    fun cached(uri: Uri, targetSizePx: Int): Bitmap? = AIImageLoader.cached(uri.toString(), targetSizePx)

    /** The image at [uri], downsampled to about [targetSizePx], or null when it can't be read. */
    suspend fun load(context: Context, uri: Uri, targetSizePx: Int): Bitmap? {
        val source = uri.toString()
        if (AIImageLoader.isLoadable(source)) return AIImageLoader.load(source, targetSizePx)
        val resolver = context.contentResolver
        return AIImageLoader.load(AIImageLoader.key(source, targetSizePx)) {
            decodeSampledBitmap(resolver, uri, targetSizePx)
        }
    }
}
