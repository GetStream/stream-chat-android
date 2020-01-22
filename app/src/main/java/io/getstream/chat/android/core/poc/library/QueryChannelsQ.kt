package io.getstream.chat.android.core.poc.library

import com.google.gson.Gson
import io.getstream.chat.android.core.poc.library.json.ChatGson
import io.getstream.chat.android.core.poc.library.requests.QuerySort
import io.getstream.chat.android.core.poc.library.utils.UndefinedDate
import java.security.MessageDigest
import java.util.*
import kotlin.collections.HashMap


class QueryChannelsQ(val filter: FilterObject, val sort: QuerySort) {

    val TAG = QueryChannelsQ::class.java.simpleName
    var id: String = ""
    var channelCIDs = mutableListOf<String>()
    var createdAt: Date = UndefinedDate
    var updatedAt: Date  = UndefinedDate

    private fun computeID() {
        val data: MutableMap<String, Any?> = HashMap()
        data["sort"] = sort.data
        data["filter"] = filter.getData()
        val gson = ChatGson.instance
        val json = gson.toJson(data)
        val MD5 = "MD5"
        try { // Create MD5 Hash
            val digest = MessageDigest
                .getInstance(MD5)
            digest.update(json.toByteArray())
            val messageDigest = digest.digest()
            // Create Hex String
            val hexString = StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            id = hexString.toString()
        } catch (e: Throwable) {
            //StreamChat.getLogger().logT(this, e)
            id = "errorCreatingQueryID"
        }
    }

//    fun getFilter(): FilterObject {
//        return filter
//    }
//
//    fun setFilter(filter: FilterObject) {
//        this.filter = filter
//        computeID()
//    }
//
//    fun getSort(): QuerySort {
//        return sort
//    }
//
//    fun setSort(sort: QuerySort) {
//        this.sort = sort
//        computeID()
//    }

    init {
        computeID()
    }
}
