/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 *
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class TranslateMessageRequest(
    @Json(name = "language")
    val language: Language,
) {

    /**
     * Language Enum
     */
    sealed class Language(val value: kotlin.String) {
        override fun toString(): String = value

        companion object {
            fun fromString(s: kotlin.String): Language = when (s) {
                "af" -> Af
                "am" -> Am
                "ar" -> Ar
                "az" -> Az
                "bg" -> Bg
                "bn" -> Bn
                "bs" -> Bs
                "cs" -> Cs
                "da" -> Da
                "de" -> De
                "el" -> El
                "en" -> En
                "es" -> Es
                "es-MX" -> EsMX
                "et" -> Et
                "fa" -> Fa
                "fa-AF" -> FaAF
                "fi" -> Fi
                "fr" -> Fr
                "fr-CA" -> FrCA
                "ha" -> Ha
                "he" -> He
                "hi" -> Hi
                "hr" -> Hr
                "ht" -> Ht
                "hu" -> Hu
                "id" -> Id
                "it" -> It
                "ja" -> Ja
                "ka" -> Ka
                "ko" -> Ko
                "lt" -> Lt
                "lv" -> Lv
                "ms" -> Ms
                "nl" -> Nl
                "no" -> No
                "pl" -> Pl
                "ps" -> Ps
                "pt" -> Pt
                "ro" -> Ro
                "ru" -> Ru
                "sk" -> Sk
                "sl" -> Sl
                "so" -> So
                "sq" -> Sq
                "sr" -> Sr
                "sv" -> Sv
                "sw" -> Sw
                "ta" -> Ta
                "th" -> Th
                "tl" -> Tl
                "tr" -> Tr
                "uk" -> Uk
                "ur" -> Ur
                "vi" -> Vi
                "zh" -> Zh
                "zh-TW" -> ZhTW
                else -> Unknown(s)
            }
        }
        object Af : Language("af")
        object Am : Language("am")
        object Ar : Language("ar")
        object Az : Language("az")
        object Bg : Language("bg")
        object Bn : Language("bn")
        object Bs : Language("bs")
        object Cs : Language("cs")
        object Da : Language("da")
        object De : Language("de")
        object El : Language("el")
        object En : Language("en")
        object Es : Language("es")
        object EsMX : Language("es-MX")
        object Et : Language("et")
        object Fa : Language("fa")
        object FaAF : Language("fa-AF")
        object Fi : Language("fi")
        object Fr : Language("fr")
        object FrCA : Language("fr-CA")
        object Ha : Language("ha")
        object He : Language("he")
        object Hi : Language("hi")
        object Hr : Language("hr")
        object Ht : Language("ht")
        object Hu : Language("hu")
        object Id : Language("id")
        object It : Language("it")
        object Ja : Language("ja")
        object Ka : Language("ka")
        object Ko : Language("ko")
        object Lt : Language("lt")
        object Lv : Language("lv")
        object Ms : Language("ms")
        object Nl : Language("nl")
        object No : Language("no")
        object Pl : Language("pl")
        object Ps : Language("ps")
        object Pt : Language("pt")
        object Ro : Language("ro")
        object Ru : Language("ru")
        object Sk : Language("sk")
        object Sl : Language("sl")
        object So : Language("so")
        object Sq : Language("sq")
        object Sr : Language("sr")
        object Sv : Language("sv")
        object Sw : Language("sw")
        object Ta : Language("ta")
        object Th : Language("th")
        object Tl : Language("tl")
        object Tr : Language("tr")
        object Uk : Language("uk")
        object Ur : Language("ur")
        object Vi : Language("vi")
        object Zh : Language("zh")
        object ZhTW : Language("zh-TW")
        data class Unknown(val unknownValue: kotlin.String) : Language(unknownValue)

        class LanguageAdapter : JsonAdapter<Language>() {
            @FromJson
            override fun fromJson(reader: JsonReader): Language? {
                val s = reader.nextString() ?: return null
                return Language.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: Language?) {
                writer.value(value?.value)
            }
        }
    }
}
