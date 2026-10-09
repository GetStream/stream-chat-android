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

package io.getstream.chat.android.ai.compose.sample.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.sample.R
import io.getstream.chat.android.ai.compose.sample.ui.theme.AppTheme

@Suppress("LongMethod")
@Composable
public fun ChatTopBar(
    title: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNewChatClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ChatFloatingButton(
            onClick = onMenuClick,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = "Menu",
            )
        }

        ChatFloatingActions {
            if (onNewChatClick != null) {
                IconButton(onClick = onNewChatClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_new),
                        contentDescription = "New chat",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            if (onDeleteClick != null) {
                var expanded by remember { mutableStateOf(false) }

                IconButton(onClick = { expanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more),
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    Text(
                        modifier = Modifier
                            .sizeIn(maxWidth = 200.dp)
                            .padding(8.dp),
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded = false
                            onDeleteClick()
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = null,
                            )
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error,
                            leadingIconColor = MaterialTheme.colorScheme.error,
                        ),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatTopBarPreview() {
    AppTheme {
        ChatTopBar(
            title = "Chat Title",
            onMenuClick = {},
            onNewChatClick = {},
            onDeleteClick = {},
        )
    }
}
