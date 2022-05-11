package io.getstream.chat.android.client.plugins.requests

public interface ApiRequestsAnalyser {

    public fun registerRequest(requestName: String, data: Map<String, String>)

    public fun dumpAllRequests(): String

    public companion object {
        public fun get(): ApiRequestsAnalyser = ApiRequestsDumper()
    }
}
