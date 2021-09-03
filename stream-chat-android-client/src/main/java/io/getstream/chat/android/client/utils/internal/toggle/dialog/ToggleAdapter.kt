package io.getstream.chat.android.client.utils.internal.toggle.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import io.getstream.chat.android.client.R

internal class ToggleAdapter(context: Context) :
    ArrayAdapter<Pair<String, Boolean>>(context, R.layout.stream_toggle_list_item, emptyList()) {

    var listener = ToggleSwitchListener { _, _ -> }
    private var data: List<Pair<String, Boolean>> = emptyList()

    fun addData(list: List<Pair<String, Boolean>>) {
        data = list
        notifyDataSetChanged()
    }

    override fun getCount() = data.size

    override fun getItem(position: Int): Pair<String, Boolean> = data[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView =
            convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.stream_toggle_list_item, parent, false)
        val label: TextView = itemView.findViewById(R.id.label)
        val switch: SwitchCompat = itemView.findViewById(R.id.switcher)
        val toggle = getItem(position)
        label.text = toggle.first
        switch.isChecked = toggle.second
        switch.setOnCheckedChangeListener { _, isChecked -> listener.onSwitched(toggle.first, isChecked) }

        return itemView
    }
}

internal fun interface ToggleSwitchListener {
    fun onSwitched(toggleName: String, isEnabled: Boolean)
}
