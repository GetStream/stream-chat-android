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

package io.getstream.chat.android.ui.feature.channels

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiFragmentContainerBinding

/**
 * An Activity representing a self-contained channel list screen. This Activity
 * is simply a thin wrapper around [ChannelListFragment].
 */
public open class ChannelListActivity : AppCompatActivity() {
    private lateinit var binding: StreamUiFragmentContainerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ChatClient.isInitialized.not()) {
            finish()
            return
        }
        binding = StreamUiFragmentContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, createChannelListFragment())
                .commit()
        }
    }

    /**
     * Creates an instance of [ChannelListFragment]. Override this method if you want to create an
     * instance of [ChannelListFragment] with custom arguments or if you want to create a subclass
     * of [ChannelListFragment].
     */
    protected open fun createChannelListFragment(): ChannelListFragment = ChannelListFragment.newInstance {
        setFragment(ChannelListFragment())
        customTheme(R.style.StreamUiTheme_ChannelListScreen)
        showSearch(true)
        showHeader(true)
        headerTitle(getString(R.string.stream_ui_channel_list_header_connected))
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updatePadding(left = insets.left, top = insets.top, right = insets.right, bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    public companion object {
        /**
         * Creates an Intent to start the [ChannelListActivity] or its subclass.
         *
         * @param context The context that will be used in the intent.
         * @param activityClass The Activity class that will be used in the intent.
         */
        @JvmStatic
        @JvmOverloads
        public fun createIntent(
            context: Context,
            activityClass: Class<out ChannelListActivity> = ChannelListActivity::class.java,
        ): Intent = Intent(context, activityClass)
    }
}
