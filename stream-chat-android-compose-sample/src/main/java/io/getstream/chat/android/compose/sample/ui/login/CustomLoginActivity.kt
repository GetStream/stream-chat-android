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

package io.getstream.chat.android.compose.sample.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.client.BuildConfig
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.data.UserCredentials
import io.getstream.chat.android.compose.sample.feature.channel.list.ChannelsActivity
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User
import io.getstream.result.Error
import kotlinx.coroutines.launch

/**
 * An Activity that allows users to manually log in to an environment with an API key,
 * user ID, user token and user name.
 */
class CustomLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTheme(allowUIAutomationTest = true) {
                CustomLoginScreen(
                    onBackButtonClick = ::finish,
                    onLoginButtonClick = { userCredentials ->
                        ChatHelper.initializeSdk(applicationContext, userCredentials.apiKey)

                        lifecycleScope.launch {
                            ChatHelper.connectUser(
                                userCredentials = userCredentials,
                                onSuccess = ::openChannels,
                                onError = ::showError,
                            )
                        }
                    },
                )
            }
        }
    }

    @Composable
    fun CustomLoginScreen(
        onBackButtonClick: () -> Unit,
        onLoginButtonClick: (UserCredentials) -> Unit,
    ) {
        Scaffold(
            topBar = { CustomLoginToolbar(onClick = onBackButtonClick) },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    var apiKeyText by remember { mutableStateOf("") }
                    var userIdText by remember { mutableStateOf("") }
                    var userTokenText by remember { mutableStateOf("") }
                    var userNameText by remember { mutableStateOf("") }

                    val isLoginButtonEnabled = apiKeyText.isNotEmpty() &&
                        userIdText.isNotEmpty() &&
                        userTokenText.isNotEmpty()

                    CustomLoginInputField(
                        hint = stringResource(id = R.string.custom_login_hint_api_key),
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                    )

                    CustomLoginInputField(
                        hint = stringResource(id = R.string.custom_login_hint_user_id),
                        value = userIdText,
                        onValueChange = { userIdText = it },
                    )

                    CustomLoginInputField(
                        hint = stringResource(id = R.string.custom_login_hint_user_token),
                        value = userTokenText,
                        onValueChange = { userTokenText = it },
                    )

                    CustomLoginInputField(
                        hint = stringResource(id = R.string.custom_login_hint_user_name),
                        value = userNameText,
                        onValueChange = { userNameText = it },
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    CustomLoginButton(
                        enabled = isLoginButtonEnabled,
                        onClick = {
                            onLoginButtonClick(
                                UserCredentials(
                                    apiKey = apiKeyText,
                                    user = User(
                                        id = userIdText,
                                        name = userNameText,
                                    ),
                                    token = userTokenText,
                                ),
                            )
                        },
                    )

                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = stringResource(R.string.sdk_version_template, BuildConfig.STREAM_CHAT_VERSION),
                        fontSize = 14.sp,
                        color = ChatTheme.colors.textLowEmphasis,
                    )
                }
            },
        )
    }

    @Composable
    private fun CustomLoginToolbar(onClick: () -> Unit) {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.user_login_advanced_options))
            },
            navigationIcon = {
                IconButton(
                    onClick = onClick,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            },
            backgroundColor = Color.White,
            elevation = 0.dp,
        )
    }

    @Composable
    private fun CustomLoginInputField(
        hint: String,
        value: String,
        onValueChange: (String) -> Unit,
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(56.dp),
            value = value,
            onValueChange = { onValueChange(it) },
            singleLine = true,
            label = { Text(hint) },
            colors = TextFieldDefaults.textFieldColors(
                textColor = ChatTheme.colors.textHighEmphasis,
                backgroundColor = ChatTheme.colors.inputBackground,
                cursorColor = ChatTheme.colors.primaryAccent,
                focusedIndicatorColor = ChatTheme.colors.primaryAccent,
                focusedLabelColor = ChatTheme.colors.primaryAccent,
                unfocusedLabelColor = ChatTheme.colors.textLowEmphasis,
            ),
        )
    }

    @Composable
    private fun CustomLoginButton(
        enabled: Boolean,
        onClick: () -> Unit = {},
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ChatTheme.colors.primaryAccent,
                disabledBackgroundColor = ChatTheme.colors.disabled,
            ),
            onClick = onClick,
        ) {
            Text(
                text = stringResource(id = R.string.custom_login_button_text),
                fontSize = 16.sp,
                color = Color.White,
            )
        }
    }

    private fun openChannels() {
        startActivity(ChannelsActivity.createIntent(this))
        finish()
    }

    private fun showError(error: Error) {
        Toast.makeText(this, "Login failed ${error.message}", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, CustomLoginActivity::class.java)
        }
    }
}
