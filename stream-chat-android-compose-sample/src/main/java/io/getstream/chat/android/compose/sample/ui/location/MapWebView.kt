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

package io.getstream.chat.android.compose.sample.ui.location

import android.graphics.Color
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView

@Composable
internal fun MapWebView(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
) {
    var htmlContent by remember { mutableStateOf<String?>(null) }
    var viewSize by remember { mutableStateOf<IntSize?>(null) }
    val density = LocalDensity.current

    WebView.setWebContentsDebuggingEnabled(true)

    AndroidView(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val size = coordinates.size
            if (viewSize != size) {
                viewSize = size

                val pxPerDp = density.density // e.g., ~2.75
                // Convert back to "CSS pixels":
                val cssWidth = size.width / pxPerDp
                val cssHeight = size.height / pxPerDp

                htmlContent = mapHtml(
                    latitude = latitude,
                    longitude = longitude,
                    cssWidth = cssWidth,
                    cssHeight = cssHeight,
                )
            }
        },
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                // settings.allowFileAccess = true
                // settings.allowContentAccess = true
                // settings.domStorageEnabled = true
                setBackgroundColor(Color.TRANSPARENT)
            }
        },
        update = { webView ->
            htmlContent?.let { data ->
                webView.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null)
            }
        },
    )
}

private fun mapHtml(
    latitude: Double,
    longitude: Double,
    cssWidth: Float,
    cssHeight: Float,
): String = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css"/>
        <style>
            html, body { margin:0; padding:0; }
            #map { width:${cssWidth}px; height:${cssHeight}px; }
        </style>
    </head>
    <body>
    <div id="map"></div>
    <script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>
    <script>
        var map = L.map('map').setView([$latitude, $longitude], 15);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '&copy; OpenStreetMap contributors'
        }).addTo(map);
        L.marker([$latitude, $longitude]).addTo(map);
    </script>
    </body>
    </html>
""".trimIndent()
