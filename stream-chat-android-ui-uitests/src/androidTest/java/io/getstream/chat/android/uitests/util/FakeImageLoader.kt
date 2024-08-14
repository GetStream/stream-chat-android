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

package io.getstream.chat.android.uitests.util

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.DataSource
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.DefaultRequestOptions
import coil.request.Disposable
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult
import io.getstream.chat.android.uitests.R
import kotlinx.coroutines.CompletableDeferred

/**
 * A fake implementation of [ImageLoader] which returns [Drawable]s from resources.
 *
 * @param context The context to load resources.
 * @param userAvatars Avatar URL to Drawable resource mapping.
 */
class FakeImageLoader(
    private val context: Context,
    private val userAvatars: Map<String, Int> = mapOf(
        AVATAR_JC to R.drawable.avatar_jc,
        AVATAR_AMIT to R.drawable.avatar_amit,
        AVATAR_RAFAL to R.drawable.avatar_rafal,
        AVATAR_FILIP to R.drawable.avatar_filip,
        AVATAR_BELAL to R.drawable.avatar_belal,
    ),
) : ImageLoader {

    override val defaults = DefaultRequestOptions()
    override val components = ComponentRegistry()
    override val memoryCache: MemoryCache? get() = null
    override val diskCache: DiskCache? get() = null

    override fun enqueue(request: ImageRequest): Disposable {
        // Always call onStart before onSuccess.
        request.target?.onStart(request.placeholder)
        val result = createDrawable(context, request)
        if (result != null) {
            request.target?.onSuccess(result)
        }
        return object : Disposable {
            override val job = CompletableDeferred(newResult(request, result))
            override val isDisposed get() = true
            override fun dispose() {}
        }
    }

    override suspend fun execute(request: ImageRequest): ImageResult {
        return newResult(request, createDrawable(context, request))
    }

    private fun newResult(request: ImageRequest, drawable: Drawable?): ImageResult {
        return if (drawable != null) {
            SuccessResult(
                drawable = drawable,
                request = request,
                dataSource = DataSource.MEMORY_CACHE,
            )
        } else {
            ErrorResult(
                drawable = null,
                request = request,
                throwable = Exception(),
            )
        }
    }

    override fun newBuilder() = throw UnsupportedOperationException()

    override fun shutdown() {}

    private fun createDrawable(context: Context, request: ImageRequest): Drawable? {
        val avatarUrl = request.data as String

        return ContextCompat.getDrawable(
            context,
            userAvatars[avatarUrl] ?: return null,
        )!!.apply {
            // Workaround for an issue with black background behind clipped images
            (this as BitmapDrawable).bitmap.setHasAlpha(true)
        }
    }

    companion object {
        const val AVATAR_JC: String = "https://example.com/jc.jpeg"
        const val AVATAR_AMIT: String = "https://example.com/amit.jpeg"
        const val AVATAR_RAFAL: String = "https://example.com/rafal.jpeg"
        const val AVATAR_FILIP: String = "https://example.com/filip.jpeg"
        const val AVATAR_BELAL: String = "https://example.com/belal.jpeg"
    }
}
