package io.getstream.chat.android.ui.search

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.getstream.sdk.chat.utils.extensions.focusAndShowKeyboard
import io.getstream.chat.android.ui.databinding.StreamUiSearchViewBinding
import io.getstream.chat.android.ui.utils.Debouncer

public class SearchInputView : FrameLayout {

    private companion object {
        private const val TYPING_DEBOUNCE_MS = 300L
        private const val FADE_DURATION = 300L
    }

    private val binding: StreamUiSearchViewBinding =
        StreamUiSearchViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var debouncedInputChangedListener: InputChangedListener? = null
    private var continuousInputChangedListener: InputChangedListener? = null
    private var searchStartedListener: SearchStartedListener? = null

    private val inputDebouncer = Debouncer(debounceMs = TYPING_DEBOUNCE_MS)

    private val query: String
        get() = binding.inputField.text.trim().toString()

    private var disableListeners = false

    public constructor(context: Context) : super(context) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        parseAttrs(attrs)

        binding.root.setOnClickListener { binding.inputField.focusAndShowKeyboard() }
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

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
    }

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

    public fun interface InputChangedListener {
        public fun onInputChanged(query: String)
    }

    public fun interface SearchStartedListener {
        public fun onSearchStarted(query: String)
    }
}
