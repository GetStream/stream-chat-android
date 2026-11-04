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

package io.getstream.chat.android.ai.openui.rendering

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.getstream.chat.android.ai.openui.icons.Circle
import io.getstream.chat.android.ai.openui.icons.iconCategoryFallbacks
import io.getstream.chat.android.ai.openui.icons.lucideIcons
import io.getstream.chat.android.ai.openui.lang.ElementNode
import io.getstream.chat.android.ai.openui.lang.stringValue

/**
 * Maps lucide icon names (what OpenUI's `Icon(name, category)` uses) to Material Symbols.
 *
 * Resolution follows the reference renderer: the exact name, then looser variants of it (without
 * numeric or variant suffixes), then the category's representative icon, then a generic one.
 */
internal object OpenUIIcons {

    /** The icon for a lucide [name] and optional [category]. */
    fun vector(name: String?, category: String? = null): ImageVector {
        val key = name.orEmpty().trim().lowercase()
        lucideIcons[key]?.let { return it() }
        for (candidate in looseCandidates(key)) lucideIcons[candidate]?.let { return it() }
        val fallback = iconCategoryFallbacks[category.orEmpty().trim().lowercase()]
        fallback?.let { lucideIcons[it] }?.let { return it() }
        return Icons.Rounded.Circle
    }

    /** Whether [name] resolves without falling back to its category. */
    fun isKnown(name: String?): Boolean {
        val key = name.orEmpty().trim().lowercase()
        return key in lucideIcons || looseCandidates(key).any { it in lucideIcons }
    }

    /**
     * The words a screen reader says for a lucide [name] ("trash-2" reads "trash", "arrow-up-right"
     * reads "arrow up right"), or `null` when the name has none.
     */
    fun label(name: String?): String? = name.orEmpty()
        .split('-', '_', ' ')
        .filter { it.isNotBlank() && it.toIntOrNull() == null }
        .joinToString(" ")
        .lowercase()
        .ifEmpty { null }

    internal fun looseCandidates(key: String): List<String> {
        val parts = key.split('-').toMutableList()
        val candidates = mutableListOf<String>()
        while (parts.size > 1) {
            parts.removeAt(parts.lastIndex)
            candidates += parts.joinToString("-")
        }
        val withoutDigits = key.split('-').filter { it.toIntOrNull() == null }.joinToString("-")
        if (withoutDigits != key) candidates.add(0, withoutDigits)
        return candidates
    }
}

/**
 * An icon from a lucide name (or an `Icon(name, category)` element). Screen readers say its name as
 * words, unless it is [decorative]: next to text, or inside a control that has its own label.
 */
@Composable
internal fun OpenUIIcon(
    name: String?,
    modifier: Modifier = Modifier,
    category: String? = null,
    tint: Color = LocalContentColor.current,
    decorative: Boolean = false,
) {
    Icon(
        OpenUIIcons.vector(name, category),
        contentDescription = if (decorative) null else OpenUIIcons.label(name),
        modifier = modifier,
        tint = tint,
    )
}

/** An icon from an `Icon(name, category)` element. */
@Composable
internal fun OpenUIIcon(
    element: ElementNode?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    decorative: Boolean = false,
) {
    OpenUIIcon(
        element?.props?.get("name").stringValue,
        modifier,
        element?.props?.get("category").stringValue,
        tint,
        decorative,
    )
}
