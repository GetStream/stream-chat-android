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

package io.getstream.chat.android.ui.common.feature.messages.composer.transliteration

import android.icu.text.Transliterator
import android.os.Build
import androidx.annotation.RequiresApi
import io.getstream.log.taggedLogger

/**
 * Default implementation for [StreamTransliterator]. This class uses the native transliteration provided by Android.
 * Requires Android Q or higher.
 */
public class DefaultStreamTransliterator(transliterationId: String? = null) : StreamTransliterator {

    private val logger by taggedLogger("Chat:Transliterator")

    private var transliterator: Transliterator? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            transliterationId?.let(::setTransliterator)
        } else {
            logger.d {
                "This android version: ${Build.VERSION.SDK_INT} doesn't support transliteration natively. " +
                    "User a custom StreamTransliterator to add transliteration."
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setTransliterator(id: String) {
        if (Transliterator.getAvailableIDs().asSequence().contains(id)) {
            this.transliterator = Transliterator.getInstance(id)
        } else {
            logger.d { "The id: $id for transliteration is not available" }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun transliterate(text: String): String {
        return try {
            transliterator?.transliterate(text)?.also {
                logger.v { "[transliterate] input: $text, output: $it" }
            } ?: text
        } catch (e: Exception) {
            logger.e(e) { "[transliterate] failed($text): $e" }
            text
        }
    }
}
