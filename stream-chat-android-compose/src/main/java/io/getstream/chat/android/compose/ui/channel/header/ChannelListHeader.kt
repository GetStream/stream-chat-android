package io.getstream.chat.android.compose.ui.channel.header

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.common.Avatar
import io.getstream.chat.android.compose.ui.common.NetworkLoadingView
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A clean, decoupled UI element that doesn't rely on ViewModels or our custom architecture setup.
 * This allows the user to fully govern how the [ChannelListHeader] behaves, by passing in all the
 * data that's required to display it and drive its actions.
 *
 * @param modifier - Modifier for styling.
 * @param title - The title to display, when the network is available.
 * @param currentUser - The currently logged in user, to load its image in the avatar.
 * @param isNetworkAvailable - A flag that governs if we show the title or the network loading view.
 * @param onAvatarClick - Action handler when the user taps on an avatar.
 * @param onHeaderActionClick - Action handler when the user taps on the header action.
 * @param action - Custom composable that allows the user to completely replace the default header
 * action. If nothing is passed in, the default element will be built, using the [onHeaderActionClick]
 * parameter as its handler, and it will represent [DefaultChannelListHeaderAction].
 * */
@ExperimentalMaterialApi
@Composable
fun ChannelListHeader(
    modifier: Modifier = Modifier,
    title: String = "",
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    isNetworkAvailable: Boolean = true,
    onAvatarClick: (User?) -> Unit = {},
    onHeaderActionClick: () -> Unit = {},
    action: (@Composable () -> Unit)? = { DefaultChannelListHeaderAction(onHeaderActionClick) }
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        elevation = 4.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            val painter = rememberImagePainter(currentUser?.image)

            Avatar(
                modifier = Modifier
                    .size(48.dp)
                    .weight(1f),
                painter = painter,
                contentDescription = currentUser?.name,
                onClick = { onAvatarClick(currentUser) }
            )

            ChannelHeaderTitle(
                modifier = Modifier.weight(6f),
                isNetworkAvailable = isNetworkAvailable,
                title = title
            )

            action?.invoke()
        }
    }
}

/**
 * Represents the channel header's title slot. It either shows a [Text] if [isNetworkAvailable] is true,
 * or a [NetworkLoadingView] if there is no connections.
 *
 * @param isNetworkAvailable - If the network connection is available or not.
 * @param title - The title to show.
 * @param modifier - Modifier for styling.
 * */
@Composable
internal fun ChannelHeaderTitle(
    isNetworkAvailable: Boolean,
    title: String,
    modifier: Modifier = Modifier
) {
    if (isNetworkAvailable) {
        Text(
            modifier = modifier
                .wrapContentWidth()
                .padding(horizontal = 16.dp),
            text = title,
            style = ChatTheme.typography.title3Bold,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            color = ChatTheme.colors.textHighEmphasis
        )
    } else {
        NetworkLoadingView(modifier = modifier)
    }
}

/**
 * Represents the default action for the ChannelList header.
 *
 * @param onHeaderActionClick - Handler for when the user taps on the action.
 * @param modifier - Modifier for styling.
 * */
@ExperimentalMaterialApi
@Composable
internal fun DefaultChannelListHeaderAction(
    onHeaderActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(48.dp)
            .shadow(4.dp, shape = CircleShape, clip = true),
        onClick = onHeaderActionClick,
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(bounded = false, radius = 24.dp)
    ) {
        Icon(
            modifier = Modifier.wrapContentSize(),
            imageVector = Icons.Default.Edit,
            contentDescription = stringResource(id = R.string.edit_action)
        )
    }
}