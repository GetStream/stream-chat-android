package com.getstream.sdk.chat;

public interface UrlSigner {
    String signFileUrl(String url);

    String signImageUrl(String url);

    class DefaultUrlSigner implements UrlSigner {

        @Override
        public String signFileUrl(String url) {
            return url;
        }

        @Override
        public String signImageUrl(String url) {
            return url;
        }
    }
}
