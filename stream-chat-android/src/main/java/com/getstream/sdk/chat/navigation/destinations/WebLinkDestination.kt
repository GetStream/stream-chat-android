package com.getstream.sdk.chat.navigation.destinations

import android.content.Context
import android.content.Intent
import android.net.Uri

internal class WebLinkDestination(url: String, context: Context) : ChatDestination(context) {
    private val url: String = com.getstream.sdk.chat.ChatUX.instance().urlSigner.signFileUrl(url) ?: ""

    override fun navigate() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        start(browserIntent)
    }
}
