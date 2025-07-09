/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@Suppress("LongParameterList")
@Composable
internal fun MapBox(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    contentDescription: String,
    contentScale: ContentScale = ContentScale.Fit,
    shape: Shape = ChatTheme.shapes.attachment,
    onClick: ((url: String) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .clickable(
                interactionSource = null,
                enabled = onClick != null,
                indication = ripple(),
                onClick = {
                    onClick?.invoke(
                        "https://www.openstreetmap.org/?" +
                            "mlat=$latitude&mlon=$longitude#" +
                            "map=15/$latitude/$longitude",
                    )
                },
            ),
    ) {
        val data = "https://static-maps.yandex.ru/1.x/?lang=en-US&" +
            "ll=$longitude,$latitude&" +
            "z=15&size=450,450&l=map"
        AsyncImage(
            modifier = Modifier.matchParentSize(),
            model = data,
            contentDescription = contentDescription,
            contentScale = contentScale,
        )
        content()
    }
}
