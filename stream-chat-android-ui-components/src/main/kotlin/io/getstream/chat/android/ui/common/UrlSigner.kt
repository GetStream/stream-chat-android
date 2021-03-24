package io.getstream.chat.android.ui.common

public interface UrlSigner {
    public fun signFileUrl(url: String): String
    public fun signImageUrl(url: String): String

    public class DefaultUrlSigner : UrlSigner {
        override fun signFileUrl(url: String): String {
            return url
        }

        override fun signImageUrl(url: String): String {
            return url
        }
    }
}
