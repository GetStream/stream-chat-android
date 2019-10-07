package com.getstream.sdk.chat.utils;

public interface RetryPolicy {
    boolean shouldRetry(Integer attempt, String errMsg, int errCode);
    Integer retryTimeout(Integer attempt, String errMsg, int errCode);
}
