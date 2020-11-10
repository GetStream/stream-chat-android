package io.getstream.chat.android.ui.textinput

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

internal class TextInputHandler() {

    fun bindEditText(editText: EditText, containsTextListener: () -> Unit = {}, emptyTextListener: () -> Unit = {}) {
        object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (charSequence?.length == 0) {
                    emptyTextListener()
                } else {
                    containsTextListener()
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        }.let(editText::addTextChangedListener)
    }
}
