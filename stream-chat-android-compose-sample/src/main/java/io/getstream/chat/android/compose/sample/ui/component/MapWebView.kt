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

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun MapWebView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    showMarker: Boolean = true,
    gesturesEnabled: Boolean = true,
) {
    var htmlContent by remember { mutableStateOf<String?>(null) }
    var viewSize by remember { mutableStateOf<IntSize?>(null) }
    val density = LocalDensity.current
    val markerColor = ChatTheme.colors.primaryAccent.toHexString()

    LaunchedEffect(viewSize) {
        // Ensure the HTML content is updated when the view size changes
        viewSize?.let { size ->
            val pxPerDp = density.density // e.g., ~2.75
            // Convert back to "CSS pixels":
            val cssWidth = size.width / pxPerDp
            val cssHeight = size.height / pxPerDp
            htmlContent = mapHtml(
                latitude = latitude,
                longitude = longitude,
                cssWidth = cssWidth,
                cssHeight = cssHeight,
                showMarker = showMarker,
                markerColor = markerColor,
                gesturesEnabled = gesturesEnabled,
            )
        }
    }

    AndroidView(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val size = coordinates.size
                if (viewSize != size) {
                    viewSize = size
                }
            },
        factory = { context ->
            WebView(context).apply {
                // Disable hardware acceleration to avoid issues with WebView rendering on some devices
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                }
                // Enable JavaScript for Leaflet to work properly
                settings.javaScriptEnabled = true
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
    showMarker: Boolean,
    markerColor: String = "#FF0000",
    gesturesEnabled: Boolean,
): String = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css"/>
        <style>
            html, body { margin:0; padding:0; }
            #map { width:${cssWidth}px; height:${cssHeight}px; }
            .leaflet-control-attribution {
              font-size: 10px !important;
            }
        </style>
    </head>
    <body>
    <div id="map"></div>
    <script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>
    <script>
        var map = L.map('map', {
          zoomControl: $gesturesEnabled,
          scrollWheelZoom: $gesturesEnabled,
          doubleClickZoom: $gesturesEnabled,
          boxZoom: $gesturesEnabled,
          keyboard: $gesturesEnabled,
          touchZoom: $gesturesEnabled
        }).setView([$latitude, $longitude], 15);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '&copy; OpenStreetMap contributors'
        }).addTo(map);
        var marker = null;
        if ($showMarker) {
          var icon = L.divIcon({
            className: '',
            html: `
              <div style="
                width: 20px;
                height: 20px;
                background: $markerColor;
                border: 3px solid white;
                border-radius: 50%;
                box-sizing: border-box;
              "></div>
            `,
            iconSize: [20, 20],
            iconAnchor: [10, 10] // center the dot
          })
          marker = L.marker([$latitude, $longitude], { icon: icon }).addTo(map);
        }
        function updateLocation(lat, lng) {
          if (marker) {
            marker.setLatLng([lat, lng]);
          }
          map.setView([lat, lng]);
        }
    </script>
    </body>
    </html>
""".trimIndent()

@Suppress("ImplicitDefaultLocale", "MagicNumber")
private fun Color.toHexString(): String {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}
