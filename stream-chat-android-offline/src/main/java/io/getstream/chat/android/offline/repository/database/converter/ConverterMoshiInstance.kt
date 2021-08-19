package io.getstream.chat.android.offline.repository.database.converter

import com.squareup.moshi.Moshi
import com.squareup.moshi.addAdapter
import io.getstream.chat.android.client.parser2.adapters.DateAdapter

@OptIn(ExperimentalStdlibApi::class)
internal val moshi: Moshi = Moshi.Builder()
    .addAdapter(DateAdapter())
    .build()
