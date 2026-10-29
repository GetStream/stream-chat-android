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

package io.getstream.chat.android.compose.ui.messages.attachments.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.StreamCardBottomSheet
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import io.getstream.chat.android.ui.common.R as UiCommonR

/**
 * Lets the user pick the camera app [launcher] opens, while it is asking for one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CaptureMediaTargetSheet(launcher: CaptureMediaLauncher) {
    if (!launcher.isPickingTarget) return
    val context = LocalContext.current
    val title = remember(launcher) { launcher.contract.title(context) }
    val items = remember(launcher) {
        val packageManager = context.packageManager
        launcher.contract.targets(context).map { target ->
            CaptureMediaTargetItem(
                label = target.activityInfo.loadLabel(packageManager).toString(),
                icon = target.activityInfo.loadIcon(packageManager).toBitmap().asImageBitmap(),
                subtitle = context.getString(target.kind.subtitle),
                onClick = { launcher.launch(target) },
            )
        }
    }
    StreamCardBottomSheet(onDismissRequest = { launcher.isPickingTarget = false }) {
        CaptureMediaTargetSheetContent(title = title, items = items)
    }
}

internal class CaptureMediaTargetItem(
    val label: String,
    val icon: ImageBitmap?,
    val subtitle: String,
    val onClick: () -> Unit,
)

@Composable
internal fun CaptureMediaTargetSheetContent(
    title: String,
    items: List<CaptureMediaTargetItem>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = StreamTokens.spacingMd,
                end = StreamTokens.spacingMd,
                bottom = StreamTokens.spacing2xl,
            ),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = StreamTokens.spacingMd),
            text = title,
            style = ChatTheme.typography.headingSmall,
            color = ChatTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingMd),
        ) {
            items.forEach { item -> CaptureMediaTarget(item) }
        }
    }
}

@Composable
private fun CaptureMediaTarget(item: CaptureMediaTargetItem) {
    Column(
        modifier = Modifier
            .width(TargetWidth)
            .clip(RoundedCornerShape(StreamTokens.radiusLg))
            .clickable(onClick = item.onClick)
            .padding(StreamTokens.spacingXs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val iconModifier = Modifier
            .padding(bottom = StreamTokens.spacingXs)
            .size(TargetIconSize)
        if (item.icon != null) {
            Image(
                modifier = iconModifier,
                painter = BitmapPainter(item.icon),
                contentDescription = null,
            )
        } else {
            Icon(
                modifier = iconModifier,
                painter = painterResource(R.drawable.stream_design_ic_camera),
                contentDescription = null,
                tint = ChatTheme.colors.textSecondary,
            )
        }
        Text(
            text = item.label,
            style = ChatTheme.typography.captionDefault,
            color = ChatTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = item.subtitle,
            style = ChatTheme.typography.metadataDefault,
            color = ChatTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val CaptureMediaContract.Target.Kind.subtitle: Int
    get() = when (this) {
        CaptureMediaContract.Target.Kind.PHOTO -> UiCommonR.string.stream_ui_message_composer_capture_media_take_photo
        CaptureMediaContract.Target.Kind.VIDEO -> UiCommonR.string.stream_ui_message_composer_capture_media_video
    }

private val TargetWidth = 88.dp
private val TargetIconSize = 48.dp

@Preview(showBackground = true)
@Composable
private fun CaptureMediaTargetSheetContentPreview() {
    ChatTheme {
        CaptureMediaTargetSheetContent()
    }
}

@Composable
internal fun CaptureMediaTargetSheetContent() {
    CaptureMediaTargetSheetContent(
        title = "Capture Image or Video",
        items = listOf(
            CaptureMediaTargetItem(label = "Camera", icon = null, subtitle = "Capture Image", onClick = {}),
            CaptureMediaTargetItem(label = "Camera", icon = null, subtitle = "Capture Video", onClick = {}),
        ),
    )
}
