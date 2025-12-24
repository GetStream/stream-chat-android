package io.getstream.chat.android.compose.ui.components.base

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import io.getstream.chat.android.compose.ui.util.clickable
import kotlin.random.Random

@Preview(showBackground = true, widthDp = 800, heightDp = 800)
@Composable
private fun StreamContextualMenuPreview() {
    var expanded by remember { mutableStateOf(false) }

    StreamContextualMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        repeat(10) {
            StreamContextualMenuItem(destructive = Random.nextBoolean(), enabled = Random.nextBoolean(), {})
        }
    }

    Button(onClick = { expanded = true }) { Text("Expandddd") }
}

@Composable
private fun StreamContextualMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    content: @Composable ColumnScope.() -> Unit,
) {
    // TODO [G.] DropdownMenu hardcodes a vertical padding :( Do we really have to recreate the whole thing?
    //  Although, it's possible we might want to do that anyway if we e.g. want a custom animation or no animation.
    //  Also maybe the default behavior in general might not be how we want our menu to behave
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        shadowElevation = 4.dp,
        // TODO [G.]
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        content = content
    )
}

@Composable
private fun StreamContextualMenuItem(
    destructive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .defaultMinSize(minWidth = 250.dp, minHeight = 40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // TODO [G.] do we hardcode the layout icon - text - icon, or do we accept anything? Both in 2 composables?
        //  Just hardcode given that integrators could easily replace the whole menu?
        val textColor = when {
            !enabled -> StreamColors.stateTextDisabled
            destructive -> StreamColors.accentError
            else -> StreamColors.textPrimary
        }

        Text(
            text = "Menu Item",
            modifier = Modifier.padding(horizontal = 8.dp),
            color = textColor
        )
    }
}
