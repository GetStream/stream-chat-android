/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.widgets.internal

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import io.getstream.chat.android.core.internal.StreamHandsOff
import io.getstream.chat.android.ui.R

/**
 * A custom view that wraps Media3's [androidx.media3.ui.PlayerView] with automatic inflation handling.
 *
 * This view automatically inflates the PlayerView layout with custom root and controller layouts
 * to avoid conflicts with different versions of the ExoPlayer library in integration projects.
 *
 * Usage:
 * ```xml
 * <io.getstream.chat.android.ui.widgets.StreamPlayerView
 *     android:id="@+id/streamPlayerView"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 * ```
 *
 * Then in code:
 * ```kotlin
 * streamPlayerView.player = exoPlayer
 * streamPlayerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
 * ```
 */

@OptIn(UnstableApi::class)
internal class StreamPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val playerView: PlayerView

    /**
     * Gets or sets the [androidx.media3.common.Player] instance.
     */
    var player: Player?
        get() = playerView.player
        set(value) {
            playerView.player = value
        }

    init {
        playerView = inflatePlayerView()
        addView(playerView)
    }

    @StreamHandsOff(
        "This method manually inflates the PlayerView layout, because we are overriding the default " +
            "root layout (player_layout_id) and the default controller layout (controller_layout_id). " +
            "These layouts are just copies of the 'exo_player_view.xml' and the 'exo_player_control_view.xml' from " +
            "the ExoPlayer library. They only have a different name (layout ID) to avoid being overridden by layouts " +
            "with the same name from different versions of the ExoPlayer library (included in integration projects). " +
            "This ensures that we always use the correct layout for our version of the ExoPlayer library",
    )
    private fun inflatePlayerView(): PlayerView {
        return LayoutInflater
            .from(context)
            .inflate(R.layout.stream_ui_player_view, this, false) as PlayerView
    }

    /**
     * Sets whether buffering should be shown.
     *
     * @param showBuffering One of [PlayerView.SHOW_BUFFERING_NEVER],
     * [PlayerView.SHOW_BUFFERING_WHEN_PLAYING], or [PlayerView.SHOW_BUFFERING_ALWAYS].
     */
    fun setShowBuffering(showBuffering: Int) {
        playerView.setShowBuffering(showBuffering)
    }

    /**
     * Sets the artwork display mode.
     *
     * @param artworkDisplayMode One of [PlayerView.ARTWORK_DISPLAY_MODE_OFF],
     * [PlayerView.ARTWORK_DISPLAY_MODE_FIT], or [PlayerView.ARTWORK_DISPLAY_MODE_FILL].
     */
    fun setArtworkDisplayMode(artworkDisplayMode: Int) {
        playerView.artworkDisplayMode = artworkDisplayMode
    }

    /**
     * Sets whether the controller should be shown.
     *
     * @param useController Whether to use the controller.
     */
    fun setUseController(useController: Boolean) {
        playerView.useController = useController
    }

    /**
     * Sets whether the controller should be shown automatically.
     *
     * @param autoShow Whether to show the controller automatically.
     */
    fun setControllerAutoShow(autoShow: Boolean) {
        playerView.controllerAutoShow = autoShow
    }

    /**
     * Sets the controller show timeout.
     *
     * @param timeout the timeout in milliseconds.
     */
    fun setControllerShowTimeoutMs(timeout: Int) {
        playerView.controllerShowTimeoutMs = timeout
    }

    /**
     * Sets whether the controller should hide on touch.
     *
     * @param controllerHideOnTouch Whether the controller should hide on touch.
     */
    fun setControllerHideOnTouch(controllerHideOnTouch: Boolean) {
        playerView.controllerHideOnTouch = controllerHideOnTouch
    }

    /**
     * Sets whether the "Previous" button shown be shown.
     *
     * @param show Whether the "Previous" button should be shown.
     */
    fun setShowPreviousButton(show: Boolean) {
        playerView.setShowPreviousButton(show)
    }

    /**
     * Sets whether the "Next" button shown be shown.
     *
     * @param show Whether the "Next" button should be shown.
     */
    fun setShowNextButton(show: Boolean) {
        playerView.setShowNextButton(show)
    }
}
