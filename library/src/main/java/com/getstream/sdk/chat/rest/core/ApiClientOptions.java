package com.getstream.sdk.chat.rest.core;

public class ApiClientOptions {


    private static String defaultURL = "chat-us-east-1.stream-io-api.com";
    private static int defaultTimeout = 6000;

    private String baseURL;
    private int timeout;

    public ApiClientOptions(String baseURL, int timeout) {
        this.baseURL = baseURL;
        this.timeout = timeout;
    }

    public ApiClientOptions() {
        this(defaultURL, defaultTimeout);
    }

    public String getHttpURL() {
        return "https://" + baseURL + "/";
    }

    public String getWssURL() {
        return "wss://" + baseURL + "/";
    }

    public int getTimeout() {
        return timeout;
    }

    public static class Builder {
        private ApiClientOptions options;

        public Builder() {
            this.options = new ApiClientOptions();
        }

        public Builder Timeout(int timeout) {
            options.timeout = timeout;
            return this;
        }

        public Builder BaseURL(String baseURL) {
            options.baseURL = baseURL;
            return this;
        }

        public ApiClientOptions build() {
            return options;
        }
    }
}
