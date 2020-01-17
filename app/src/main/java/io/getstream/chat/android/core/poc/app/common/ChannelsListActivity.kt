package io.getstream.chat.android.core.poc.app.common

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.library.FilterObject
import io.getstream.chat.android.core.poc.library.QueryChannelsRequest
import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.requests.QuerySort
import kotlinx.android.synthetic.main.activity_channels.*


class ChannelsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels)

        val client = App.client

        client.setUser(User("bender"), object : TokenProvider {
            override fun getToken(listener: TokenProvider.TokenProviderListener) {
                listener.onSuccess("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ")
            }
        }).enqueue {
            if (it.isSuccess()) {

                val subscribtion = client.events().subscribe {
                    Log.d("chat-events", it.toString())
                }

                root.postDelayed({
                    subscribtion.unsubscribe()
                    Log.d("chat-events", "unsubscribed")
                }, 10000)


                client.queryChannels(
                    QueryChannelsRequest(
                        FilterObject(),
                        QuerySort()
                    ).withLimit(1)
                ).enqueue {

                    if (it.isSuccess()) {

                    } else {

                    }
                }
            } else {

            }
        }

//        val coroutinesFragment =
//            io.getstream.chat.android.core.poc.app.examples.coroutines.ChannelsListFragment()
//
//        val liveDataFragment =
//            io.getstream.chat.android.core.poc.app.examples.livedata.ChannelsListFragment()
//
//        val rxDataFragment =
//            io.getstream.chat.android.core.poc.app.examples.rx.ChannelsListFragment()
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
