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

package io.getstream.chat.android.compose.sample.ui.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.NearMeDisabled
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.sample.vm.SharedLocationViewModel
import io.getstream.chat.android.compose.sample.vm.SharedLocationViewModelFactory
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import java.util.Calendar
import java.util.Date

@Composable
internal fun SharedLocationItem(
    modifier: Modifier,
    message: Message,
    location: Location,
    onMapClick: (url: String) -> Unit = {},
) {
    if (location.endAt == null) {
        StaticSharedLocation(
            modifier = modifier,
            location = location,
            onMapClick = onMapClick,
        )
    } else {
        val viewModel = viewModel(
            SharedLocationViewModel::class,
            factory = SharedLocationViewModelFactory(location.cid),
        )
        LiveLocationSharing(
            modifier = modifier,
            currentUser = viewModel.currentUser,
            message = message,
            location = location,
            onMapClick = onMapClick,
            onStopSharingClick = {
                viewModel.stopLiveLocationSharing(messageId = location.messageId)
            },
        )
    }
}

@Composable
private fun StaticSharedLocation(
    modifier: Modifier,
    location: Location,
    onMapClick: (url: String) -> Unit = {},
) {
    MapBox(
        modifier = modifier.aspectRatio(1f),
        latitude = location.latitude,
        longitude = location.longitude,
        onClick = onMapClick,
    ) {
        Icon(
            modifier = Modifier.align(Alignment.Center),
            imageVector = Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = Color.Red,
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun LiveLocationSharing(
    modifier: Modifier,
    currentUser: User?,
    message: Message,
    location: Location,
    onMapClick: (url: String) -> Unit = {},
    onStopSharingClick: () -> Unit = {},
) {
    val isOwnMessage = message.user.id == currentUser?.id
    val endAt = requireNotNull(location.endAt)
    val isLiveLocationEnded = !endAt.after(Date())
    Column(
        modifier = modifier
            .clip(ChatTheme.shapes.attachment)
            .background(
                color = if (isOwnMessage) {
                    ChatTheme.ownMessageTheme.backgroundColor
                } else {
                    ChatTheme.otherMessageTheme.backgroundColor
                },
            ),
    ) {
        MapBox(
            modifier = Modifier.aspectRatio(1f),
            latitude = location.latitude,
            longitude = location.longitude,
            onClick = onMapClick,
        ) {
            val animatedPadding by rememberInfiniteTransition().animateFloat(
                initialValue = 0f,
                targetValue = if (isLiveLocationEnded) 0f else 10f,
                animationSpec = infiniteRepeatable(animation = tween(AnimationDurationMillis)),
            )
            val animatedColor by rememberInfiniteTransition().animateColor(
                initialValue = ChatTheme.colors.primaryAccent,
                targetValue = if (isLiveLocationEnded) {
                    ChatTheme.colors.primaryAccent
                } else {
                    ChatTheme.colors.primaryAccent.copy(alpha = 0f)
                },
                animationSpec = infiniteRepeatable(animation = tween(AnimationDurationMillis)),
            )
            UserAvatar(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        color = animatedColor,
                        shape = ChatTheme.shapes.avatar,
                    )
                    .padding(animatedPadding.dp)
                    .size(32.dp),
                user = message.user,
                showOnlineIndicator = false,
            )
        }
        if (isLiveLocationEnded) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.NearMeDisabled,
                    contentDescription = null,
                    tint = ChatTheme.colors.textLowEmphasis,
                )
                Text(
                    text = "Live location sharing ended",
                    style = ChatTheme.typography.footnote,
                    color = ChatTheme.colors.textLowEmphasis,
                )
            }
        } else {
            Column {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!isOwnMessage) {
                        Icon(
                            imageVector = Icons.Rounded.NearMe,
                            contentDescription = null,
                            tint = ChatTheme.colors.primaryAccent,
                        )
                    }
                    Text(
                        text = "Sharing live location until ${ChatTheme.dateFormatter.formatDate(location.endAt)}",
                        style = ChatTheme.typography.footnote,
                        color = ChatTheme.colors.textLowEmphasis,
                    )
                }
                if (isOwnMessage) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.errorAccent),
                        onClick = onStopSharingClick,
                    ) {
                        Text(text = "Stop Sharing")
                    }
                }
            }
        }
    }
}

private const val AnimationDurationMillis = 2000

@Preview(showBackground = true)
@Composable
private fun StaticSharedLocationItemPreview() {
    ChatTheme {
        StaticSharedLocation(
            modifier = Modifier.fillMaxWidth(),
            location = Location(
                latitude = 37.7749,
                longitude = -122.4194,
            ),
        )
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun MyLiveLocationSharingItemPreview() {
    ChatTheme {
        val currentUser = User(id = "userId", online = true)
        LiveLocationSharing(
            modifier = Modifier.fillMaxWidth(),
            currentUser = currentUser,
            message = Message(user = currentUser),
            location = Location(
                latitude = 37.7749,
                longitude = -122.4194,
                endAt = Calendar.getInstance().apply { add(Calendar.MINUTE, 15) }.time,
            ),
        )
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun OtherLiveLocationSharingItemPreview() {
    ChatTheme {
        LiveLocationSharing(
            modifier = Modifier.fillMaxWidth(),
            currentUser = null,
            message = Message(),
            location = Location(
                latitude = 37.7749,
                longitude = -122.4194,
                endAt = Calendar.getInstance().apply { add(Calendar.MINUTE, 15) }.time,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherEndedLiveLocationSharingItemPreview() {
    ChatTheme {
        LiveLocationSharing(
            modifier = Modifier.fillMaxWidth(),
            currentUser = null,
            message = Message(),
            location = Location(
                latitude = 37.7749,
                longitude = -122.4194,
                endAt = Date(),
            ),
        )
    }
}
