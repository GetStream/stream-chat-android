/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.navigation.destinations

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.utils.extensions.addSchemeToUrlIfNeeded

@InternalStreamChatApi
public class WebLinkDestination(context: Context, private val url: String) : ChatDestination(context) {

    override fun navigate() {
        try {
            val urlWithSchema = url.addSchemeToUrlIfNeeded()
            start(Intent(Intent.ACTION_VIEW, Uri.parse(urlWithSchema)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.stream_ui_message_list_error_cannot_open_link, url),
                Toast.LENGTH_LONG,
            ).show()
        }
    }
}
