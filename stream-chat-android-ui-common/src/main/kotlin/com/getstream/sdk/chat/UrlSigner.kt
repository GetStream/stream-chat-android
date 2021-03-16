package com.getstream.sdk.chat

import java.io.Serializable

public interface UrlSigner : Serializable {
    public fun signFileUrl(url: String): String
    public fun signImageUrl(url: String): String

    public class DefaultUrlSigner : UrlSigner {
        override fun signFileUrl(url: String): String = url
        override fun signImageUrl(url: String): String = url
    }
}
