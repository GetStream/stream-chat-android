/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Defines the possible options of the app bottom bar.
 */
enum class AppBottomBarOption {
    CHATS,
    THREADS,
}

/**
 * Renders the default app bottom bar for switching between chats/threads.
 *
 * @param selectedOption The currently selected [AppBottomBarOption].
 * @param onOptionSelected Action when invoked when the user clicks on an [AppBottomBarOption].
 */
@Composable
fun AppBottomBar(
    selectedOption: AppBottomBarOption,
    onOptionSelected: (AppBottomBarOption) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.barsBackground),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        AppBottomBarOptionTile(
            icon = R.drawable.ic_chats,
            text = R.string.app_bottom_bar_chats,
            isSelected = selectedOption == AppBottomBarOption.CHATS,
            onClick = { onOptionSelected(AppBottomBarOption.CHATS) },
        )
        AppBottomBarOptionTile(
            icon = R.drawable.ic_threads,
            text = R.string.app_bottom_bar_threads,
            isSelected = selectedOption == AppBottomBarOption.THREADS,
            onClick = { onOptionSelected(AppBottomBarOption.THREADS) },
        )
    }
}

@Composable
private fun AppBottomBarOptionTile(
    @DrawableRes icon: Int,
    @StringRes text: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (isSelected) ChatTheme.colors.textHighEmphasis else ChatTheme.colors.textLowEmphasis
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = contentColor,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(text),
            fontSize = 12.sp,
            color = contentColor,
        )
    }
}
