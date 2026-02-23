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

package io.getstream.chat.android.guides.catalog

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.guides.R
import io.getstream.chat.android.guides.login.LoginActivity
import io.getstream.chat.android.guides.catalog.compose.customattachments.ChannelsActivity as ComposeCustomAttachmentsActivity
import io.getstream.chat.android.guides.catalog.compose.customizingimageandvideoattachments.ChannelsActivity as ComposeCustomizingImageAndVideoAttachmentsActivity
import io.getstream.chat.android.guides.catalog.compose.customreactions.ChannelsActivity as ComposeCustomReactionsActivity
import io.getstream.chat.android.guides.catalog.uicomponents.channelsscreen.ChannelsActivity as UiComponentsChannelsScreenActivity
import io.getstream.chat.android.guides.catalog.uicomponents.customattachments.ChannelsActivity as UiComponentsCustomAttachmentsActivity
import io.getstream.chat.android.guides.catalog.uicomponents.customcomposer.ChannelsActivity as UiComponentsCustomComposerActivity
import io.getstream.chat.android.guides.catalog.uicomponents.customreactions.ChannelsActivity as UiComponentsCustomReactionsActivity
import io.getstream.chat.android.guides.catalog.uicomponents.messagesscreen.ChannelsActivity as UiComponentsMessagesScreenActivity

/**
 * An Activity with a list of guides and samples.
 */
class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChatTheme {
                CatalogScreen()
            }
        }
    }

    /**
     * A Composable that represents a screen with a list of guides and samples.
     */
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
                onClick = ::logout,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = null,
                    tint = ChatTheme.colors.textPrimary,
                )
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
            ) {
                GuideSection(stringResource(R.string.catalog_section_compose))

                GuideItem(
                    titleText = stringResource(R.string.compose_custom_attachments_guide_title),
                    descriptionText = stringResource(R.string.compose_custom_attachments_guide_description),
                ) {
                    startActivity(ComposeCustomAttachmentsActivity.createIntent(this@CatalogActivity))
                }

                GuideItem(
                    titleText = stringResource(R.string.compose_customizing_image_and_video_previews_title),
                    descriptionText = stringResource(R.string.compose_customizing_image_and_video_previews_description),
                ) {
                    startActivity(ComposeCustomizingImageAndVideoAttachmentsActivity.createIntent(this@CatalogActivity))
                }

                GuideItem(
                    titleText = stringResource(R.string.compose_custom_reactions_guide_title),
                    descriptionText = stringResource(R.string.compose_custom_reactions_guide_description),
                ) {
                    startActivity(ComposeCustomReactionsActivity.createIntent(this@CatalogActivity))
                }

                GuideSection(stringResource(R.string.catalog_section_uicomponents))

                GuideItem(
                    titleText = stringResource(R.string.uicomponents_custom_attachments_guide_title),
                    descriptionText = stringResource(R.string.uicomponents_custom_attachments_guide_description),
                ) {
                    startActivity(UiComponentsCustomAttachmentsActivity.createIntent(this@CatalogActivity))
                }

                GuideItem(
                    titleText = stringResource(R.string.uicomponents_custom_reactions_guide_title),
                    descriptionText = stringResource(R.string.uicomponents_custom_reactions_guide_description),
                ) {
                    startActivity(UiComponentsCustomReactionsActivity.createIntent(this@CatalogActivity))
                }

                GuideItem(
                    titleText = stringResource(R.string.uicomponents_channels_screen_guide_title),
                    descriptionText = stringResource(R.string.uicomponents_channels_screen_guide_description),
                ) {
                    startActivity(UiComponentsChannelsScreenActivity.createIntent(this@CatalogActivity))
                }

                GuideItem(
                    titleText = stringResource(R.string.uicomponents_messages_screen_guide_title),
                    descriptionText = stringResource(R.string.uicomponents_messages_screen_guide_description),
                ) {
                    startActivity(UiComponentsMessagesScreenActivity.createIntent(this@CatalogActivity))
                }

                GuideItem(
                    titleText = stringResource(R.string.uicomponents_custom_composer_guide_title),
                    descriptionText = stringResource(R.string.uicomponents_custom_composer_guide_description),
                ) {
                    startActivity(UiComponentsCustomComposerActivity.createIntent(this@CatalogActivity))
                }
            }
        }
    }

    @Composable
    private fun GuideSection(text: String) {
        Text(
            text = text,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            style = ChatTheme.typography.headingMedium,
            color = ChatTheme.colors.textPrimary,
        )
    }

    @Composable
    private fun GuideItem(
        titleText: String,
        descriptionText: String,
        onClick: () -> Unit,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(
                    onClick = onClick,
                    indication = ripple(),
                    interactionSource = remember { MutableInteractionSource() },
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = ChatTheme.colors.backgroundCoreSurface),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = titleText,
                    style = ChatTheme.typography.bodyEmphasis,
                    fontSize = 16.sp,
                    color = ChatTheme.colors.textPrimary,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = descriptionText,
                    style = ChatTheme.typography.bodyDefault,
                    color = ChatTheme.colors.textPrimary,
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
