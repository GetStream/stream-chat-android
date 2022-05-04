package io.getstream.chat.android.client.plugins.requests

public interface ApiRequestsAnalyser {

    public fun registerRequest(requestName: String, data: Map<String, String>)
}
