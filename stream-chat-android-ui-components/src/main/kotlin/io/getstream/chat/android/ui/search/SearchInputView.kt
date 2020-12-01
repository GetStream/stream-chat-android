package io.getstream.chat.android.ui.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import io.getstream.chat.android.ui.databinding.StreamSearchViewBinding
import io.getstream.chat.android.ui.utils.Debouncer

public class SearchInputView : FrameLayout {

    private companion object {
        const val TYPING_DEBOUNCE_MS = 300L
    }

    private val binding: StreamSearchViewBinding =
        StreamSearchViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var debouncedInputChangedListener: InputChangedListener? = null
    private var continuousInputChangedListener: InputChangedListener? = null
    private var searchStartedListener: SearchStartedListener? = null

    private val inputDebouncer = Debouncer(debounceMs = TYPING_DEBOUNCE_MS)

    private val query: String
        get() = binding.inputField.text.trim().toString()

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

        binding.inputField.doAfterTextChanged { newText ->
            updateClearButtonVisibility(newText)
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
            binding.inputField.setText("")
        }

        updateClearButtonVisibility(query)
    }

    private fun updateClearButtonVisibility(text: CharSequence?) {
        binding.clearInputButton.isVisible = !text.isNullOrEmpty()
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
