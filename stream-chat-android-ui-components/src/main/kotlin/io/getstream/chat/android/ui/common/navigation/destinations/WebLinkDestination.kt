package io.getstream.chat.android.ui.common.navigation.destinations

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.getstream.sdk.chat.navigation.destinations.ChatDestination
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.UrlSigner

@InternalStreamChatApi
public class WebLinkDestination(url: String, context: Context, urlSigner: UrlSigner) : ChatDestination(context) {
    private val url: String = urlSigner.signFileUrl(url)

    override fun navigate() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        start(browserIntent)
    }
}
