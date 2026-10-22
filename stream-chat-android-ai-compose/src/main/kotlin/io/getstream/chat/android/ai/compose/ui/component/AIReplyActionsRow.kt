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

package io.getstream.chat.android.ai.compose.ui.component

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.conversation.AIMessageRating
import io.getstream.chat.android.ai.compose.conversation.AIReplyActions
import io.getstream.chat.android.ai.compose.ui.component.internal.findActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The buttons under a finished reply: copy and share, then regenerate and rate when the app
 * handles them.
 *
 * Copying puts [text] on the clipboard and shows a check mark for a moment. Sharing opens the
 * system share sheet with [text]. Regenerate shows only when [AIReplyActions.onRegenerate] is set,
 * and the thumbs up and down only when [AIReplyActions.onRate] is set. The thumb that matches
 * [AIReplyActions.rating] is filled and reads as selected. Tapping it again takes the rating back.
 *
 * @param text The reply's text, which copying and sharing hand on.
 * @param actions What else the person can do with the reply.
 * @param modifier Modifier to be applied to the row.
 */
@Composable
public fun AIReplyActionsRow(
    text: String,
    actions: AIReplyActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        CopyButton(text = text, tint = tint)
        ActionButton(
            icon = R.drawable.stream_ai_compose_ic_share,
            description = stringResource(R.string.stream_ai_compose_conversation_share),
            tint = tint,
            onClick = { shareText(context, text) },
        )
        actions.onRegenerate?.let { onRegenerate ->
            ActionButton(
                icon = R.drawable.stream_ai_compose_ic_regenerate,
                description = stringResource(R.string.stream_ai_compose_conversation_regenerate),
                tint = tint,
                onClick = onRegenerate,
            )
        }
        actions.onRate?.let { onRate ->
            RateButtons(rating = actions.rating, onRate = onRate, tint = tint)
        }
    }
}

@Composable
private fun CopyButton(text: String, tint: Color) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }
    // Bumped on every copy so a second copy keeps the check mark for the full time again.
    var copies by remember { mutableIntStateOf(0) }
    LaunchedEffect(copies) {
        if (copies > 0) {
            delay(CopiedDurationMs)
            isCopied = false
        }
    }
    ActionButton(
        icon = if (isCopied) R.drawable.stream_ai_compose_ic_check else R.drawable.stream_ai_compose_ic_copy,
        description = if (isCopied) {
            stringResource(R.string.stream_ai_compose_conversation_copied)
        } else {
            stringResource(R.string.stream_ai_compose_conversation_copy)
        },
        tint = tint,
        onClick = {
            scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(null, text))) }
            isCopied = true
            copies++
        },
    )
}

@Composable
private fun RateButtons(rating: AIMessageRating?, onRate: (AIMessageRating?) -> Unit, tint: Color) {
    val positive = rating == AIMessageRating.Positive
    val negative = rating == AIMessageRating.Negative
    ActionButton(
        icon = if (positive) {
            R.drawable.stream_ai_compose_ic_thumb_up_filled
        } else {
            R.drawable.stream_ai_compose_ic_thumb_up
        },
        description = stringResource(R.string.stream_ai_compose_conversation_rate_positive),
        tint = tint,
        selected = positive,
        onClick = { onRate(if (positive) null else AIMessageRating.Positive) },
    )
    ActionButton(
        icon = if (negative) {
            R.drawable.stream_ai_compose_ic_thumb_down_filled
        } else {
            R.drawable.stream_ai_compose_ic_thumb_down
        },
        description = stringResource(R.string.stream_ai_compose_conversation_rate_negative),
        tint = tint,
        selected = negative,
        onClick = { onRate(if (negative) null else AIMessageRating.Negative) },
    )
}

@Composable
private fun ActionButton(
    @DrawableRes icon: Int,
    description: String,
    tint: Color,
    onClick: () -> Unit,
    selected: Boolean? = null,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.semantics { selected?.let { this.selected = it } },
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = description,
            modifier = Modifier.size(IconSize),
        )
    }
}

private fun shareText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(send, null)
    // Outside an activity the share sheet needs a task of its own.
    if (context.findActivity() == null) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}

@Composable
internal fun AIReplyActionsRowSample(rating: AIMessageRating? = null) {
    AIReplyActionsRow(
        text = "Here is the weather for today.",
        actions = AIReplyActions(rating = rating, onRegenerate = {}, onRate = {}),
    )
}

@Preview(showBackground = true)
@Composable
private fun AIReplyActionsRowPreview() {
    AIReplyActionsRowSample(rating = AIMessageRating.Positive)
}

private const val CopiedDurationMs = 1_500L
private val IconSize = 18.dp
