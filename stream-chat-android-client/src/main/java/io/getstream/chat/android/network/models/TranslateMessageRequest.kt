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
internal data class TranslateMessageRequest(
    @Json(name = "language")
    internal val language: Language,
) {

    /**
     * Language Enum
     */
    internal sealed class Language(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): Language = when (s) {
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
        internal object Af : Language("af")
        internal object Am : Language("am")
        internal object Ar : Language("ar")
        internal object Az : Language("az")
        internal object Bg : Language("bg")
        internal object Bn : Language("bn")
        internal object Bs : Language("bs")
        internal object Cs : Language("cs")
        internal object Da : Language("da")
        internal object De : Language("de")
        internal object El : Language("el")
        internal object En : Language("en")
        internal object Es : Language("es")
        internal object EsMX : Language("es-MX")
        internal object Et : Language("et")
        internal object Fa : Language("fa")
        internal object FaAF : Language("fa-AF")
        internal object Fi : Language("fi")
        internal object Fr : Language("fr")
        internal object FrCA : Language("fr-CA")
        internal object Ha : Language("ha")
        internal object He : Language("he")
        internal object Hi : Language("hi")
        internal object Hr : Language("hr")
        internal object Ht : Language("ht")
        internal object Hu : Language("hu")
        internal object Id : Language("id")
        internal object It : Language("it")
        internal object Ja : Language("ja")
        internal object Ka : Language("ka")
        internal object Ko : Language("ko")
        internal object Lt : Language("lt")
        internal object Lv : Language("lv")
        internal object Ms : Language("ms")
        internal object Nl : Language("nl")
        internal object No : Language("no")
        internal object Pl : Language("pl")
        internal object Ps : Language("ps")
        internal object Pt : Language("pt")
        internal object Ro : Language("ro")
        internal object Ru : Language("ru")
        internal object Sk : Language("sk")
        internal object Sl : Language("sl")
        internal object So : Language("so")
        internal object Sq : Language("sq")
        internal object Sr : Language("sr")
        internal object Sv : Language("sv")
        internal object Sw : Language("sw")
        internal object Ta : Language("ta")
        internal object Th : Language("th")
        internal object Tl : Language("tl")
        internal object Tr : Language("tr")
        internal object Uk : Language("uk")
        internal object Ur : Language("ur")
        internal object Vi : Language("vi")
        internal object Zh : Language("zh")
        internal object ZhTW : Language("zh-TW")
        internal data class Unknown(val unknownValue: String) : Language(unknownValue)

        internal class LanguageAdapter : JsonAdapter<Language>() {
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
