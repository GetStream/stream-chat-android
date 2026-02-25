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

package io.getstream.chat.android.compose.sample.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongParameterList")
@Composable
internal fun MapBox(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    shape: Shape = RoundedCornerShape(12.dp),
    onClick: ((url: String) -> Unit)? = null,
    onLongClick: ((url: String) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val mapUrl = "https://www.openstreetmap.org/?" +
        "mlat=$latitude&mlon=$longitude#" +
        "map=15/$latitude/$longitude"

    Box(
        modifier = modifier.clip(shape),
    ) {
        MapWebView(
            modifier = modifier.matchParentSize(),
            latitude = latitude,
            longitude = longitude,
            showMarker = false,
            gesturesEnabled = false,
        )

        // WebView consumes all touch events by default (even when not visibly interactive).
        // so we need to ensure that the clickable area is on top of it.
        Box(
            modifier = Modifier
                .matchParentSize()
                .combinedClickable(
                    interactionSource = null,
                    enabled = onClick != null,
                    indication = ripple(),
                    onClick = { onClick?.invoke(mapUrl) },
                    onLongClick = { onLongClick?.invoke(mapUrl) },
                ),
        )

        content()
    }
}
