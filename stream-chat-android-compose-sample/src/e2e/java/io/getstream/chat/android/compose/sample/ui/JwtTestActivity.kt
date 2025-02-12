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

package io.getstream.chat.android.compose.sample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.compose.sample.ChatHelper
import io.getstream.chat.android.compose.sample.data.PredefinedUserCredentials.API_KEY
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User
import kotlinx.coroutines.launch

class JwtTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTheme(allowUIAutomationTest = true) {
                JwtTestScreen(
                    onClick = { tokenProvider ->
                        lifecycleScope.launch {
                            if (ChatClient.instance().config.apiKey != API_KEY) {
                                ChatHelper.initializeSdk(
                                    applicationContext,
                                    API_KEY,
                                    intent.getStringExtra("BASE_URL")
                                )
                            }

                            ChatClient.instance().connectUser(
                                user = User(id = "luke_skywalker"),
                                tokenProvider = tokenProvider,
                            ).enqueue()
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun JwtTestScreen(
        onClick: (TokenProvider) -> Unit
    ) {
        val connectionState by ChatClient.instance()
            .clientState
            .connectionState
            .collectAsState(initial = ConnectionState.Connecting)

        val initializationState by ChatClient.instance()
            .clientState
            .initializationState
            .collectAsState(initial = ConnectionState.Connecting)

        val tokenProvider: TokenProvider = object : TokenProvider {
            override fun loadToken(): String {
                return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibHVrZV9za3l3YWxrZXIifQ.kFSLHRB5X62t0Zlc7nwczWUfsQMwfkpylC6jCUZ6Mc0"
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier
                    .testTag("Stream_JWT_InitButton")
                    .padding(horizontal = 16.dp)
                    .clickable(
                        onClick = { onClick(tokenProvider) },
                        indication = ripple(),
                        interactionSource = remember { MutableInteractionSource() },
                    ),
                text = "JWT Test",
                fontSize = 33.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                modifier = Modifier
                    .testTag("Stream_JWT_ConnectionStatus")
                    .padding(horizontal = 16.dp),
                text = connectionState.toString(),
                fontSize = 33.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                modifier = Modifier
                    .testTag("Stream_JWT_InitializationStatus")
                    .padding(horizontal = 16.dp),
                text = initializationState.toString(),
                fontSize = 33.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, JwtTestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }
}
