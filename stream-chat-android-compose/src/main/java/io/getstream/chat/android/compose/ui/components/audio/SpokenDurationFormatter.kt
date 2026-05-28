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

package io.getstream.chat.android.compose.ui.components.audio

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import androidx.core.os.ConfigurationCompat
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.helper.DurationFormatter
import java.util.Locale

/**
 * Builds spoken-form duration strings (e.g. "5 seconds", "1 minute 23 seconds") for use as
 * `contentDescription` so TalkBack reads `0:05` as natural language instead of character by
 * character.
 *
 * On API 24+ uses Android's ICU `MeasureFormat` for locale-aware natural language and caches
 * the formatter instance for the lifetime of this object. On older API levels falls back to
 * [fallbackFormatter] — the same clock format shown on screen.
 *
 * @param locale Locale used for natural-language output on API 24+.
 * @param fallbackFormatter Formatter used to render the duration on API levels below 24.
 */
internal class SpokenDurationFormatter(
    locale: Locale,
    fallbackFormatter: DurationFormatter,
) {
    private val formatStrategy: (Int) -> String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            icuFormatStrategy(locale)
        } else {
            fallbackFormatter::format
        }

    fun format(durationInMs: Int): String = formatStrategy(durationInMs)
}

@RequiresApi(Build.VERSION_CODES.N)
private fun icuFormatStrategy(locale: Locale): (Int) -> String {
    val formatter = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.WIDE)
    return { durationInMs ->
        val totalSeconds = (durationInMs / MillisPerSecond).coerceAtLeast(0L)
        val minutes = totalSeconds / SecondsPerMinute
        val seconds = totalSeconds % SecondsPerMinute
        when {
            minutes > 0 && seconds > 0 -> formatter.formatMeasures(
                Measure(minutes, MeasureUnit.MINUTE),
                Measure(seconds, MeasureUnit.SECOND),
            )
            minutes > 0 -> formatter.format(Measure(minutes, MeasureUnit.MINUTE))
            else -> formatter.format(Measure(seconds, MeasureUnit.SECOND))
        }
    }
}

/**
 * Returns a [SpokenDurationFormatter] for the current configuration, or `null` when no
 * accessibility service is enabled — call sites can skip computing the spoken description
 * entirely in that case.
 *
 * The formatter instance is `remember`ed so the underlying `MeasureFormat` is created once
 * per locale change, instead of on every recomposition.
 */
@Composable
internal fun rememberSpokenDurationFormatter(): SpokenDurationFormatter? {
    if (!rememberIsAccessibilityEnabled()) return null
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()
    val fallbackFormatter = ChatTheme.durationFormatter
    return remember(locale, fallbackFormatter) {
        SpokenDurationFormatter(locale, fallbackFormatter)
    }
}

@Composable
private fun rememberIsAccessibilityEnabled(): Boolean {
    val context = LocalContext.current
    val manager = remember(context) { context.getSystemService<AccessibilityManager>() } ?: return false
    var enabled by remember(manager) { mutableStateOf(manager.isEnabled) }
    DisposableEffect(manager) {
        val listener = AccessibilityManager.AccessibilityStateChangeListener { enabled = it }
        manager.addAccessibilityStateChangeListener(listener)
        enabled = manager.isEnabled
        onDispose { manager.removeAccessibilityStateChangeListener(listener) }
    }
    return enabled
}

private const val MillisPerSecond = 1000L
private const val SecondsPerMinute = 60L
