package io.getstream.chat.android.client.plugins.requests

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val NOT_FOUND = "not found"

internal class ApiRequestsDumper(
    private val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
) : ApiRequestsAnalyser {

    private val requestsDataMap: MutableMap<String, MutableList<RequestData>> = mutableMapOf()

    override fun registerRequest(requestName: String, data: Map<String, String>) {
        val requestData = RequestData(requestName, Date(), data)
        val requestDataList = requestsDataMap[requestName]

        if (requestDataList != null) {
            requestDataList.add(requestData)
        } else {
            requestsDataMap[requestName] = mutableListOf(requestData)
        }
    }

    internal fun countRequestByName(requestName: String): Int {
        return requestsDataMap[requestName]?.count() ?: 0
    }

    internal fun countAllRequests(): Int {
        return requestsDataMap.values.fold(0) { acc, list -> acc + list.count() }
    }

    internal fun dumpRequestByName(requestName: String): String {
        return requestsDataMap[requestName]
            ?.toHumanReadableStringBuilder()
            ?.toString()
            ?: NOT_FOUND
    }

    override fun dumpAllRequests(): String {
        return buildString {
            requestsDataMap.values.forEach { requestDataList ->
                append(requestDataList.toHumanReadableStringBuilder())
                appendLine()
            }
        }
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
