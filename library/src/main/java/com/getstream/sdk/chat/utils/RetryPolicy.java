package com.getstream.sdk.chat.utils;

import io.getstream.chat.android.client.ChatClient;

public interface RetryPolicy {
    /**
     * Should Retry evaluates if we should retry the failure
     *
     * @param client
     * @param attempt
     * @param errMsg
     * @param errCode
     * @return
     */
    boolean shouldRetry(ChatClient client, Integer attempt, String errMsg, int errCode);

    /**
     * In the case that we want to retry a failed request the retryTimeout method is called
     * to determine the timeout
     *
     * @param client
     * @param attempt
     * @param errMsg
     * @param errCode
     * @return
     */
    Integer retryTimeout(ChatClient client, Integer attempt, String errMsg, int errCode);
}
