/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.ui.sample.feature

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import io.getstream.chat.ui.sample.R

const val EXTRA_CHANNEL_ID = "extra_channel_id"
const val EXTRA_CHANNEL_TYPE = "extra_channel_type"
const val EXTRA_MESSAGE_ID = "extra_message_id"
const val EXTRA_PARENT_MESSAGE_ID = "extra_parent_message_id"

class HostActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()
        if (savedInstanceState != null && lastNonConfigurationInstance == null) {
            // the application process was killed by the OS
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
            finishAffinity()
        }
    }

    private fun setupEdgeToEdge() {
        val root = findViewById<View>(android.R.id.content).rootView
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            root.updatePadding(left = insets.left, top = insets.top, right = insets.right, bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    companion object {

        fun createLaunchIntent(
            context: Context,
            messageId: String,
            parentMessageId: String?,
            channelType: String,
            channelId: String,
        ) = Intent(context, HostActivity::class.java).apply {
            putExtra(EXTRA_CHANNEL_ID, channelId)
            putExtra(EXTRA_CHANNEL_TYPE, channelType)
            putExtra(EXTRA_MESSAGE_ID, messageId)
            putExtra(EXTRA_PARENT_MESSAGE_ID, parentMessageId)
        }
    }
}
