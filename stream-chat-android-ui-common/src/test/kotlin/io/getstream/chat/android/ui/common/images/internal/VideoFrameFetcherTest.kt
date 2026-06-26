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

package io.getstream.chat.android.ui.common.images.internal

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.Extras
import coil3.ImageLoader
import coil3.request.Options
import coil3.toUri
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class VideoFrameFetcherTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val factory = VideoFrameFetcher.Factory()
    private val uri = "https://cdn.example.com/video.mp4".toUri()

    @Test
    fun `creates a fetcher when the request is marked as a video preview`() {
        val fetcher = factory.create(uri, optionsWith(videoPreview = true), mock<ImageLoader>())

        assertNotNull(fetcher)
    }

    @Test
    fun `skips requests that are not marked as a video preview`() {
        val fetcher = factory.create(uri, optionsWith(videoPreview = false), mock<ImageLoader>())

        assertNull(fetcher)
    }

    private fun optionsWith(videoPreview: Boolean): Options {
        val extras = Extras.Builder().set(videoFramePreviewKey, videoPreview).build()
        return Options(context = context, extras = extras)
    }
}
