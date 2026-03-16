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

package io.getstream.chat.android.uitests.util

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import coil3.ComponentRegistry
import coil3.Image
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.Disposable
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
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
        AVATAR_LEIA to R.drawable.avatar_leia,
        AVATAR_ANAKIN to R.drawable.avatar_anakin,
        AVATAR_CHEWBACCA to R.drawable.avatar_chewbacca,
        AVATAR_HAN to R.drawable.avatar_han,
        AVATAR_ALEX to R.drawable.avatar_alex,
        AVATAR_SARAH to R.drawable.avatar_sarah,
        AVATAR_ELENA to R.drawable.avatar_elena,
        AVATAR_MARCO to R.drawable.avatar_marco,
        AVATAR_PRIYA to R.drawable.avatar_priya,
        AVATAR_JAMES to R.drawable.avatar_james,
        AVATAR_AISHA to R.drawable.avatar_aisha,
        AVATAR_LUCAS to R.drawable.avatar_lucas,
        AVATAR_MAYA to R.drawable.avatar_maya,
        AVATAR_OMAR to R.drawable.avatar_omar,
        AVATAR_DAVID to R.drawable.avatar_david,
        AVATAR_NINA to R.drawable.avatar_nina,
    ),
) : ImageLoader {

    override val defaults = ImageRequest.Defaults()
    override val components = ComponentRegistry()
    override val memoryCache: MemoryCache? get() = null
    override val diskCache: DiskCache? get() = null

    override fun enqueue(request: ImageRequest): Disposable {
        // Always call onStart before onSuccess.
        request.target?.onStart(request.placeholder())
        val result = createDrawableImage(context, request)
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
        return newResult(request, createDrawableImage(context, request))
    }

    private fun newResult(request: ImageRequest, image: Image?): ImageResult {
        return if (image != null) {
            SuccessResult(
                image = image,
                request = request,
                dataSource = DataSource.MEMORY_CACHE,
            )
        } else {
            ErrorResult(
                image = null,
                request = request,
                throwable = Exception(),
            )
        }
    }

    override fun newBuilder() = throw UnsupportedOperationException()

    override fun shutdown() {}

    private fun createDrawableImage(context: Context, request: ImageRequest): Image? {
        val avatarUrl = request.data as String

        return ContextCompat.getDrawable(
            context,
            userAvatars[avatarUrl] ?: return null,
        )!!.apply {
            // Workaround for an issue with black background behind clipped images
            (this as BitmapDrawable).bitmap.setHasAlpha(true)
        }.asImage()
    }

    companion object {
        const val AVATAR_JC: String = "https://example.com/jc.jpeg"
        const val AVATAR_LEIA: String = "https://example.com/leia.jpeg"
        const val AVATAR_ANAKIN: String = "https://example.com/anakin.jpeg"
        const val AVATAR_CHEWBACCA: String = "https://example.com/chewbacca.jpeg"
        const val AVATAR_HAN: String = "https://example.com/belal.jpeg"
        const val AVATAR_ALEX: String = "https://images.unsplash.com/photo-1560250097-0b93528c311a?w=300&h=300&fit=crop&crop=top"
        const val AVATAR_SARAH: String = "https://images.unsplash.com/photo-1548142813-c348350df52b?w=200&h=200&fit=crop&crop=face"
        const val AVATAR_ELENA: String = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300&h=300&fit=crop&crop=top"
        const val AVATAR_MARCO: String = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&h=300&fit=crop&crop=top"
        const val AVATAR_PRIYA: String = "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=200&h=200&fit=crop&crop=face"
        const val AVATAR_JAMES: String = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&h=200&fit=crop&crop=face"
        const val AVATAR_AISHA: String = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=300&h=300&fit=crop&crop=top"
        const val AVATAR_LUCAS: String = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300&h=300&fit=crop&crop=top"
        const val AVATAR_MAYA: String = "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=300&h=300&fit=crop&crop=top"
        const val AVATAR_OMAR: String = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300&h=300&fit=crop&crop=top"
        const val AVATAR_DAVID: String = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300&h=300&fit=crop&crop=top"
        const val AVATAR_NINA: String = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&h=200&fit=crop&crop=face"
    }
}
