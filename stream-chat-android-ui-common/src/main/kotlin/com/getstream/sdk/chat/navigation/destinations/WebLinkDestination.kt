package com.getstream.sdk.chat.navigation.destinations

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.getstream.sdk.chat.UrlSigner
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class WebLinkDestination(url: String, context: Context, urlSigner: UrlSigner = UrlSigner.DefaultUrlSigner()) :
    ChatDestination(context) {

    private val url: String = urlSigner.signFileUrl(url) ?: ""

    override fun navigate() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        start(browserIntent)
    }
}
