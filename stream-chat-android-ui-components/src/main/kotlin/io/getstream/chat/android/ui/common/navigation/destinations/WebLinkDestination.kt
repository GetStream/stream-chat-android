package io.getstream.chat.android.ui.common.navigation.destinations

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.getstream.sdk.chat.navigation.destinations.ChatDestination
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class WebLinkDestination(context: Context, private val url: String) : ChatDestination(context) {

    override fun navigate() {
        start(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
