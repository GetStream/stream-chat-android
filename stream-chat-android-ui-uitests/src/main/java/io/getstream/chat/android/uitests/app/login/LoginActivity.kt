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

package io.getstream.chat.android.uitests.app.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.uitests.app.compose.ComposeChannelsActivity
import io.getstream.chat.android.uitests.app.uicomponents.UiComponentsChannelsActivity

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
     * Represents login screen.
     */
    @Composable
    private fun LoginScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var selectedSdkType by remember { mutableStateOf(SdkType.UI_COMPONENTS) }

            Spacer(modifier = Modifier.height(32.dp))

            SdkTypeList(
                modifier = Modifier.fillMaxWidth(),
                sdkType = selectedSdkType,
                onSdkTypeClick = { selectedSdkType = it },
            )

            Spacer(modifier = Modifier.height(32.dp))

            UserList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onUserCredentialsClick = { userCredentials ->
                    login(
                        userCredentials = userCredentials,
                        sdkType = selectedSdkType,
                    )
                },
            )
        }
    }

    /**
     * Represents a toggle group that allows the user to select the type of
     * Stream Android SDK that will be used after login.
     *
     * @param sdkType The type of Android SDK that will be used after login.
     * @param onSdkTypeClick Callback that is called when the user clicks on SDK.
     * @param modifier Modifier for styling.
     */
    @Composable
    private fun SdkTypeList(
        sdkType: SdkType,
        onSdkTypeClick: (SdkType) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SdkType.entries.forEach { type ->
                Row(
                    modifier = Modifier.padding(all = 8.dp),
                ) {
                    val isSdkSelected = type == sdkType

                    OutlinedButton(
                        modifier = Modifier.semantics { contentDescription = type.value },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isSdkSelected) Color.White else Color.Blue,
                            containerColor = if (isSdkSelected) Color.Blue else Color.White,
                        ),
                        onClick = {
                            onSdkTypeClick(type)
                        },
                    ) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = type.value,
                        )
                    }
                }
            }
        }
    }

    /**
     * Represents a predefined list of users to choose from.
     *
     * @param onUserCredentialsClick Callback that is called when a user is clicked.
     * @param modifier Modifier for styling.
     */
    @Composable
    fun UserList(
        onUserCredentialsClick: (UserCredentials) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        LazyColumn(modifier = modifier) {
            items(items = userCredentialsList) { userCredentials ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(
                            onClick = { onUserCredentialsClick(userCredentials) },
                            indication = ripple(),
                            interactionSource = remember { MutableInteractionSource() },
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UserAvatar(
                        modifier = Modifier.size(40.dp),
                        user = userCredentials.user,
                    )

                    Text(
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        text = userCredentials.user.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChatTheme.colors.textPrimary,
                    )
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(color = ChatTheme.colors.borderCoreDefault),
                )
            }
        }
    }

    /**
     * Initializes the SDK with the given user and navigates to the channel list screen.
     *
     * @param userCredentials The user credentials to login with.
     * @param sdkType The type of Android SDK that will be used after login.
     */
    private fun login(userCredentials: UserCredentials, sdkType: SdkType) {
        ChatClient.instance().connectUser(
            user = userCredentials.user,
            token = userCredentials.token,
        ).enqueue()

        val intent = when (sdkType) {
            SdkType.UI_COMPONENTS -> UiComponentsChannelsActivity.createIntent(this)
            SdkType.COMPOSE -> ComposeChannelsActivity.createIntent(this)
        }
        startActivity(intent)

        finish()
    }

    /**
     * The type of Stream Android SDK that will be used after login.
     *
     * @param value The name of the type.
     */
    private enum class SdkType(val value: String) {
        UI_COMPONENTS("UI Components"),
        COMPOSE("Compose"),
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
