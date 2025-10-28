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

package io.getstream.chat.android.guides.login

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.guides.R
import io.getstream.chat.android.guides.catalog.CatalogActivity

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

    /**
     * A Composable that represents the entire login screen.
     */
    @Composable
    private fun LoginScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Icon(
                modifier = Modifier.size(width = 80.dp, height = 40.dp),
                painter = painterResource(id = R.drawable.ic_stream),
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.login_screen_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ChatTheme.colors.textHighEmphasis,
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                items(items = LoginUsers.createUsers()) { loginUser ->
                    UserItem(loginUser = loginUser)

                    DividerItem()
                }
            }
        }
    }

    /**
     * Represents a user whose credentials will be used for login.
     *
     * @param loginUser The user credentials to login with.
     */
    @Composable
    fun UserItem(loginUser: LoginUser) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable(
                    onClick = { login(loginUser) },
                    indication = ripple(),
                    interactionSource = remember { MutableInteractionSource() },
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                modifier = Modifier.size(40.dp),
                user = loginUser.user,
            )

            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = loginUser.user.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatTheme.colors.textHighEmphasis,
                )

                Text(
                    text = stringResource(id = R.string.login_user_subtitle),
                    fontSize = 12.sp,
                    color = ChatTheme.colors.textLowEmphasis,
                )
            }

            Icon(
                modifier = Modifier.wrapContentSize(),
                painter = painterResource(id = R.drawable.ic_arrow),
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent,
            )
        }
    }

    /**
     * Represents a separator between user items.
     */
    @Composable
    private fun DividerItem() {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(color = ChatTheme.colors.borders),
        )
    }

    /**
     * Initializes the SDK with the given user and navigates to the catalog screen.
     *
     * @param loginUser The user credentials to login with.
     */
    private fun login(loginUser: LoginUser) {
        ChatClient.instance().connectUser(
            user = loginUser.user,
            token = loginUser.token,
        ).enqueue()

        startActivity(Intent(this, CatalogActivity::class.java))
        finish()
    }

    companion object {
        /**
         * Creates an [Intent] to start [LoginActivity].
         *
         * @param context The context used to create the intent.
         * @return The [Intent] to start [LoginActivity].
         */
        fun createIntent(context: Context): Intent = Intent(context, LoginActivity::class.java)
    }
}
