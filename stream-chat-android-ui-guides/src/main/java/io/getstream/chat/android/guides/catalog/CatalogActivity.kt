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

package io.getstream.chat.android.guides.catalog

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.guides.R
import io.getstream.chat.android.guides.login.LoginActivity
import io.getstream.chat.android.guides.compose.customattachments.ChannelsActivity as ComposeCustomAttachmentsActivity
import io.getstream.chat.android.guides.uicomponents.customattachments.messagecomposer.ChannelsActivity as CustomAttachmentsWithMessageComposerActivity
import io.getstream.chat.android.guides.uicomponents.customattachments.messageinput.ChannelsActivity as CustomAttachmentsWithMessageInputActivity

class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTheme {
                CatalogScreen()
            }
        }
    }

    @Composable
    private fun CatalogScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            IconButton(
                modifier = Modifier
                    .size(56.dp)
                    .padding(4.dp)
                    .align(Alignment.End),
                onClick = ::logout
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = null,
                    tint = ChatTheme.colors.textHighEmphasis,
                )
            }

            GuideSection("Compose guides")

            GuideItem(
                titleText = "Custom attachments",
                subtitleText = "Description"
            ) {
                startActivity(ComposeCustomAttachmentsActivity.createIntent(this@CatalogActivity))
            }

            GuideSection("UI Components guides")

            GuideItem(
                titleText = "Custom attachments (MessageInputView)",
                subtitleText = "Description"
            ) {
                startActivity(CustomAttachmentsWithMessageInputActivity.createIntent(this@CatalogActivity))
            }

            GuideItem(
                titleText = "Custom attachments (MessageComposerView)",
                subtitleText = "Description"
            ) {
                startActivity(CustomAttachmentsWithMessageComposerActivity.createIntent(this@CatalogActivity))
            }
        }
    }

    @Composable
    private fun GuideSection(text: String) {
        Text(
            text = text,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            style = ChatTheme.typography.title3Bold,
            color = ChatTheme.colors.textHighEmphasis,
        )
    }

    @Composable
    private fun GuideItem(
        titleText: String,
        subtitleText: String,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .clickable(
                    onClick = onClick,
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = titleText,
                    style = ChatTheme.typography.bodyBold,
                    fontSize = 16.sp,
                    color = ChatTheme.colors.textHighEmphasis,
                )

                Text(
                    text = subtitleText,
                    style = ChatTheme.typography.body,
                    color = ChatTheme.colors.textHighEmphasis,
                )
            }
        }
    }

    /**
     * Logs out and navigated to the login screen.
     */
    private fun logout() {
        ChatClient.instance()
            .disconnect(true)
            .enqueue()

        finish()
        startActivity(LoginActivity.createIntent(this))
        overridePendingTransition(0, 0)
    }
}
