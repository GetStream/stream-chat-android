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

package io.getstream.chat.android.compose.ui

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.Density
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import org.junit.Rule

/**
 * [DeviceConfig.PIXEL_2] geometry (411x731dp) at hdpi. The dp layout is identical to the
 * Pixel 2, but rendering at 1.5x keeps the golden files small, which speeds up comparisons.
 */
internal val PIXEL_2_HDPI = DeviceConfig.PIXEL_2.atHdpi()

/**
 * [DeviceConfig.PIXEL_4A] geometry (392x850dp) at hdpi, for snapshots that need a taller screen.
 */
internal val PIXEL_4A_HDPI = DeviceConfig.PIXEL_4A.atHdpi()

/**
 * A copy of this device with the same dp geometry rendered at 1.5x instead of the native density.
 */
private fun DeviceConfig.atHdpi(): DeviceConfig = copy(
    screenWidth = screenWidth * Density.HIGH.dpiValue / density.dpiValue,
    screenHeight = screenHeight * Density.HIGH.dpiValue / density.dpiValue,
    xdpi = Density.HIGH.dpiValue,
    ydpi = Density.HIGH.dpiValue,
    density = Density.HIGH,
)

/**
 * API level Paparazzi renders at. Defaults to 36 — Paparazzi 2.0.0-alpha05's stable ceiling — while
 * the module still compiles against 37. Override per run with `-Ppaparazzi.compileSdk=NN`, which
 * build.gradle.kts forwards as this system property (e.g. to record/verify at 35 or 37). Rendering
 * above 36 is unstable on alpha05.
 */
internal val paparazziCompileSdk: Int =
    System.getProperty("paparazzi.compileSdk")?.toIntOrNull() ?: 36

/**
 * The two known Paparazzi 2.0.0-alpha05 / layoutlib 16.2.1 bugs. In both, the snapshot is already
 * captured when the error is thrown during render teardown / on a background thread, so swallowing
 * them yields a valid golden. Both are fixed by layoutlib 16.2.3 (Paparazzi alpha05.2 / alpha06).
 *  - https://github.com/cashapp/paparazzi/issues/2342 — HandlerThread → Thread.setPosixNicenessInternal
 *  - https://github.com/cashapp/paparazzi/issues/2373 — Dialog/Popup teardown → WindowManagerImpl.removeView NPE
 * Remove this (and its call sites) once Paparazzi ships layoutlib >= 16.2.3.
 */
private fun Throwable.isPaparazziAlpha05LayoutlibBug(): Boolean =
    generateSequence(this) { it.cause }.any { t ->
        (t is NoSuchMethodError && t.message?.contains("setPosixNicenessInternal") == true) ||
            (
                t is NullPointerException && t.stackTrace.any {
                    it.className == "android.view.WindowManagerImpl" && it.methodName == "removeView"
                }
                )
    }

/**
 * Removes the known layoutlib-16.2.1 bug errors from Paparazzi's internal logger.
 *
 * The #2342 `setPosixNicenessInternal` error is raised on a background HandlerThread during render,
 * captured into `PaparazziSdk.logger`, and re-thrown at rule teardown (`close()`) — outside the
 * `snapshot()` call — so a try/catch around `snapshot()` cannot reach it. We reach into the logger
 * and drop those entries so teardown stays clean; the snapshot itself is already captured. Reflective
 * because the fields are internal; guarded so it degrades to a no-op if Paparazzi's internals change.
 * Remove once Paparazzi ships layoutlib >= 16.2.3 (alpha05.2 / alpha06).
 */
private fun Paparazzi.dropKnownLayoutlibBugErrors() {
    runCatching {
        val sdk = Paparazzi::class.java.getDeclaredField("sdk")
            .apply { isAccessible = true }.get(this) ?: return
        val logger = sdk.javaClass.getDeclaredField("logger")
            .apply { isAccessible = true }.get(sdk)
        val errors = logger.javaClass.getDeclaredField("errors")
            .apply { isAccessible = true }.get(logger) as? MutableList<*> ?: return
        errors.removeAll { it is Throwable && it.isPaparazziAlpha05LayoutlibBug() }
    }
}

internal interface PaparazziComposeTest : MockedChatClientTest {

    @get:Rule
    val paparazzi: Paparazzi

    /**
     * Builds a Paparazzi rule that renders at API 36 while the module keeps compileSdk 37.
     *
     * Paparazzi 2.0.0-alpha05's layoutlib is unstable rendering at API 37 — e.g. any HandlerThread
     * hits `Thread.setPosixNicenessInternal`, and Popup dismissal hits a null WindowManager child
     * list. API 36 is alpha05's stable ceiling. Overriding only the environment's compileSdkVersion
     * keeps the build on 37 and renders/records goldens at 36. Remove once Paparazzi supports 37.
     */
    fun createPaparazzi(
        deviceConfig: DeviceConfig = PIXEL_2_HDPI,
        renderingMode: SessionParams.RenderingMode = SessionParams.RenderingMode.NORMAL,
    ): Paparazzi = Paparazzi(
        environment = detectEnvironment().copy(compileSdkVersion = paparazziCompileSdk),
        deviceConfig = deviceConfig,
        renderingMode = renderingMode,
    )

    /**
     * Wraps [Paparazzi.snapshot], swallowing the known alpha05/layoutlib-16.2.1 teardown bugs
     * (see [isPaparazziAlpha05LayoutlibBug]). The snapshot is captured before they throw.
     */
    private fun snapshotSwallowingLayoutlibBugs(content: @Composable () -> Unit) {
        try {
            paparazzi.snapshot(composable = content)
        } catch (t: Throwable) {
            if (!t.isPaparazziAlpha05LayoutlibBug()) throw t
        }
        // #2342 is captured on a background thread and re-thrown at rule teardown, outside the
        // try above — drop it from the logger here so close() stays clean.
        paparazzi.dropKnownLayoutlibBugErrors()
    }

    fun snapshot(
        isInDarkMode: Boolean = false,
        contentAlignment: Alignment = Alignment.TopStart,
        backgroundColor: Color = Color.Unspecified,
        composable: @Composable () -> Unit,
    ) {
        snapshotSwallowingLayoutlibBugs {
            TestEnvironment {
                ChatTheme(isInDarkMode = isInDarkMode) {
                    Box(
                        modifier = Modifier
                            .background(backgroundColor.takeOrElse(ChatTheme.colors::backgroundCoreApp)),
                        contentAlignment = contentAlignment,
                    ) {
                        composable()
                    }
                }
            }
        }
    }

    fun snapshotWithDarkMode(
        contentAlignment: Alignment = Alignment.TopStart,
        composable: @Composable () -> Unit,
    ) {
        snapshotSwallowingLayoutlibBugs {
            TestEnvironment {
                Column {
                    ChatTheme(isInDarkMode = true) {
                        Box(
                            modifier = Modifier
                                .weight(weight = .5f, fill = false)
                                .background(ChatTheme.colors.backgroundCoreApp),
                            contentAlignment = contentAlignment,
                        ) {
                            composable()
                        }
                    }
                    ChatTheme(isInDarkMode = false) {
                        Box(
                            modifier = Modifier
                                .weight(weight = .5f, fill = false)
                                .background(ChatTheme.colors.backgroundCoreApp),
                            contentAlignment = contentAlignment,
                        ) {
                            composable()
                        }
                    }
                }
            }
        }
    }

    fun snapshotWithDarkModeRow(
        contentAlignment: Alignment = Alignment.TopStart,
        composable: @Composable () -> Unit,
    ) {
        snapshotSwallowingLayoutlibBugs {
            TestEnvironment {
                Row {
                    ChatTheme(isInDarkMode = true) {
                        Box(
                            modifier = Modifier
                                .weight(weight = .5f, fill = false)
                                .background(ChatTheme.colors.backgroundCoreApp),
                            contentAlignment = contentAlignment,
                        ) {
                            composable()
                        }
                    }
                    ChatTheme(isInDarkMode = false) {
                        Box(
                            modifier = Modifier
                                .weight(weight = .5f, fill = false)
                                .background(ChatTheme.colors.backgroundCoreApp),
                            contentAlignment = contentAlignment,
                        ) {
                            composable()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestEnvironment(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalInspectionMode provides true,
        LocalViewModelStoreOwner provides FakeViewModelStoreOwner,
        LocalOnBackPressedDispatcherOwner provides FakeBackDispatcherOwner,
        LocalActivityResultRegistryOwner provides NoOpRegistryOwner,
        content = content,
    )
}

/**
 * A fake [ViewModelStoreOwner] necessary for composable components that use [ViewModel].
 */
private val FakeViewModelStoreOwner = object : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}

/**
 * A fake [OnBackPressedDispatcherOwner] necessary for composable components that use [BackHandler].
 */
private val FakeBackDispatcherOwner = object : OnBackPressedDispatcherOwner {
    private val dispatcher = OnBackPressedDispatcher()

    override val onBackPressedDispatcher: OnBackPressedDispatcher = dispatcher

    override val lifecycle: Lifecycle = LifecycleRegistry.createUnsafe(this).apply {
        currentState = Lifecycle.State.RESUMED
    }
}

/**
 * A no-op [ActivityResultRegistryOwner] necessary for composable components that use
 * [androidx.activity.compose.rememberLauncherForActivityResult].
 */
private val NoOpRegistryOwner = object : ActivityResultRegistryOwner {
    override val activityResultRegistry: ActivityResultRegistry
        get() = NoOpActivityResultRegistry
}

private val NoOpActivityResultRegistry = object : ActivityResultRegistry() {
    override fun <I, O> onLaunch(
        requestCode: Int,
        contract: ActivityResultContract<I, O>,
        input: I,
        options: ActivityOptionsCompat?,
    ) {
        // no-op
    }
}
