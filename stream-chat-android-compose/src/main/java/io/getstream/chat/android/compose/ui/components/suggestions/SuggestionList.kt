package io.getstream.chat.android.compose.ui.components.suggestions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the suggestion list popup that allows user to auto complete the current input.
 *
 * @param modifier Modifier for styling.
 * @param shape The shape of suggestion list popup.
 * @param contentPadding The inner content padding inside the popup.
 * @param headerContent The content shown at the top of a suggestion list popup.
 * @param content The content shown inside the suggestion list popup.
 */
@Composable
public fun SuggestionList(
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.suggestionList,
    contentPadding: PaddingValues = PaddingValues(vertical = ChatTheme.dimens.suggestionListPadding),
    headerContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Popup(popupPositionProvider = SuggestionListPositionProvider()) {
        Card(
            modifier = modifier,
            elevation = ChatTheme.dimens.suggestionListElevation,
            shape = shape,
            backgroundColor = ChatTheme.colors.barsBackground,
        ) {
            Column(Modifier.padding(contentPadding)) {
                headerContent()

                content()
            }
        }
    }
}

/**
 * Calculates the position of the suggestion list [Popup] on the screen.
 */
private class SuggestionListPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        return IntOffset(
            x = 0,
            y = anchorBounds.top - popupContentSize.height
        )
    }
}
