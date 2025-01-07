package io.getstream.chat.docs.kotlin.cookbook.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.request.ImageRequest
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.placeholder.PlaceholderPlugin
import io.getstream.chat.android.models.Channel
import io.getstream.chat.docs.R
import io.getstream.chat.docs.kotlin.cookbook.ui.common.OnListEndReached

@Composable
fun CustomChannelListScreen(
    viewModel: CustomChannelListViewModel = viewModel(),
    navigateToMessageList: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.error == null) {
        CustomChannelList(
            channels = uiState.channels,
            onChannelClick = navigateToMessageList,
            onListEndReached = viewModel::loadMoreChannels
        )
    } else {
        Error(message = uiState.error!!)
    }
}

@Composable
private fun CustomChannelList(channels: List<Channel>, onChannelClick: (String) -> Unit, onListEndReached: () -> Unit) {
    val listState = rememberLazyListState()
    listState.OnListEndReached(buffer = 5, handler = onListEndReached)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(all = 15.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        itemsIndexed(channels) { index, item ->
            CustomChannelListItem(channel = item, onChannelClick = onChannelClick)
            if (index < channels.lastIndex) {
                Spacer(modifier = Modifier.height(7.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun CustomChannelListItem(channel: Channel, onChannelClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChannelClick(channel.cid) }
            .padding(all = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = if (channel.name != "") channel.name else "Channel", fontWeight = FontWeight.Bold)
            Text(text = "Members: ${channel.memberCount}", fontWeight = FontWeight.Light)
        }
        ChannelImage(channel.image)
    }
}

@Composable
private fun ChannelImage(url: String) {
    // We use coil for getting the images
    val context = LocalContext.current
    CoilImage(
        imageRequest = {
            ImageRequest.Builder(context)
                .data(url)
                .crossfade(durationMillis = 500)
                .build()
        },
        modifier = Modifier
            .size(45.dp)
            .clip(shape = RoundedCornerShape(15.dp)),
        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
        component = rememberImageComponent {
            +PlaceholderPlugin.Loading(painterResource(id = R.drawable.ic_avatar))
            +PlaceholderPlugin.Failure(painterResource(id = R.drawable.ic_avatar))
        },
    )
}

@Composable
private fun Error(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomChannelList() {
    CustomChannelList(
        channels = listOf(
            Channel(name = "Byron Waelchi", memberCount = 10),
            Channel(name = "Bernice Li", memberCount = 5),
        ),
        onChannelClick = {},
        onListEndReached = {},
    )
}
