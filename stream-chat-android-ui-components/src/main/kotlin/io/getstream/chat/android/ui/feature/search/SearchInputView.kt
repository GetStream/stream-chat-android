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

package io.getstream.chat.android.ui.feature.search

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.transition.Fade
import androidx.transition.TransitionManager
import io.getstream.chat.android.ui.databinding.StreamUiSearchViewBinding
import io.getstream.chat.android.ui.utils.Debouncer
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.focusAndShowKeyboard
import io.getstream.chat.android.ui.utils.extensions.setTextSizePx
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

public class SearchInputView : FrameLayout {

    private companion object {
        private const val TYPING_DEBOUNCE_MS = 300L
        private const val FADE_DURATION = 300L
    }

    private val binding: StreamUiSearchViewBinding =
        StreamUiSearchViewBinding.inflate(streamThemeInflater, this, true)

    private var debouncedInputChangedListener: InputChangedListener? = null
    private var continuousInputChangedListener: InputChangedListener? = null
    private var searchStartedListener: SearchStartedListener? = null

    private val inputDebouncer = Debouncer(debounceMs = TYPING_DEBOUNCE_MS)

    private lateinit var style: SearchInputViewStyle

    private val query: String
        get() = binding.inputField.text.trim().toString()

    private var disableListeners = false

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        style = SearchInputViewStyle(context, attrs)

        binding.root.setOnClickListener { binding.inputField.focusAndShowKeyboard() }
        binding.root.updateLayoutParams {
            this.height = style.searchInputHeight
        }

        binding.clearInputButton.setImageDrawable(style.clearInputDrawable)
        binding.searchIcon.setImageDrawable(style.searchIconDrawable)
        binding.searchIcon.updateLayoutParams<MarginLayoutParams> {
            width = style.searchIconWidth
            height = style.searchIconHeight
            marginStart = style.searchIconMarginStart
        }
        binding.clearInputButton.updateLayoutParams<MarginLayoutParams> {
            width = style.clearIconWidth
            height = style.clearIconHeight
            marginEnd = style.clearIconMarginEnd
        }
        binding.inputField.hint = style.hintText
        binding.inputField.setHintTextColor(style.hintColor)
        binding.inputField.setTextColor(style.textColor)
        style.backgroundDrawable.also {
            val outline = style.backgroundDrawableOutline
            if (it is GradientDrawable && outline != null) {
                it.setStroke(outline.width, outline.color)
            }
        }
        binding.root.background = style.backgroundDrawable
        binding.inputField.setTextSizePx(style.textSize.toFloat())
        binding.inputField.updateLayoutParams<MarginLayoutParams> {
            marginStart = style.textMarginStart
            marginEnd = style.textMarginEnd
        }

        binding.inputField.doAfterTextChanged { newText ->
            updateClearButtonVisibility(newText)

            if (disableListeners) return@doAfterTextChanged

            val newQuery = query
            continuousInputChangedListener?.onInputChanged(newQuery)
            inputDebouncer.submit {
                debouncedInputChangedListener?.onInputChanged(newQuery)
            }
        }

        binding.inputField.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    searchStartedListener?.onSearchStarted(query)
                    true
                }
                else -> false
            }
        }

        binding.clearInputButton.setOnClickListener {
            clear()
        }

        updateClearButtonVisibility(query)
    }

    private fun updateClearButtonVisibility(text: CharSequence?) {
        val isClearButtonVisible = !text.isNullOrEmpty()
        if (isClearButtonVisible && !binding.clearInputButton.isVisible) {
            TransitionManager.beginDelayedTransition(binding.root, Fade().setDuration(FADE_DURATION))
        }
        binding.clearInputButton.isVisible = isClearButtonVisible
    }

    /**
     * Used to reach to detach from window events. Cancels any current input values that are debounced.
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        inputDebouncer.shutdown()
    }

    /**
     * Updates the current input to the specified string.
     *
     * Note that listeners are notified of this change.
     */
    public fun setQuery(query: String) {
        binding.inputField.setText(query.trim())
    }

    /**
     * Clears the current input.
     *
     * @return If there was an existing input that was cleared, false if it was already empty.
     */
    public fun clear(): Boolean {
        if (query.isEmpty()) {
            return false
        }

        withoutListenerNotifications {
            binding.inputField.setText("")

            // Notify both listeners instantly, manually
            continuousInputChangedListener?.onInputChanged("")
            debouncedInputChangedListener?.onInputChanged("")
        }

        return true
    }

    /**
     * Performs the given [actions] without notifying listeners about the changes
     * via the [TextWatcher] on the input field.
     */
    private inline fun withoutListenerNotifications(actions: () -> Unit) {
        disableListeners = true
        actions()
        disableListeners = false
    }

    /**
     * Sets a listener for continuous input events. This listener is notified of each individual
     * character change in the input as it changes.
     */
    public fun setContinuousInputChangedListener(inputChangedListener: InputChangedListener?) {
        this.continuousInputChangedListener = inputChangedListener
    }

    /**
     * Sets a listener for debounced input events. Quick changes to the input will not be passed to
     * this listener, it will only be invoked when the input has been stable for a short while.
     */
    public fun setDebouncedInputChangedListener(inputChangedListener: InputChangedListener?) {
        this.debouncedInputChangedListener = inputChangedListener
    }

    /**
     * Sets the listener to be called when search is triggered.
     */
    public fun setSearchStartedListener(searchStartedListener: SearchStartedListener?) {
        this.searchStartedListener = searchStartedListener
    }

    /**
     * Listener that exposes a handle when the input changes.
     */
    public fun interface InputChangedListener {
        /**
         * Handle when the input changes.
         *
         * @param query The current query value.
         */
        public fun onInputChanged(query: String)
    }

    /**
     * Listener that exposes a handle when the search starts.
     */
    public fun interface SearchStartedListener {

        /**
         * Handle when the search starts.
         *
         * @param query The current value of the query with which the search started.
         */
        public fun onSearchStarted(query: String)
    }
}
