package io.getstream.chat.android.client.sample.common

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.utils.observable.Subscription
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.R


class ChannelsListActivity : AppCompatActivity() {

    val client = App.client
    var sub: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels)



        sub = client.events().subscribe {

            if (it is ErrorEvent) {

            } else if (it is ConnectedEvent) {
                client.queryChannels(
                    QueryChannelsRequest(
                        0,
                        1,
                        FilterObject(),
                        QuerySort()
                    )
                ).enqueue { channels ->
                    if (channels.isSuccess) {

                    } else {

                    }
                }
            }
        }

        client.setUser(User("bender"))

        client.events().subscribe {
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
        sub?.unsubscribe()
        client.disconnect()
        super.onDestroy()
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
