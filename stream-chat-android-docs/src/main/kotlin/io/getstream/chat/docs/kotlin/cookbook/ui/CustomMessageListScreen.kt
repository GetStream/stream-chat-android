package io.getstream.chat.docs.kotlin.cookbook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.models.Message
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CustomMessageListScreen(viewModel: CustomMessageListViewModel = viewModel(), cid: String?) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) { cid?.let { viewModel.loadNewestMessages(it) } }

    CustomMessageList(messages = uiState.value.messages)
}

@Composable
private fun CustomMessageList(messages: List<Message>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        reverseLayout = true,
    ) {
        items(messages) { message ->
            if (message.text != "") CustomMessageListItem(message = message)
        }
    }
}

@Composable
private fun CustomMessageListItem(message: Message) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Column {
        Text(text = "${message.user.name} said:", fontSize = 12.sp, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = Color(0xFFEEEEEE),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 10.dp)
                )
                .padding(all = 10.dp)
        ) {
            Text(text = message.text)
            Spacer(modifier = Modifier.width(15.dp))
            message.createdAt?.let {
                Text(text = timeFormat.format(it), fontSize = 12.sp, fontWeight = FontWeight.Light)
            }
        }
    }
}