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

import android.content.Context
import android.graphics.Color
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    val htmlContent = remember(chartJsJson) {
        buildHtmlContent(chartJsJson.escapeJson())
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val chartJsCode = loadChartJsFromAssets(context)
            createWebView(context, htmlContent, chartJsCode)
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        },
    )
}

private fun buildHtmlContent(escapedJson: String) = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="Content-Security-Policy" content="default-src 'self'; script-src 'unsafe-inline' 'unsafe-eval';
            style-src 'unsafe-inline'; img-src data:; connect-src 'none';">
        <style>
            body { margin: 0; padding: 8px; background: transparent; }
            #chartContainer { width: 100%; height: 100%; min-height: 200px; }
        </style>
    </head>
    <body>
        <div id="chartContainer"><canvas id="chartCanvas"></canvas></div>
        <script>
            window.initChart = function() {
                try {
                    if (window.chartInstance) {
                        window.chartInstance.destroy();
                        window.chartInstance = null;
                    }
                    const config = JSON.parse("$escapedJson");
                    const ctx = document.getElementById('chartCanvas').getContext('2d');
                    window.chartInstance = new Chart(ctx, config);
                } catch (error) {
                    console.error('Chart.js error:', error);
                    document.getElementById('chartContainer').innerHTML =
                        '<p style="color: red; padding: 20px;">Error rendering chart: ' + error.message + '</p>';
                }
            };
        </script>
    </body>
    </html>
""".trimIndent()

private fun loadChartJsFromAssets(context: Context): String? =
    try {
        context.assets.open("chart.umd.min.js").use { it.readBytes().decodeToString() }
    } catch (_: Exception) {
        null
    }

private fun createWebView(
    context: Context,
    htmlContent: String,
    chartJsCode: String?,
): WebView = WebView(context).apply {
    setBackgroundColor(Color.TRANSPARENT)
    settings.apply {
        javaScriptEnabled = true
        setSupportZoom(false)
    }
    isVerticalScrollBarEnabled = false
    isHorizontalScrollBarEnabled = false

    webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view?.let { initializeChart(it, chartJsCode) }
        }
    }
    loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
}

private fun initializeChart(webView: WebView, chartJsCode: String?) {
    val initScript = "if (typeof Chart !== 'undefined' && typeof window.initChart === 'function') " +
        "{ window.initChart(); }"

    if (chartJsCode != null) {
        webView.evaluateJavascript(chartJsCode) {
            webView.evaluateJavascript(initScript) { }
        }
    } else {
        webView.evaluateJavascript(
            """
                var script = document.createElement('script');
                script.src = 'https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js';
                script.onload = function() { $initScript };
                document.head.appendChild(script);
            """.trimIndent(),
        ) { }
    }
}

private fun String.escapeJson() =
    replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("</script>", "<\\/script>")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
