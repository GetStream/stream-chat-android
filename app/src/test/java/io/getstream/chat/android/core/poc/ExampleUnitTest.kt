package io.getstream.chat.android.core.poc

import com.google.gson.Gson
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val gson = Gson()

        val query = ChannelsQuery().apply {
            sort[Field.cid] = "111"
            offset = 10
            limit = 10
            message_limit = 10
            filter_conditions[Field.cid] = "666"
            filter_conditions[Field.user_id] = "777"
            filter_conditions[Field.user_id] = mutableMapOf<Field, String>().apply {
                put(Field.user_id, "11")
            }
        }

        println(gson.toJson(query))
    }
}
