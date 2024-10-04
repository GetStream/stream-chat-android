package io.getstream.chat.android.compose.sample.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.sample.vm.MembersViewModel

@Composable
internal fun MembersList(
    viewModel: MembersViewModel,
) {
    val memberNames by viewModel.memberNames.collectAsStateWithLifecycle()
    LazyRow(
        modifier = Modifier.height(32.dp)
            .fillMaxWidth()
            .background(color = Color.LightGray),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(memberNames) { index, (name, isCurrentUser) ->
            if (index == 0) {
                Spacer(modifier = Modifier.width(16.dp))
            }
            val displayName = when (index == memberNames.lastIndex) {
                true -> name
                else -> "$name, "
            }
            Text(
                modifier = Modifier,
                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                text = displayName,
            )
            if (index == memberNames.lastIndex) {
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}