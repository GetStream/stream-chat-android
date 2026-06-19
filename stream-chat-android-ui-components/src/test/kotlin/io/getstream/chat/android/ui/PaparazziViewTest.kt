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

package io.getstream.chat.android.ui

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.InstantAnimationsRule
import app.cash.paparazzi.Paparazzi
import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.Disposable
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import com.android.resources.NightMode
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.common.images.internal.StreamCoil
import kotlinx.coroutines.CompletableDeferred
import org.junit.Before
import org.junit.Rule
import java.util.Date

internal abstract class PaparazziViewTest : MockedChatClientTest {

    @get:Rule
    abstract val paparazzi: Paparazzi

    @get:Rule
    val instantAnimations = InstantAnimationsRule()

    abstract val deviceConfig: DeviceConfig

    @Before
    fun prepare() {
        ChatUI.appContext = paparazzi.context
        ChatUI.dateFormatter = TestDateFormatter
        StreamCoil.setImageLoader { TestImageLoader }
    }

    fun snapshot(
        isInDarkMode: Boolean = false,
        viewFactory: (context: Context) -> View,
    ) {
        applyNightMode(isInDarkMode)
        paparazzi.snapshot(view = viewFactory(paparazzi.context))
    }

    fun snapshotColumn(view: (context: Context) -> View) {
        snapshotLightAndDark(orientation = LinearLayout.VERTICAL, viewFactory = view)
    }

    fun snapshotRow(view: (context: Context) -> View) {
        snapshotLightAndDark(orientation = LinearLayout.HORIZONTAL, viewFactory = view)
    }

    private fun snapshotLightAndDark(
        orientation: Int,
        viewFactory: (Context) -> View,
    ) {
        applyNightMode(false)
        val lightView = viewFactory(paparazzi.context)

        applyNightMode(true)
        val darkView = viewFactory(paparazzi.context)

        // Reset to light so the container and final snapshot render in light mode
        applyNightMode(false)

        val container = LinearLayout(paparazzi.context).apply {
            this.orientation = orientation
            val layoutParams = if (orientation == LinearLayout.VERTICAL) {
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    .5f,
                )
            } else {
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    .5f,
                )
            }
            addView(lightView, layoutParams)
            addView(darkView, layoutParams)
        }

        paparazzi.snapshot(container)
    }

    private fun applyNightMode(isDark: Boolean) {
        paparazzi.unsafeUpdateConfig(
            deviceConfig = deviceConfig.copy(
                nightMode = if (isDark) NightMode.NIGHT else NightMode.NOTNIGHT,
            ),
        )
    }
}

private object TestDateFormatter : DateFormatter {
    private const val TIME = "13:49"
    override fun formatDate(date: Date?): String = TIME
    override fun formatTime(date: Date?): String = TIME
    override fun formatRelativeDate(date: Date): String = "Today"
    override fun formatRelativeTime(date: Date?): String = "Just now"
}

private object TestImageLoader : ImageLoader {

    override val defaults = ImageRequest.Defaults()
    override val components = ComponentRegistry()
    override val memoryCache: MemoryCache? get() = null
    override val diskCache: DiskCache? get() = null

    override fun enqueue(request: ImageRequest): Disposable {
        request.target?.onStart(request.placeholder())
        return object : Disposable {
            override val job = CompletableDeferred(value = errorResult(request))
            override val isDisposed get() = true
            override fun dispose() = Unit
        }
    }

    override suspend fun execute(request: ImageRequest): ImageResult = errorResult(request)

    override fun newBuilder() = throw UnsupportedOperationException()

    override fun shutdown() = Unit

    private fun errorResult(request: ImageRequest) = ErrorResult(
        image = null,
        request = request,
        throwable = RuntimeException(),
    )
}
