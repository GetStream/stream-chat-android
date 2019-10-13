package com.getstream.sdk.chat.rest.core;

public class ApiClientOptions {

    private static String defaultURL = "chat-us-east-1.stream-io-api.com";
    private static int defaultTimeout = 6000;
    private static int defaultCDNTimeout = 1000 * 30;

    private String baseURL;
    private String cdnURL;
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
            if (baseURL != null && baseURL.startsWith("https://")) {
                baseURL = baseURL.split("https://")[1];
            }
            if (baseURL != null && baseURL.startsWith("http://")) {
                baseURL = baseURL.split("http://")[1];
            }
            if (baseURL.endsWith("/")) {
                baseURL = baseURL.substring(0, baseURL.length() - 1);
            }
            options.baseURL = baseURL;
            return this;
        }

        public Builder CDNURL(String cdnURL) {
            if (cdnURL != null && cdnURL.startsWith("https://")) {
                cdnURL = cdnURL.split("https://")[1];
            }
            if (cdnURL != null && cdnURL.startsWith("http://")) {
                cdnURL = cdnURL.split("http://")[1];
            }
            if (cdnURL.endsWith("/")) {
                cdnURL = cdnURL.substring(0, cdnURL.length() - 1);
            }
            options.cdnURL = cdnURL;
            return this;
        }

        public ApiClientOptions build() {
            return options;
        }
    }
}