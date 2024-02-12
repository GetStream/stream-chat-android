package io.getstream.chat.docs.kotlin.cookbook.ui.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*

@Composable
fun LazyListState.OnListEndReached(buffer: Int = 0, handler: () -> Unit) {
    val shouldCallHandler by remember {
        derivedStateOf {
            layoutInfo.visibleItemsInfo.lastOrNull()?.let { lastVisibleItem ->
                lastVisibleItem.index == layoutInfo.totalItemsCount - 1 - buffer
            } ?: false
        }
    }

    LaunchedEffect(shouldCallHandler) {
        if (shouldCallHandler) handler()
    }
}
