package io.getstream.chat.docs.kotlin.cookbook.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getMembersStatusText
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.docs.R

@Composable
fun CustomMessageListHeader(cid: String?, onBackClick: () -> Unit = {}) {
    cid?.let {
        val viewModel = viewModel(
            modelClass = MessageListViewModel::class.java,
            factory = MessagesViewModelFactory(LocalContext.current, channelId = cid)
        )

        val channel = viewModel.channel
        val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
        val currentUser by viewModel.user.collectAsStateWithLifecycle()

        MessageListHeader(
            channel = channel,
            currentUser = currentUser,
            connectionState = connectionState,
            modifier = Modifier.height(55.dp),
            typingUsers = viewModel.typingUsers,
            messageMode = viewModel.messageMode,
            onBackPressed = onBackClick,
            color = Color(0xFF0F7B6F),
            leadingContent = { CustomHeaderLeadingContent(onClick = onBackClick) },
            centerContent = { CustomHeaderCenterContent(channel = channel, currentUser = currentUser) },
            trailingContent = { CustomHeaderTrailingContent() },
        )
    }
}

@Composable
private fun CustomHeaderLeadingContent(onClick: () -> Unit) {
    CustomHeaderButton(
        iconRes = R.drawable.ic_back,
        contentDescription = "Back",
        onClick = onClick
    )
}

@Composable
private fun CustomHeaderCenterContent(channel: Channel, currentUser: User?) {
    Row {
        ChannelAvatar(
            modifier = Modifier.size(40.dp),
            channel = channel,
            currentUser = currentUser,
        )
        Spacer(modifier = Modifier.size(10.dp))
        Column {
            Text(
                text = ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = channel.getMembersStatusText(LocalContext.current, currentUser),
                color = Color.LightGray,
                style = ChatTheme.typography.metadataDefault
            )
        }
    }
}

@Composable
private fun CustomHeaderTrailingContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = 10.dp),
        horizontalArrangement = Arrangement.End
    ) {
        CustomHeaderButton(
            iconRes = R.drawable.ic_videocam,
            contentDescription = "Video Call",
            onClick = {}
        )
        CustomHeaderButton(
            iconRes = R.drawable.ic_phone,
            contentDescription = "Audio Call",
            onClick = {}
        )
        CustomHeaderButton(
            iconRes = R.drawable.ic_menu,
            contentDescription = "Menu",
            onClick = {}
        )
    }
}

@Composable
private fun CustomHeaderButton(@DrawableRes iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        content = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                tint = Color.White,
            )
        }
    )
}