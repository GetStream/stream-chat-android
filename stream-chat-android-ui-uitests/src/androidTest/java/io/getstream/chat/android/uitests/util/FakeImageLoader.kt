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
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.memory.MemoryCache
import coil.request.DefaultRequestOptions
import coil.request.Disposable
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult
import io.getstream.chat.android.uitests.R

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
        AVATAR_USER to R.drawable.avatar_user,
    ),
) : ImageLoader {

    private val disposable = object : Disposable {
        override val isDisposed get() = true
        override fun dispose() {}
        override suspend fun await() {}
    }

    override val defaults = DefaultRequestOptions()

    // Optionally, you can add a custom fake memory cache implementation.
    override val memoryCache get() = throw UnsupportedOperationException()

    override val bitmapPool = BitmapPool(0)

    override fun enqueue(request: ImageRequest): Disposable {
        // Always call onStart before onSuccess.
        request.target?.onStart(placeholder = createDrawable(context, request))
        request.target?.onSuccess(result = createDrawable(context, request))
        return disposable
    }

    override suspend fun execute(request: ImageRequest): ImageResult {
        return SuccessResult(
            drawable = createDrawable(context, request),
            request = request,
            metadata = ImageResult.Metadata(
                memoryCacheKey = MemoryCache.Key(""),
                isSampled = false,
                dataSource = DataSource.MEMORY_CACHE,
                isPlaceholderMemoryCacheKeyPresent = false
            )
        )
    }

    override fun shutdown() {}

    override fun newBuilder() = ImageLoader.Builder(context)

    private fun createDrawable(context: Context, request: ImageRequest): Drawable {
        val data = request.data
        val avatarUrl = if (data is String) data else AVATAR_USER

        return ContextCompat.getDrawable(
            context,
            userAvatars.getOrDefault(avatarUrl, R.drawable.avatar_user)
        )!!
    }

    companion object {
        const val AVATAR_JC: String = "https://example.com/jc.jpeg"
        const val AVATAR_AMIT: String = "https://example.com/amit.jpeg"
        const val AVATAR_RAFAL: String = "https://example.com/rafal.jpeg"
        const val AVATAR_FILIP: String = "https://example.com/filip.jpeg"
        const val AVATAR_BELAL: String = "https://example.com/belal.jpeg"
        const val AVATAR_USER: String = "https://example.com/user.jpeg"
    }
}
