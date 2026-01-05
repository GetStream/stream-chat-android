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

package io.getstream.chat.android.client.plugins.requests

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val NOT_FOUND = "not found"
private const val NOT_FOUND_INT = -1

/**
 * Class for analyse requests of the SDK. It can be use to understand how the SDK is calling the backend API.
 */
internal class ApiRequestsDumper(
    private val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()),
) : ApiRequestsAnalyser {

    private val requestsDataMap: MutableMap<String, List<RequestData>> = mutableMapOf()

    /**
     * Registers the request using the name as an ID.
     *
     * @param requestName Name of the request.
     * @param data All the data that should be included in the analyser about the request.
     */
    override fun registerRequest(requestName: String, data: Map<String, String>) {
        synchronized(this) {
            val requestData = RequestData(requestName, Date(), data)
            requestsDataMap[requestName] = (requestsDataMap[requestName] ?: emptyList()) + requestData
        }
    }

    /**
     * Gets the information about the request by name
     *
     * @param requestName The name of the request.
     */
    override fun dumpRequestByName(requestName: String): String {
        return requestsDataMap[requestName]
            ?.toHumanReadableStringBuilder()
            ?.toString()
            ?: NOT_FOUND
    }

    /**
     * Gets all the information in the analyser.
     */
    override fun dumpAll(): String {
        return buildString {
            requestsDataMap.values.forEach { requestDataList ->
                append(requestDataList.toHumanReadableStringBuilder())
                appendLine()
            }
        }
    }

    /**
     * Clears all the information of the analyser. Should be used to avoid batches of data that are too big
     * to understand.
     */
    override fun clearAll() {
        requestsDataMap.clear()
    }

    /**
     * Clear an specific requests containing some string in its name. Return -1 it the request is not found.
     */
    override fun clearRequestContaining(queryText: String) {
        synchronized(this) {
            val keys = requestsDataMap.keys.filter { key ->
                key.contains(queryText)
            }
            keys.forEach(requestsDataMap::remove)
        }
    }

    /**
     * Returns the number of times that a requests was made using name.
     */
    override fun countRequestContaining(requestName: String): Int {
        val matchKey = requestsDataMap.keys.find { key ->
            key.contains(requestName)
        }

        return requestsDataMap[matchKey]?.count() ?: NOT_FOUND_INT
    }

    /**
     * Counts all the requests made.
     */
    override fun countAllRequests(): Int {
        return requestsDataMap.values.fold(0) { acc, list -> acc + list.count() }
    }

    /**
     * A list of the same request that is converted to a human readable String
     */
    private fun List<RequestData>.toHumanReadableStringBuilder(): StringBuilder {
        val dataList = this
        val requestName = first().name
        val count = count()

        val extraDataBuilder = StringBuilder().apply {
            dataList.forEachIndexed { i, requestData ->
                val time = dateFormat.format(requestData.time)
                val params = requestData.extraData.entries.joinToString { (key, value) -> "$key - $value" }
                appendLine("Call $i. Time: $time. Params: $params")
            }
        }

        return StringBuilder().apply {
            appendLine("Request: $requestName. Count: $count")
            append(extraDataBuilder)
        }
    }
}
