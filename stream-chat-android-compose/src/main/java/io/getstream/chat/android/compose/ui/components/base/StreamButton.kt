package io.getstream.chat.android.compose.ui.components.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
internal fun StreamButtonStyle.contentColor(enabled: Boolean) =
    if (enabled) contentColor else disabledContentColor

@Stable
internal fun StreamButtonStyle.containerColor(enabled: Boolean) =
    if (enabled) containerColor else disabledContainerColor

@Stable
internal fun StreamButtonStyle.border(enabled: Boolean) =
    if (enabled) border else disabledBorder

public data class StreamButtonStyle(
    public val containerColor: Color,
    public val contentColor: Color,
    public val disabledContainerColor: Color,
    public val disabledContentColor: Color,
    public val border: BorderStroke?,
    public val disabledBorder: BorderStroke?,
)

public object StreamButtonStyleDefaults {
    public val primarySolid: StreamButtonStyle
        @Composable
        get() = StreamButtonStyle(
            containerColor = Colors.buttonPrimaryBackground,
            contentColor = Colors.buttonPrimaryText,
            disabledContainerColor = Colors.stateBackgroundDisabled,
            disabledContentColor = Colors.stateTextDisabled,
            border = BorderStroke(1.dp, Colors.buttonPrimaryBorder),
            disabledBorder = null,
        )
    public val primaryGhost: StreamButtonStyle
        @Composable
        get() = StreamButtonStyle(
            containerColor = Colors.buttonGhostBackground,
            contentColor = Colors.buttonGhostText,
            disabledContainerColor = Colors.buttonGhostBackground,
            disabledContentColor = Colors.stateTextDisabled,
            border = null,
            disabledBorder = null,
        )
    public val secondaryOutline: StreamButtonStyle
        @Composable
        get() = StreamButtonStyle(
            containerColor = Colors.buttonOutlineBackground,
            contentColor = Colors.buttonSecondaryText,
            disabledContainerColor = Colors.buttonOutlineBackground,
            disabledContentColor = Colors.stateTextDisabled,
            border = BorderStroke(1.dp, Colors.buttonOutlineBorder),
            disabledBorder = BorderStroke(1.dp, Colors.buttonOutlineBorder),
        )
    public val secondaryGhost: StreamButtonStyle
        @Composable
        get() = StreamButtonStyle(
            containerColor = Colors.buttonGhostBackground,
            contentColor = Colors.buttonSecondaryText,
            disabledContainerColor = Colors.buttonGhostBackground,
            disabledContentColor = Colors.stateTextDisabled,
            border = null,
            disabledBorder = null,
        )

    public val destructiveSolid: StreamButtonStyle
        @Composable
        get() = StreamButtonStyle(
            containerColor = Colors.buttonDestructiveBackground,
            contentColor = Colors.buttonDestructiveText,
            disabledContainerColor = Colors.stateBackgroundDisabled,
            disabledContentColor = Colors.stateTextDisabled,
            border = null,
            disabledBorder = null,
        )
}

private object Colors {
    val buttonPrimaryBackground = Color(0xFF005FFF)
    val buttonPrimaryBorder = Color(0xFF0052CE)
    val buttonPrimaryText = Color.White

    val buttonGhostBackground = Color.Transparent
    val buttonGhostText = Color(0xFF384047)

    val buttonOutlineBackground = Color.Transparent
    val buttonOutlineBorder = Color(0xFFE2E6EA)

    val buttonSecondaryText = Color(0xFF384047)

    val buttonDestructiveBackground = Color(0xFFD92F26)
    val buttonDestructiveText = Color.White

    val stateBackgroundDisabled = Color(0xFFE2E6EA)
    val stateTextDisabled = Color(0xFFB8BEC4)

    val todo = Color.Green
}

private enum class StreamButtonSize(val minimumSize: Dp) {
    Small(32.dp), Medium(40.dp), Large(48.dp)
}

// TODO [G.] should this be exposed or only used internally?
@Composable
private fun StreamButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: StreamButtonStyle = StreamButtonStyleDefaults.primarySolid,
    size: StreamButtonSize = StreamButtonSize.Medium,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        enabled = enabled,
        shape = RoundedCornerShape(50),
        color = style.containerColor(enabled),
        contentColor = style.contentColor(enabled),
        border = style.border(enabled),
        interactionSource = remember(::MutableInteractionSource)
    ) {
        Row(
            Modifier
                .defaultMinSize(minHeight = size.minimumSize)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun StreamIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: StreamButtonStyle = StreamButtonStyleDefaults.primarySolid,
    size: StreamButtonSize = StreamButtonSize.Medium,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .defaultMinSize(size.minimumSize, size.minimumSize)
                .clip(CircleShape)
                .background(color = style.containerColor(enabled))
                .run { style.border(enabled)?.let { border(it, CircleShape) } ?: this }
                .clickable(
                    onClick = onClick,
                    enabled = enabled,
                    role = Role.Button,
                    interactionSource = remember(::MutableInteractionSource),
                    indication = ripple()
                ),
        contentAlignment = Alignment.Center
    ) {
        val contentColor = style.contentColor(enabled)
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

private object PreviewData {
    val styles
        @Composable get() = listOf(
            StreamButtonStyleDefaults.primarySolid to "Primary Solid",
            StreamButtonStyleDefaults.primaryGhost to "Primary Ghost",
            StreamButtonStyleDefaults.secondaryOutline to "Secondary Outline",
            StreamButtonStyleDefaults.secondaryGhost to "Secondary Ghost",
            StreamButtonStyleDefaults.destructiveSolid to "Destructive Solid",
        )
}

@Preview(showBackground = true)
@Composable
private fun StreamButtonPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PreviewData.styles.forEach { (style, name) ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StreamButton(onClick = {}, style = style, modifier = Modifier.defaultMinSize(150.dp)) {
                    Text(name)
                }
                StreamButton(onClick = {}, style = style, enabled = false, modifier = Modifier.defaultMinSize(150.dp)) {
                    Text(name)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StreamIconButtonPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // TODO [G.] we can't pass the same styles: the content color is different
        PreviewData.styles.forEach { (style, _) ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StreamIconButton(onClick = {}, style = style) {
                    Icon(Icons.Default.Add, null)
                }
                StreamIconButton(onClick = {}, style = style, enabled = false) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }
    }
}

// TODO [G.] how to do focused state?
//  - What api should we expose?
//  - In figma the focus border seems to be external to the button

// TODO [G.] How extensible/customizable should our buttons be?
//  Personally I'd say they shouldn't be that customizable. Rather, they should be easy to replace

// TODO [G.] what about shapes? Related to the above
