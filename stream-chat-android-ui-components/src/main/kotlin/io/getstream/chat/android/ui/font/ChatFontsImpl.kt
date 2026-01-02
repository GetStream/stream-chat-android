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

package io.getstream.chat.android.ui.font

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import io.getstream.log.taggedLogger
import java.util.HashMap

internal class ChatFontsImpl(
    private val style: ChatStyle,
    private val context: Context,
) : ChatFonts {

    private val resourceMap: MutableMap<Int, Typeface> = HashMap()
    private val pathMap: MutableMap<String, Typeface> = HashMap()

    private val logger by taggedLogger(ChatFonts::class.java.simpleName)

    override fun setFont(textStyle: TextStyle, textView: TextView) {
        if (textStyle.font != null) {
            textView.setTypeface(textStyle.font, textStyle.style)
        } else {
            setDefaultFont(textView, textStyle.style, null)
        }
    }

    override fun setFont(textStyle: TextStyle, textView: TextView, defaultTypeface: Typeface) {
        if (textStyle.font != null) {
            textView.setTypeface(textStyle.font, textStyle.style)
        } else {
            setDefaultFont(textView, textStyle.style, defaultTypeface)
        }
    }

    override fun getFont(textStyle: TextStyle): Typeface? {
        return when {
            textStyle.fontResource != -1 ->
                getFont(textStyle.fontResource)
            !textStyle.fontAssetsPath.isNullOrEmpty() ->
                getFont(textStyle.fontAssetsPath)
            else -> null
        }
    }

    private fun getFont(fontPath: String): Typeface? {
        if (fontPath in pathMap) {
            return pathMap[fontPath]
        }

        val typeface = safeLoadTypeface(fontPath) ?: return null
        pathMap[fontPath] = typeface
        return typeface
    }

    private fun getFont(@FontRes fontRes: Int): Typeface? {
        if (fontRes in resourceMap) {
            return resourceMap[fontRes]
        }

        val typeface = safeLoadTypeface(fontRes) ?: return null
        resourceMap.put(fontRes, typeface)
        return typeface
    }

    private fun setDefaultFont(textView: TextView, textStyle: Int, defaultTypeface: Typeface?) {
        if (style.hasDefaultFont()) {
            style.defaultTextStyle?.let {
                textView.setTypeface(getFont(it), textStyle)
            }
        } else {
            textView.setTypeface(defaultTypeface ?: Typeface.DEFAULT, textStyle)
        }
    }

    private fun safeLoadTypeface(@FontRes fontRes: Int): Typeface? {
        return try {
            ResourcesCompat.getFont(context, fontRes)
        } catch (t: Throwable) {
            logger.e(t) { "[safeLoadTypeface] failed: $t" }
            null
        }
    }

    private fun safeLoadTypeface(fontPath: String): Typeface? {
        return try {
            Typeface.createFromAsset(context.assets, fontPath)
        } catch (t: Throwable) {
            logger.e(t) { "[safeLoadTypeface] failed: $t" }
            null
        }
    }
}
