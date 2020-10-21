package io.getstream.chat.android.client.sample.common

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.client.sample.App
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object DbUtils {
    fun removeChannel(id: String, activity: AppCompatActivity) {
        App.db.channels().delete(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Toast.makeText(activity, "Deleted count: $it", Toast.LENGTH_SHORT)
                        .show()
                },
                {
                    it.printStackTrace()
                    Toast.makeText(
                        activity,
                        "Not deleted: " + it.message,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            )
    }

    fun removeSet(
        set: String,
        channelsListActivity: ChannelsListActivity
    ) {
        val ids = set.split(",")
    }

    fun removeRange(
        range: String,
        activity: ChannelsListActivity
    ) {
        val from = range.split("-")[0]
        val to = range.split("-")[1]

        App.db.channels().delete(from, to)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Toast.makeText(activity, "Deleted count: $it", Toast.LENGTH_SHORT)
                        .show()
                },
                {
                    it.printStackTrace()
                    Toast.makeText(
                        activity,
                        "Not deleted: " + it.message,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            )
    }
}
