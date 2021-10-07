package io.getstream.chat.android.compose.ui.channel.header

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.common.NetworkLoadingView
import io.getstream.chat.android.compose.ui.common.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A clean, decoupled UI element that doesn't rely on ViewModels or our custom architecture setup.
 * This allows the user to fully govern how the [ChannelListHeader] behaves, by passing in all the
 * data that's required to display it and drive its actions.
 *
 * @param modifier Modifier for styling.
 * @param title The title to display, when the network is available.
 * @param currentUser The currently logged in user, to load its image in the avatar.
 * @param isNetworkAvailable A flag that governs if we show the title or the network loading view.
 * @param onAvatarClick Action handler when the user taps on an avatar.
 * @param onHeaderActionClick Action handler when the user taps on the header action.
 * @param leadingContent Custom composable that allows the user to replace the default header leading content.
 * By default it shows a [UserAvatar].
 * @param trailingContent Custom composable that allows the user to completely replace the default header
 * action. If nothing is passed in, the default element will be built, using the [onHeaderActionClick]
 * parameter as its handler, and it will represent [DefaultChannelListHeaderAction].
 */
@Composable
public fun ChannelListHeader(
    modifier: Modifier = Modifier,
    title: String = "",
    currentUser: User? = null,
    isNetworkAvailable: Boolean = true,
    onAvatarClick: (User?) -> Unit = {},
    onHeaderActionClick: () -> Unit = {},
    leadingContent: @Composable RowScope.() -> Unit = {
        DefaultChannelHeaderLeadingContent(
            currentUser,
            onAvatarClick
        )
    },
    titleContent: @Composable RowScope.() -> Unit = {
        ChannelHeaderTitle(
            modifier = Modifier.weight(1f),
            isNetworkAvailable = isNetworkAvailable,
            title = title
        )
    },
    trailingContent: @Composable RowScope.() -> Unit = { DefaultChannelListHeaderAction(onHeaderActionClick) },
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        elevation = 4.dp,
        color = ChatTheme.colors.barsBackground,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            leadingContent()

            titleContent()

            trailingContent()
        }
    }
}

/**
 * Represents the default leading content for the [ChannelListHeader], which is a [UserAvatar].
 *
 * We show the avatar if the user is available, otherwise we add a spacer to make sure the alignment is correct.
 */
@Composable
internal fun DefaultChannelHeaderLeadingContent(
    currentUser: User?,
    onAvatarClick: (User?) -> Unit,
) {
    val size = Modifier.size(36.dp)

    if (currentUser != null) {
        UserAvatar(
            modifier = size,
            user = currentUser,
            contentDescription = currentUser.name,
            onClick = { onAvatarClick(currentUser) }
        )
    } else {
        Spacer(modifier = size)
    }
}

/**
 * Represents the channel header's title slot. It either shows a [Text] if [isNetworkAvailable] is true,
 * or a [NetworkLoadingView] if there is no connections.
 *
 * @param isNetworkAvailable If the network connection is available or not.
 * @param title The title to show.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun ChannelHeaderTitle(
    isNetworkAvailable: Boolean,
    title: String,
    modifier: Modifier = Modifier,
) {
    if (isNetworkAvailable) {
        Text(
            modifier = modifier
                .wrapContentWidth()
                .padding(horizontal = 16.dp),
            text = title,
            style = ChatTheme.typography.title3Bold,
            maxLines = 1,
            color = ChatTheme.colors.textHighEmphasis
        )
    } else {
        NetworkLoadingView(modifier = modifier)
    }
}

/**
 * Represents the default action for the ChannelList header.
 *
 * @param onHeaderActionClick Handler for when the user taps on the action.
 * @param modifier Modifier for styling.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun DefaultChannelListHeaderAction(
    onHeaderActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .size(36.dp)
            .shadow(4.dp, shape = CircleShape, clip = true),
        onClick = onHeaderActionClick,
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(bounded = false, radius = 24.dp),
        color = ChatTheme.colors.primaryAccent,
    ) {
        Icon(
            modifier = Modifier.wrapContentSize(),
            painter = painterResource(id = R.drawable.stream_compose_ic_edit),
            contentDescription = stringResource(id = R.string.stream_compose_edit_action),
            tint = Color.White,
        )
    }
}
