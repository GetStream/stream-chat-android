package com.getstream.sdk.chat.rest.core;

import com.getstream.sdk.chat.BuildConfig;

public class ApiClientOptions {

    private String baseURL;
    private int timeout;
    private int keepAliveDuration;

    private String cdnURL;
    private int cdntimeout;

    public ApiClientOptions(String baseURL, int timeout, int cdntimeout, int keepAliveDuration) {
        this.baseURL = baseURL;
        this.timeout = timeout;
        this.cdnURL = baseURL;
        this.cdntimeout = cdntimeout;
        this.keepAliveDuration = keepAliveDuration;
    }

    public ApiClientOptions() {
        this(
                BuildConfig.DEFAULT_API_ENDPOINT,
                BuildConfig.DEFAULT_API_TIMEOUT,
                BuildConfig.DEFAULT_CDN_TIMEOUT,
                BuildConfig.DEFAULT_API_KEEP_ALIVE_TIMEOUT
        );
    }

    public String getHttpURL() {
        return "https://" + baseURL + "/";
    }

    public String getCdnHttpURL() {
        return "https://" + cdnURL + "/";
    }

    public String getWssURL() {
        return "wss://" + baseURL + "/";
    }

    public int getTimeout() {
        return timeout;
    }

    public int getCdntimeout() {
        return cdntimeout;
    }

    public int getKeepAliveDuration() {
        return keepAliveDuration;
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

        public Builder CDNTimeout(int timeout) {
            options.cdntimeout = timeout;
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