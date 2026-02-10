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

package io.getstream.chat.android.compose.sample.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import io.getstream.chat.android.compose.sample.data.PredefinedUserCredentials
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class JwtTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTheme(allowUIAutomationTest = true) {
                JwtTestScreen(
                    onClick = {
                        lifecycleScope.launch {
                            val userId = PredefinedUserCredentials.availableUsers.first().user.id
                            val baseUrl = intent.getStringExtra("BASE_URL")!!
                            val tokenProvider = object : TokenProvider {
                                override fun loadToken(): String {
                                    return runBlocking { fetchJwtToken(baseUrl, userId) }
                                }
                            }

                            ChatClient.instance().connectUser(
                                user = User(id = userId),
                                tokenProvider = tokenProvider,
                            ).enqueue()
                        }
                    },
                )
            }
        }
    }

    @SuppressLint("RememberReturnType")
    @Composable
    fun JwtTestScreen(onClick: () -> Unit) {
        val connectionState by ChatClient.instance()
            .clientState
            .connectionState
            .collectAsState(initial = ConnectionState.Connecting)

        val changeCount = remember { mutableIntStateOf(0) }
        val firstConnected = remember { mutableStateOf(false) }

        LaunchedEffect(connectionState) {
            if (connectionState == ConnectionState.Connected) {
                firstConnected.value = true
            }
            if (firstConnected.value && connectionState == ConnectionState.Offline) {
                changeCount.intValue++
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Text(
                modifier = Modifier
                    .testTag("Stream_JWT_ConnectionButton")
                    .padding(horizontal = 16.dp)
                    .clickable(
                        onClick = { onClick() },
                        indication = ripple(),
                        interactionSource = remember { MutableInteractionSource() },
                    ),
                text = "JWT Test",
                fontSize = 33.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(100.dp))

            Row() {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Connection status:",
                    fontSize = 20.sp,
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    modifier = Modifier
                        .testTag("Stream_JWT_ConnectionStatus_$connectionState")
                        .padding(horizontal = 16.dp),
                    text = connectionState.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(100.dp))

            Row() {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Disconnections:",
                    fontSize = 20.sp,
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    modifier = Modifier
                        .testTag("Stream_JWT_HasBeenDisconnected_${changeCount.intValue > 0}")
                        .padding(horizontal = 16.dp),
                    text = changeCount.intValue.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    companion object {
        fun createIntent(context: Context, baseUrl: String): Intent {
            return Intent(context, JwtTestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("BASE_URL", baseUrl)
            }
        }
    }

    private suspend fun fetchJwtToken(baseUrl: String, userId: String): String {
        return withContext(Dispatchers.IO) {
            val endpoint = "$baseUrl/jwt/get?platform=android"
            val request = Request.Builder().url(endpoint).build()

            OkHttpClient().newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ""
                }
                response.body?.string() ?: ""
            }
        }
    }
}
