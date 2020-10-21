package io.getstream.chat.android.client.sample.common

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.R
import io.getstream.chat.android.client.utils.observable.Disposable

class ChannelsListActivity : AppCompatActivity() {

    val client = App.client
    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels)

        disposable = client.subscribe {

//            if (it is ErrorEvent) {
//
//            } else if (it is ConnectedEvent) {
//                client.queryChannels(
//                    QueryChannelsRequest(
//                        FilterObject(),
//                        0,
//                        1
//                    )
//                ).enqueue { channels ->
//                    if (channels.isSuccess) {
//
//                    } else {
//
//                    }
//                }
//            }
        }

        val token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"

        client.setUser(User("bender"), token)

        client.subscribe {
            Log.d("chat-events", it.toString())
        }

//        val coroutinesFragment =
//            io.getstream.chat.android.client.sample.examples.coroutines.ChannelsListFragment()
//
//        val liveDataFragment =
//            io.getstream.chat.android.client.sample.examples.livedata.ChannelsListFragment()
//
//        val rxDataFragment =
//            io.getstream.chat.android.client.sample.examples.rx.ChannelsListFragment()
//
//        val fragment = rxDataFragment
//
//        btnReloadView.setOnClickListener {
//            fragment.reload()
//        }
//
//
//        App.db.channels()
//            .getPageRx(0, 5)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe {
//                Toast.makeText(this, "page:" + it.size, Toast.LENGTH_SHORT).show()
//            }
//
//        btnRemoveChannel.setOnClickListener {
//            showRemoveChannel()
//        }
//
//        btnClearDb.setOnClickListener {
//
//
//            App.db.channels()
//                .deleteAll()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {
//                    //Toast.makeText(this, "Cleared", Toast.LENGTH_SHORT).show()
//                }
//        }
//
//        if (savedInstanceState == null) {
//
//            supportFragmentManager.beginTransaction()
//                .add(R.id.root, fragment)
//                .commit()
//        }
    }

    override fun onDestroy() {
        disposable?.dispose()
        client.disconnect()
        super.onDestroy()
    }

    private fun showRemoveChannel() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Delete with id")

        val input = EditText(this)
        // input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val id = input.text.toString()

            if (id.isNotEmpty()) {
                if (id.contains(",")) {
                    DbUtils.removeSet(id, this)
                } else if (id.contains("-")) {
                    DbUtils.removeRange(id, this)
                } else {
                    DbUtils.removeChannel(id, this)
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }
}
