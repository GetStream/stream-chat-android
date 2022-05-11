package io.getstream.chat.android.client.plugins.requests

public interface ApiRequestsAnalyser {

    public fun registerRequest(requestName: String, data: Map<String, String>)

    public fun dumpAllRequests(): String

    public companion object {
        private var instance: ApiRequestsAnalyser? = null

        public fun get(): ApiRequestsAnalyser = instance ?: ApiRequestsDumper().also { dumper ->
            instance = dumper
        }
    }
}
