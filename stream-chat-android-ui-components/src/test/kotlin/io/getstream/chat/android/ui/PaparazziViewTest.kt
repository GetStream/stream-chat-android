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

package io.getstream.chat.android.ui

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import app.cash.paparazzi.Paparazzi
import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.Disposable
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
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

    // Light mode context
    private val lightContext get() = paparazzi.context
    // TODO Figure out how to properly support dark/light modes in a single paparazzi snapshot instance.
    /*.createConfigurationContext(Configuration().apply {
        uiMode = Configuration.UI_MODE_NIGHT_NO
    })*/

    // Dark mode context
    private val darkContext get() = paparazzi.context
    // TODO Figure out how to properly support dark/light modes in a single paparazzi snapshot instance.
    /*.createConfigurationContext(Configuration().apply {
        uiMode = Configuration.UI_MODE_NIGHT_YES
    })*/

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
        val context = if (isInDarkMode) darkContext else lightContext
        paparazzi.snapshot(view = viewFactory(context))
    }

    fun snapshotColumn(view: (context: Context) -> View) {
        snapshotLightAndDark(orientation = LinearLayout.VERTICAL, viewFactory = view)
    }

    fun snapshotRow(view: (context: Context) -> View) {
        snapshotLightAndDark(orientation = LinearLayout.HORIZONTAL, viewFactory = view)
    }

    private fun snapshotLightAndDark(
        @LinearLayout.OrientationMode orientation: Int,
        viewFactory: (Context) -> View,
    ) {
        val lightView = viewFactory(lightContext)
        // TODO Figure out how to properly support dark/light modes in a single paparazzi snapshot instance.
        // val darkView = viewFactory(darkContext)

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
            // TODO Figure out how to properly support dark/light modes in a single paparazzi snapshot instance.
            // addView(darkView, layoutParams)
        }

        paparazzi.snapshot(container)
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
