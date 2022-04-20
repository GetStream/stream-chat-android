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

package io.getstream.chat.android.uitests.app.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.channel.ChannelListActivity
import io.getstream.chat.android.uitests.R
import io.getstream.chat.android.uitests.app.compose.ChannelsActivity

/**
 * An Activity that allows users to log in using one of our predefined sample users.
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTheme {
                LoginScreen()
            }
        }
    }

    @Composable
    fun LoginScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Icon(
                modifier = Modifier.size(width = 80.dp, height = 40.dp),
                painter = painterResource(id = R.drawable.ic_stream),
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent,
            )

            Spacer(modifier = Modifier.height(20.dp))

            val checkedState = remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    modifier = Modifier.semantics { contentDescription = "Compose" },
                    checked = checkedState.value,
                    onCheckedChange = { checkedState.value = it }
                )

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.sdk_name_compose),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatTheme.colors.textHighEmphasis
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                items(items = LOGIN_USERS) { loginUser ->
                    UserItem(
                        loginUser = loginUser,
                        onItemClick = {
                            login(
                                loginUser = loginUser,
                                isCompose = checkedState.value
                            )
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(color = ChatTheme.colors.borders)
                    )
                }
            }
        }
    }

    /**
     * Represents a user whose credentials will be used for login.
     */
    @Composable
    fun UserItem(
        loginUser: LoginUser,
        onItemClick: (LoginUser) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable(
                    onClick = { onItemClick(loginUser) },
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                modifier = Modifier.size(40.dp),
                user = loginUser.user,
            )

            Text(
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                text = loginUser.user.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ChatTheme.colors.textHighEmphasis
            )
        }
    }

    /**
     *  Initializes the SDK with the given user and navigates to the channel list screen.
     *
     * @param loginUser The user credentials to login with.
     * @param isCompose If Compose UI components will be used.
     */
    private fun login(loginUser: LoginUser, isCompose: Boolean) {
        ChatClient.instance().connectUser(
            user = loginUser.user,
            token = loginUser.token
        ).enqueue()

        if (isCompose) {
            startActivity(ChannelsActivity.createIntent(this))
        } else {
            startActivity(ChannelListActivity.createIntent(this))
        }
        finish()
    }

    companion object {
        /**
         * Create an [Intent] to start [LoginActivity].
         *
         * @param context The context used to create the intent.
         * @return The [Intent] to start [LoginActivity].
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}
