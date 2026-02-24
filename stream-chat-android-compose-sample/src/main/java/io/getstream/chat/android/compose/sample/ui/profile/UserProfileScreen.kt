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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.compose.sample.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.component.PaneRow
import io.getstream.chat.android.compose.sample.ui.component.PaneTitle
import io.getstream.chat.android.compose.ui.components.BackButton
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.PushPreferenceLevel
import io.getstream.chat.android.models.UnreadChannel
import io.getstream.chat.android.models.UnreadChannelByType
import io.getstream.chat.android.models.UnreadCounts
import io.getstream.chat.android.models.UnreadThread
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.util.Calendar

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigationIconClick: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = viewModel<UserProfileViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var modalSheet by remember { mutableStateOf<ModalSheet?>(null) }

    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { result ->
        result?.let { imageUri ->
            val imageFile = imageUri.toCacheFile(context)
            viewModel.updateProfilePicture(imageFile)
        }
    }

    var cameraFile by remember { mutableStateOf<File?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { result ->
        if (result) {
            cameraFile?.let(viewModel::updateProfilePicture)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            Column {
                TopBar(onNavigationIconClick = onNavigationIconClick)
                LinearProgressIndicator(state = state.progressIndicator)
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = ChatTheme.colors.backgroundCoreApp,
    ) { paddingValues ->
        UserProfileScreenContent(
            modifier = Modifier.padding(paddingValues),
            state = state,
            onUnreadCountsClick = {
                modalSheet = ModalSheet.UnreadCounts
                viewModel.loadUnreadCounts()
            },
            onPushPreferencesClick = {
                modalSheet = ModalSheet.PushPreferences
            },
            onUpdateProfilePictureClick = {
                modalSheet = ModalSheet.UpdateProfilePicture
            },
            onUpdatePrivacySettingsClick = {
                modalSheet = ModalSheet.UpdatePrivacySettings
            },
        )
    }
    when (modalSheet) {
        ModalSheet.UnreadCounts -> ModalBottomSheet(
            onDismissRequest = { modalSheet = null },
            containerColor = ChatTheme.colors.backgroundCoreApp,
        ) {
            UnreadCounts(state.unreadCounts)
        }

        ModalSheet.PushPreferences -> ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { modalSheet = null },
            containerColor = ChatTheme.colors.backgroundCoreApp,
        ) {
            UserProfilePushPreferencesScreen(
                preferences = state.user?.pushPreference ?: PushPreference(PushPreferenceLevel.all, null),
                onSavePreferences = { level ->
                    modalSheet = null
                    viewModel.setPushPreferences(level)
                },
                onSnoozeNotifications = { until ->
                    modalSheet = null
                    viewModel.snoozeNotifications(until)
                },
            )
        }

        ModalSheet.UpdateProfilePicture -> ModalBottomSheet(
            onDismissRequest = { modalSheet = null },
            containerColor = ChatTheme.colors.backgroundCoreApp,
        ) {
            UpdateProfilePicture(
                onChooseFromLibraryClick = {
                    modalSheet = null
                    pickVisualMediaLauncher.launch(input = PickVisualMediaRequest())
                },
                onTakePhotoClick = {
                    modalSheet = null
                    cameraFile = context.generateCameraImageFile().also { file ->
                        takePictureLauncher.launch(input = file.getUri(context))
                    }
                },
                onRemovePhotoClick = {
                    modalSheet = null
                    viewModel.removeProfilePicture()
                },
            )
        }

        ModalSheet.UpdatePrivacySettings -> ModalBottomSheet(
            onDismissRequest = { modalSheet = null },
            containerColor = ChatTheme.colors.backgroundCoreApp,
        ) {
            state.user?.let { user ->
                UserProfilePrivacySettingsScreen(
                    privacySettings = user.privacySettings,
                    onSaveClick = { settings ->
                        modalSheet = null
                        viewModel.updatePrivacySettings(settings)
                    },
                )
            }
        }

        null -> Unit
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UserProfileViewEvent.Failure -> when (event) {
                    is UserProfileViewEvent.LoadUnreadCountsError -> {
                        modalSheet = null
                        snackbarHostState.showSnackbar(message = event.error.message, actionLabel = "Dismiss")
                    }

                    is UserProfileViewEvent.UpdateProfilePictureError,
                    is UserProfileViewEvent.RemoveProfilePictureError,
                    ->
                        snackbarHostState.showSnackbar(message = event.error.message, actionLabel = "Dismiss")

                    is UserProfileViewEvent.UpdatePushPreferencesError -> {
                        snackbarHostState.showSnackbar(message = event.error.message, actionLabel = "Dismiss")
                    }
                }

                is UserProfileViewEvent.UpdateProfilePictureSuccess ->
                    snackbarHostState.showSnackbar(message = "Profile picture updated")
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopBar(onNavigationIconClick: () -> Unit) {
    TopAppBar(
        title = {},
        navigationIcon = {
            BackButton(
                painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
                onBackPressed = onNavigationIconClick,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ChatTheme.colors.backgroundCoreApp,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinearProgressIndicator(
    state: UserProfileViewState.ProgressIndicator?,
) {
    Box(
        modifier = Modifier.heightIn(min = ProgressIndicatorDefaults.LinearIndicatorTrackGapSize),
    ) {
        when {
            state == null -> Unit

            state.progress != null -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { state.progress },
                    color = ChatTheme.colors.accentPrimary,
                    trackColor = ChatTheme.colors.backgroundCoreSurface,
                    drawStopIndicator = { /* Don't draw the stop indicator */ },
                )
            }

            else -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = ChatTheme.colors.accentPrimary,
                    trackColor = ChatTheme.colors.backgroundCoreSurface,
                )
            }
        }
    }
}

private enum class ModalSheet {
    UnreadCounts,
    PushPreferences,
    UpdateProfilePicture,
    UpdatePrivacySettings,
}

@Suppress("LongMethod")
@Composable
private fun UserProfileScreenContent(
    state: UserProfileViewState,
    modifier: Modifier = Modifier,
    onUnreadCountsClick: () -> Unit = {},
    onPushPreferencesClick: () -> Unit = {},
    onUpdateProfilePictureClick: () -> Unit = {},
    onUpdatePrivacySettingsClick: () -> Unit = {},
) {
    when (val user = state.user) {
        null -> {
            LoadingIndicator(
                modifier = modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            )
        }

        else -> {
            Column(
                modifier = modifier
                    .fillMaxWidth(),
            ) {
                UserProfilePicture(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    user = user,
                    enabled = state.progressIndicator == null,
                    onClick = onUpdateProfilePictureClick,
                )
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .padding(start = 16.dp),
                ) {
                    Text(
                        text = "Name",
                        style = ChatTheme.typography.headingMedium,
                        color = ChatTheme.colors.textPrimary,
                    )
                    Text(
                        text = user.name.takeIf(String::isNotBlank) ?: user.id,
                        style = ChatTheme.typography.bodyDefault,
                        color = ChatTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Divider()
                val avgResponseTimeInSeconds = user.avgResponseTime ?: 0
                if (avgResponseTimeInSeconds > 0) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(start = 16.dp),
                    ) {
                        Text(
                            text = "Average Response Time",
                            style = ChatTheme.typography.headingMedium,
                            color = ChatTheme.colors.textPrimary,
                        )
                        Text(
                            text = LocalContext.current.formatTime(seconds = avgResponseTimeInSeconds),
                            style = ChatTheme.typography.bodyDefault,
                            color = ChatTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Divider()
                }
                NavigationItem(
                    label = "Unread Counts",
                    onClick = onUnreadCountsClick,
                )
                Divider()
                NavigationItem(
                    label = "Push Preferences",
                    onClick = onPushPreferencesClick,
                )
                Divider()
                NavigationItem(
                    label = "Privacy Settings",
                    onClick = onUpdatePrivacySettingsClick,
                )
            }
        }
    }
}

@Composable
private fun NavigationItem(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(start = 16.dp)
            .minimumInteractiveComponentSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ChatTheme.typography.headingMedium,
            color = ChatTheme.colors.textPrimary,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ChatTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun UserProfilePicture(
    modifier: Modifier,
    user: User,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = ripple(bounded = false),
                onClick = onClick,
            ),
    ) {
        UserAvatar(
            modifier = Modifier.size(72.dp),
            user = user,
        )
        Icon(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(24.dp)
                .background(
                    color = ChatTheme.colors.backgroundElevationElevation1,
                    shape = CircleShape,
                )
                .padding(4.dp),
            imageVector = Icons.Rounded.Edit,
            contentDescription = null,
            tint = ChatTheme.colors.textSecondary,
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun UpdateProfilePicture(
    onChooseFromLibraryClick: () -> Unit = {},
    onTakePhotoClick: () -> Unit = {},
    onRemovePhotoClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    ) {
        Text(
            text = "Update Profile Picture",
            style = ChatTheme.typography.headingMedium,
            color = ChatTheme.colors.textPrimary,
            modifier = Modifier.padding(16.dp),
        )
        TextButton(
            shape = RectangleShape,
            onClick = onChooseFromLibraryClick,
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoLibrary,
                contentDescription = null,
                tint = ChatTheme.colors.textSecondary,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = "Choose from library",
                style = ChatTheme.typography.headingMedium,
                color = ChatTheme.colors.textPrimary,
            )
        }
        TextButton(
            shape = RectangleShape,
            onClick = onTakePhotoClick,
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoCamera,
                contentDescription = null,
                tint = ChatTheme.colors.textSecondary,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = "Take a photo",
                style = ChatTheme.typography.headingMedium,
                color = ChatTheme.colors.textPrimary,
            )
        }
        TextButton(
            shape = RectangleShape,
            onClick = onRemovePhotoClick,
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = null,
                tint = ChatTheme.colors.accentError.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = "Remove picture",
                style = ChatTheme.typography.headingMedium,
                color = ChatTheme.colors.accentError,
            )
        }
    }
}

@Composable
private fun UnreadCounts(unreadCounts: UnreadCounts?) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        val counts = unreadCounts ?: UnreadCounts()
        val unreadCountByTeam = remember(counts.messagesCountByTeam) {
            counts.messagesCountByTeam.entries.toList()
        }
        val summaries = remember(
            counts.messagesCount,
            counts.messagesCount,
            counts.threadsCount,
        ) {
            mapOf(
                "Total Unread Messages" to counts.messagesCount,
                "Total Unread Channels" to counts.channels.size,
                "Total Unread Threads" to counts.threadsCount,
            ).entries.toList()
        }
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
        ) {
            summary(items = summaries)
            unreadChannels(items = counts.channels)
            unreadThreads(items = counts.threads)
            unreadChannelsByType(items = counts.channelsByType)
            unreadChannelsByTeam(items = unreadCountByTeam)
        }
        if (unreadCounts == null) {
            LoadingIndicator()
        }
    }
}

private fun LazyListScope.summary(
    items: List<Map.Entry<String, Int>>,
) {
    item {
        PaneTitle(
            text = "SUMMARY",
            padding = PaddingValues(
                start = 16.dp,
                bottom = 8.dp,
                end = 16.dp,
            ),
        )
    }
    itemsIndexed(
        items = items,
        key = { _, item -> item.key },
    ) { index, (title, count) ->
        PaneRow(
            index = index,
            lastIndex = items.lastIndex,
        ) {
            Text(
                text = title,
                style = ChatTheme.typography.bodyDefault,
                color = ChatTheme.colors.textPrimary,
            )
            Text(
                text = "$count",
                style = ChatTheme.typography.bodyEmphasis,
                color = ChatTheme.colors.textPrimary,
            )
        }
    }
}

private fun LazyListScope.unreadChannels(
    items: List<UnreadChannel>,
) {
    item {
        PaneTitle(text = "UNREAD CHANNELS (${items.size})")
    }
    itemsIndexed(
        items = items,
        key = { _, item -> item.cid },
    ) { index, item ->
        PaneRow(
            index = index,
            lastIndex = items.lastIndex,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.cid,
                    style = ChatTheme.typography.bodyEmphasis,
                    color = ChatTheme.colors.textPrimary,
                )
                Text(
                    text = "Last read ${ChatTheme.dateFormatter.formatRelativeTime(item.lastRead)}",
                    style = ChatTheme.typography.metadataDefault,
                    color = ChatTheme.colors.textSecondary,
                )
            }
            CountText(
                count = item.messagesCount,
                backgroundColor = Color.Red.copy(alpha = 0.1f),
            )
        }
    }
}

private fun LazyListScope.unreadThreads(
    items: List<UnreadThread>,
) {
    item {
        PaneTitle(text = "UNREAD THREADS (${items.size})")
    }
    itemsIndexed(
        items = items,
        key = { _, item -> item.parentMessageId },
    ) { index, item ->
        PaneRow(
            index = index,
            lastIndex = items.lastIndex,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.parentMessageId,
                    style = ChatTheme.typography.bodyEmphasis,
                    color = ChatTheme.colors.textPrimary,
                )
                Text(
                    text = "Last read ${ChatTheme.dateFormatter.formatRelativeTime(item.lastRead)}",
                    style = ChatTheme.typography.metadataDefault,
                    color = ChatTheme.colors.textSecondary,
                )
                Text(
                    text = "Last read message: ${item.lastReadMessageId}",
                    style = ChatTheme.typography.metadataDefault,
                    color = ChatTheme.colors.textSecondary,
                )
            }
            CountText(
                count = item.messagesCount,
                backgroundColor = Color.Orange.copy(alpha = 0.1f),
            )
        }
    }
}

@Suppress("MagicNumber")
private val Color.Companion.Orange get() = Color(0xFFFFA500)

@Suppress("MagicNumber")
private val Color.Companion.Purple get() = Color(0xFF800080)

private fun LazyListScope.unreadChannelsByType(
    items: List<UnreadChannelByType>,
) {
    item {
        PaneTitle(text = "UNREAD BY CHANNEL TYPE")
    }
    itemsIndexed(
        items = items,
        key = { _, item -> item.channelType },
    ) { index, item ->
        PaneRow(
            index = index,
            lastIndex = items.lastIndex,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.channelType,
                    style = ChatTheme.typography.bodyEmphasis,
                    color = ChatTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${item.channelsCount} channels",
                    style = ChatTheme.typography.metadataDefault,
                    color = ChatTheme.colors.textSecondary,
                )
            }
            CountText(
                count = item.messagesCount,
                backgroundColor = Color.Blue.copy(alpha = 0.1f),
            )
        }
    }
}

private fun LazyListScope.unreadChannelsByTeam(
    items: List<Map.Entry<String, Int>>,
) {
    item {
        PaneTitle(text = "UNREAD BY TEAM (${items.size})")
    }
    itemsIndexed(
        items = items,
        key = { _, item -> item.key },
    ) { index, (team, count) ->
        PaneRow(
            index = index,
            lastIndex = items.lastIndex,
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = team,
                style = ChatTheme.typography.bodyDefault,
                color = ChatTheme.colors.textPrimary,
            )
            CountText(
                count = count,
                backgroundColor = Color.Purple.copy(alpha = 0.1f),
            )
        }
    }
}

@Composable
private fun CountText(
    count: Int,
    backgroundColor: Color,
) {
    Text(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        text = count.toString(),
        style = ChatTheme.typography.bodyEmphasis,
        color = ChatTheme.colors.textSecondary,
    )
}

@Composable
private fun Divider() {
    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
}

@Preview(showBackground = true)
@Composable
private fun UserProfileScreenContentPreview() {
    ChatTheme {
        UserProfileScreenContent(
            modifier = Modifier.fillMaxSize(),
            state = UserProfileViewState(
                user = User(
                    id = "user-id",
                    name = "John Doe",
                    avgResponseTime = 300,
                ),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateProfilePicturePreview() {
    ChatTheme {
        UpdateProfilePicture()
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun UnreadCountsLoadingPreview() {
    ChatTheme {
        UnreadCounts(
            unreadCounts = null,
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun UnreadCountsPreview() {
    val lastReadDate = Calendar.getInstance().apply {
        set(2025, Calendar.AUGUST, 15, 8, 15)
    }.time
    ChatTheme {
        UnreadCounts(
            unreadCounts = UnreadCounts(
                messagesCount = 10,
                threadsCount = 5,
                messagesCountByTeam = mapOf("team1" to 5, "team2" to 3),
                channels = listOf(
                    UnreadChannel(
                        cid = "messaging:1",
                        messagesCount = 3,
                        lastRead = lastReadDate,
                    ),
                    UnreadChannel(
                        cid = "messaging:2",
                        messagesCount = 7,
                        lastRead = lastReadDate,
                    ),
                ),
                threads = listOf(
                    UnreadThread(
                        parentMessageId = "message:1",
                        messagesCount = 2,
                        lastRead = lastReadDate,
                        lastReadMessageId = "message:2",
                    ),
                ),
                channelsByType = listOf(
                    UnreadChannelByType(
                        channelType = "messaging",
                        channelsCount = 2,
                        messagesCount = 10,
                    ),
                ),
            ),
        )
    }
}
