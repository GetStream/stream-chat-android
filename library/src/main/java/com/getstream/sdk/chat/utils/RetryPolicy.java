package com.getstream.sdk.chat.utils;

import com.getstream.sdk.chat.rest.core.Client;

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
    boolean shouldRetry(Client client, Integer attempt, String errMsg, int errCode);

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
    Integer retryTimeout(Client client, Integer attempt, String errMsg, int errCode);
}
