package io.getstream.chat.android.core.poc.app.common

import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.app.App
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_channels.*


class ChannelsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels)

        val coroutinesFragment =
            io.getstream.chat.android.core.poc.app.examples.coroutines.ChannelsListFragment()

        val liveDataFragment =
            io.getstream.chat.android.core.poc.app.examples.livedata.ChannelsListFragment()

        val rxDataFragment =
            io.getstream.chat.android.core.poc.app.examples.rx.ChannelsListFragment()

        val fragment = rxDataFragment

        btnReloadView.setOnClickListener {
            fragment.reload()
        }


        App.db.channels()
            .getPageRx(0, 5)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Toast.makeText(this, "page:" + it.size, Toast.LENGTH_SHORT).show()
            }

        btnRemoveChannel.setOnClickListener {
            showRemoveChannel()
        }

        btnClearDb.setOnClickListener {


            App.db.channels()
                .deleteAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    //Toast.makeText(this, "Cleared", Toast.LENGTH_SHORT).show()
                }
        }

        if (savedInstanceState == null) {

            supportFragmentManager.beginTransaction()
                .add(R.id.root, fragment)
                .commit()
        }
    }

    private fun showRemoveChannel() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Delete with id")

        val input = EditText(this)
        //input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, which ->
                val id = input.text.toString()

                if (id.isEmpty()) {

                } else {

                    if (id.contains(",")) {
                        DbUtils.removeSet(id, this)
                    } else if (id.contains("-")) {
                        DbUtils.removeRange(id, this)
                    } else {
                        DbUtils.removeChannel(id, this)
                    }

                }


            })
        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }


}
