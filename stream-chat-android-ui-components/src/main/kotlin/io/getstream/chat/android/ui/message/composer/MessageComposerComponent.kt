package io.getstream.chat.android.ui.message.composer

import io.getstream.chat.android.common.composer.MessageComposerState

/**
 * An interface implemented by [MessageComposerView] and its children that is
 * used for state propagation.
 */
public interface MessageComposerComponent {
    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    public fun renderState(state: MessageComposerState)
}
