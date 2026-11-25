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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Renders a chart diagram using Chart.js in WebView.
 * Chart.js is a popular JavaScript charting library with a standard JSON configuration format.
 *
 * **Chart.js Configuration Format:**
 * The chartCode should be a valid Chart.js configuration JSON object.
 *
 * Example for line chart:
 * {
 *   "type": "line",
 *   "data": {
 *     "labels": ["Jan", "Feb", "Mar", "Apr", "May"],
 *     "datasets": [{
 *       "label": "Sales",
 *       "data": [10, 20, 15, 25, 30],
 *       "borderColor": "rgb(75, 192, 192)",
 *       "backgroundColor": "rgba(75, 192, 192, 0.2)"
 *     }]
 *   },
 *   "options": {
 *     "responsive": true,
 *     "maintainAspectRatio": false
 *   }
 * }
 *
 * Chart.js supports many chart types: line, bar, pie, doughnut, radar, polarArea, bubble, scatter, etc.
 * See https://www.chartjs.org/docs/latest/ for full documentation.
 *
 * @param chartJsJson The Chart.js configuration JSON string
 * @param modifier Modifier to be applied to the chart
 */
@Composable
internal fun ChartJsDiagram(
    chartJsJson: String,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context -> ChartWebView(context, loadChartJsFromAssets(context)) },
        update = { webView -> webView.show(chartJsJson) },
        onRelease = { webView -> webView.destroy() },
    )
}

/**
 * A WebView showing one chart. The page runs no scripts of its own: Chart.js and each chart's
 * configuration go in through [evaluateJavascript], so nothing in a configuration can add markup
 * that runs. Without the Chart.js asset it stays empty.
 */
// JavaScript runs only the scripts the app evaluates, and the view is only built in code.
@SuppressLint("SetJavaScriptEnabled", "ViewConstructor")
private class ChartWebView(context: Context, private val chartJsCode: String?) : WebView(context) {

    private var config: String? = null
    private var ready = false

    init {
        setBackgroundColor(Color.TRANSPARENT)
        settings.javaScriptEnabled = true
        settings.setSupportZoom(false)
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        webViewClient = object : WebViewClient() {
            // The chart page never navigates anywhere.
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = true

            // Android 6 calls only this overload.
            @Suppress("OVERRIDE_DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean = true

            override fun onPageFinished(view: WebView, url: String?) {
                chartJsCode?.let { view.evaluateJavascript(it) { onChartJsLoaded() } }
            }
        }
        loadDataWithBaseURL(null, ChartPage, "text/html", "UTF-8", null)
    }

    fun show(chartJsJson: String) {
        if (chartJsJson == config) return
        config = chartJsJson
        if (ready) render(chartJsJson)
    }

    private fun onChartJsLoaded() {
        ready = true
        config?.let(::render)
    }

    private fun render(chartJsJson: String) {
        evaluateJavascript(renderChartScript(chartJsJson), null)
    }
}

/** The page a chart draws on. Its policy lets no script run but those the app evaluates. */
private val ChartPage = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline'; img-src data:;">
        <style>
            body { margin: 0; padding: 8px; background: transparent; }
            #chartContainer { width: 100%; height: 100%; min-height: 200px; }
            .chartError { color: red; padding: 20px; }
        </style>
    </head>
    <body><div id="chartContainer"></div></body>
    </html>
""".trimIndent()

/**
 * The script that draws the chart [chartJsJson] configures, replacing the one shown before, or
 * shows why it can't as text.
 */
@VisibleForTesting
internal fun renderChartScript(chartJsJson: String): String = """
    (function(json) {
        const container = document.getElementById('chartContainer');
        try {
            if (window.chartInstance) {
                window.chartInstance.destroy();
                window.chartInstance = null;
            }
            const canvas = document.createElement('canvas');
            container.textContent = '';
            container.appendChild(canvas);
            window.chartInstance = new Chart(canvas.getContext('2d'), JSON.parse(json));
        } catch (error) {
            console.error('Chart.js error:', error);
            const message = document.createElement('p');
            message.className = 'chartError';
            message.textContent = 'Error rendering chart: ' + error.message;
            container.textContent = '';
            container.appendChild(message);
        }
    })(${chartJsJson.toJsStringLiteral()});
""".trimIndent()

private fun loadChartJsFromAssets(context: Context): String? =
    try {
        context.assets.open("chart.umd.min.js").use { it.readBytes().decodeToString() }
    } catch (_: Exception) {
        null
    }

/** This text as a double-quoted JavaScript string literal. */
@VisibleForTesting
internal fun String.toJsStringLiteral(): String = buildString(length + 2) {
    append('"')
    for (char in this@toJsStringLiteral) {
        when (char) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (char < ' ' || char.code == LineSeparator || char.code == ParagraphSeparator) {
                append("\\u").append(char.code.toString(HexRadix).padStart(UnicodeEscapeDigits, '0'))
            } else {
                append(char)
            }
        }
    }
    append('"')
}

private const val HexRadix = 16

// JavaScript engines before ES2019 end a string literal at these.
private const val LineSeparator = 0x2028
private const val ParagraphSeparator = 0x2029
private const val UnicodeEscapeDigits = 4
