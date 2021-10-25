package io.getstream.chat.android.ui.common.navigation.destinations

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.getstream.sdk.chat.navigation.destinations.ChatDestination
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class WebLinkDestination(context: Context, private val url: String) : ChatDestination(context) {

    override fun navigate() {
        try {
            start(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "There is no app to view this url\n$url", Toast.LENGTH_LONG).show()
        }
    }
}
