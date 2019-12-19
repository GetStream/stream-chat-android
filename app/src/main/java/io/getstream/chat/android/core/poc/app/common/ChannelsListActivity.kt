package io.getstream.chat.android.core.poc.app.common

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.app.App
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class ChannelsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val coroutinesFragment =
            io.getstream.chat.android.core.poc.app.coroutines.ChannelsListFragment()

        val liveDataFragment =
            io.getstream.chat.android.core.poc.app.livedata.ChannelsListFragment()

        val rxDataFragment =
            io.getstream.chat.android.core.poc.app.rx.ChannelsListFragment()

        val fragment = rxDataFragment

        btnReloadView.setOnClickListener {
            fragment.reload()
        }

        btnClearDb.setOnClickListener {
            App.db.channels()
                .deleteAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toast.makeText(this, "Cleared", Toast.LENGTH_SHORT)
                }
        }

        if (savedInstanceState == null) {

            supportFragmentManager.beginTransaction()
                .add(R.id.root, fragment)
                .commit()
        }
    }
}
