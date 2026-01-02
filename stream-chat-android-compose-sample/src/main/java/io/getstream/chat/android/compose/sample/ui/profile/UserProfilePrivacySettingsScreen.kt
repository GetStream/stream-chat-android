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

package io.getstream.chat.android.compose.sample.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@Suppress("LongMethod")
@Composable
fun UserProfilePrivacySettingsScreen(
    privacySettings: PrivacySettings?,
    onSaveClick: (settings: PrivacySettings) -> Unit,
) {
    var typingIndicators by remember(privacySettings) {
        mutableStateOf(privacySettings?.typingIndicators ?: TypingIndicators())
    }
    var deliveryReceipts by remember(privacySettings) {
        mutableStateOf(privacySettings?.deliveryReceipts ?: DeliveryReceipts())
    }
    var readReceipts by remember(privacySettings) {
        mutableStateOf(privacySettings?.readReceipts ?: ReadReceipts())
    }

    Column {
        SwitchItem(
            label = "Typing Indicators",
            checked = typingIndicators.enabled,
            onCheckedChange = { checked ->
                typingIndicators = typingIndicators.copy(enabled = checked)
            },
        )
        SwitchItem(
            label = "Delivery Receipts",
            checked = deliveryReceipts.enabled,
            onCheckedChange = { checked ->
                deliveryReceipts = deliveryReceipts.copy(enabled = checked)
            },
        )
        SwitchItem(
            label = "Read Receipts",
            checked = readReceipts.enabled,
            onCheckedChange = { checked ->
                readReceipts = readReceipts.copy(enabled = checked)
            },
        )
        Button(
            onClick = {
                onSaveClick(
                    PrivacySettings(
                        typingIndicators = typingIndicators,
                        deliveryReceipts = deliveryReceipts,
                        readReceipts = readReceipts,
                    ),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChatTheme.colors.primaryAccent,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_checkmark),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Save Settings",
                    style = ChatTheme.typography.bodyBold.copy(
                        color = Color.White,
                        fontSize = 16.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun SwitchItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (checked: Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = { onCheckedChange(!checked) },
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = ChatTheme.typography.title3,
            color = ChatTheme.colors.textHighEmphasis,
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
        )
    }
}
