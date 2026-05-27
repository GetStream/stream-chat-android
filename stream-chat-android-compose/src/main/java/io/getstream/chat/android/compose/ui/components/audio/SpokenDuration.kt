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
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.helper.DurationFormatter
import java.util.Locale

/**
 * Spoken-form duration string (e.g. "5 seconds", "1 minute 23 seconds") suitable for
 * `contentDescription`. Visible UI keeps the compact `mm:ss` form; this helper produces the
 * speech-friendly equivalent so TalkBack doesn't read raw `0:05` character by character.
 *
 * Uses `MeasureFormat` (API 24+) for locale-aware natural-language output. On older API
 * levels falls back to the visible clock format — same TalkBack experience as today.
 *
 * @param durationInMs Duration to format, in milliseconds.
 */
@Composable
internal fun spokenDuration(durationInMs: Int): String =
    spokenDuration(
        durationInMs = durationInMs,
        locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault(),
        fallbackFormatter = ChatTheme.durationFormatter,
    )

/**
 * Non-Composable variant of [spokenDuration] for callers that build strings outside
 * Compose (e.g. quoted-message body builders).
 *
 * @param durationInMs Duration to format, in milliseconds.
 * @param locale Locale used for natural-language output on API 24+.
 * @param fallbackFormatter Formatter used to render the duration on API levels below 24.
 */
internal fun spokenDuration(
    durationInMs: Int,
    locale: Locale,
    fallbackFormatter: DurationFormatter,
): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        spokenDurationIcu(durationInMs, locale)
    } else {
        fallbackFormatter.format(durationInMs)
    }

@RequiresApi(Build.VERSION_CODES.N)
private fun spokenDurationIcu(durationInMs: Int, locale: Locale): String {
    val totalSeconds = (durationInMs / MillisPerSecond).coerceAtLeast(0L)
    val minutes = totalSeconds / SecondsPerMinute
    val seconds = totalSeconds % SecondsPerMinute
    val formatter = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.WIDE)
    return when {
        minutes > 0 && seconds > 0 -> formatter.formatMeasures(
            Measure(minutes, MeasureUnit.MINUTE),
            Measure(seconds, MeasureUnit.SECOND),
        )
        minutes > 0 -> formatter.format(Measure(minutes, MeasureUnit.MINUTE))
        else -> formatter.format(Measure(seconds, MeasureUnit.SECOND))
    }
}

private const val MillisPerSecond = 1000L
private const val SecondsPerMinute = 60L
