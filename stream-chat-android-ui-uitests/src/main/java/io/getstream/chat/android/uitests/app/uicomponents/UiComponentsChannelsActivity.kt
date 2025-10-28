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

package io.getstream.chat.android.uitests.app.uicomponents

import android.content.Context
import android.content.Intent
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.ui.feature.channels.ChannelListActivity
import io.getstream.chat.android.ui.feature.channels.ChannelListFragment
import io.getstream.chat.android.uitests.app.login.LoginActivity

class UiComponentsChannelsActivity :
    ChannelListActivity(),
    ChannelListFragment.HeaderUserAvatarClickListener {

    /**
     * Logs out and navigated to the login screen.
     */
    override fun onUserAvatarClick() {
        ChatClient.instance().disconnect(flushPersistence = false).enqueue {
            finish()
            startActivity(LoginActivity.createIntent(this))
            overridePendingTransition(0, 0)
        }
    }

    companion object {
        /**
         * Create an [Intent] to start [UiComponentsChannelsActivity].
         *
         * @param context The context used to create the intent.
         * @return The [Intent] to start [UiComponentsChannelsActivity].
         */
        fun createIntent(context: Context): Intent = Intent(context, UiComponentsChannelsActivity::class.java)
    }
}
