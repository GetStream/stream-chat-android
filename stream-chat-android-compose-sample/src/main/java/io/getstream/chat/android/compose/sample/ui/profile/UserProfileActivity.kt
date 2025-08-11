/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.profile

import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.components.BackButton
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.models.User

class UserProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                UserProfileScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun UserProfileScreen() {
        val viewModel = viewModel<UserProfileViewModel>()
        val user by viewModel.user.collectAsStateWithLifecycle()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        BackButton(
                            modifier = Modifier.mirrorRtl(layoutDirection = LocalLayoutDirection.current),
                            painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
                            onBackPressed = ::finish,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ChatTheme.colors.appBackground,
                    ),
                )
            },
            containerColor = ChatTheme.colors.appBackground,
        ) { paddingValues ->
            UserProfileScreenContent(user, paddingValues)
        }
    }

    @Suppress("LongMethod")
    @Composable
    private fun UserProfileScreenContent(
        user: User?,
        paddingValues: PaddingValues,
    ) {
        when (user) {
            null -> {
                LoadingIndicator(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxWidth(),
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    UserAvatar(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                            .size(72.dp),
                        user = user,
                        showOnlineIndicator = false,
                        onClick = null,
                    )
                    Text(
                        text = "Name",
                        style = ChatTheme.typography.title3,
                        color = ChatTheme.colors.textHighEmphasis,
                    )
                    Text(
                        text = user.name.takeIf(String::isNotBlank) ?: user.id,
                        style = ChatTheme.typography.body,
                        color = ChatTheme.colors.textHighEmphasis,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Divider()
                    val avgResponseTimeInSeconds = user.avgResponseTime ?: 0
                    if (avgResponseTimeInSeconds > 0) {
                        Text(
                            text = "Average Response Time",
                            style = ChatTheme.typography.title3,
                            color = ChatTheme.colors.textHighEmphasis,
                        )
                        Text(
                            text = formatTime(
                                resources = LocalContext.current.resources,
                                seconds = avgResponseTimeInSeconds,
                            ),
                            style = ChatTheme.typography.body,
                            color = ChatTheme.colors.textHighEmphasis,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
}

@Suppress("MagicNumber")
private fun formatTime(
    resources: Resources,
    seconds: Long,
): String {
    val minutes = (seconds / 60).toInt()
    val remainingSeconds = (seconds % 60).toInt()
    return buildString {
        if (minutes > 0) {
            append(resources.getQuantityString(R.plurals.time_minutes, minutes, minutes))
        }
        if (remainingSeconds > 0) {
            if (isNotEmpty()) append(" ")
            append(resources.getQuantityString(R.plurals.time_seconds, remainingSeconds, remainingSeconds))
        }
    }
}
