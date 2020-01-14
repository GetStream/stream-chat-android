package io.getstream.chat.android.core.poc.library.api


class ApiClientOptions constructor(

) {
    var baseURL: String = ""
    var cdnURL: String = ""
    var timeout: Int = 0
    var cdntimeout: Int = 0



    val httpURL: String
        get() = "https://$baseURL/"

    val cdnHttpURL: String
        get() = "https://$cdnURL/"

    val wssURL: String
        get() = "wss://$baseURL/"

    class Builder {

        private val options = ApiClientOptions()

        fun Timeout(timeout: Int): Builder {
            options.timeout = timeout
            return this
        }

        fun CDNTimeout(timeout: Int): Builder {
            options.cdntimeout = timeout
            return this
        }

        fun BaseURL(baseURL: String?): Builder {
            var baseURL = baseURL
            if (baseURL != null && baseURL.startsWith("https://")) {
                baseURL = baseURL.split("https://").toTypedArray()[1]
            }
            if (baseURL != null && baseURL.startsWith("http://")) {
                baseURL = baseURL.split("http://").toTypedArray()[1]
            }
            if (baseURL!!.endsWith("/")) {
                baseURL = baseURL.substring(0, baseURL.length - 1)
            }
            options.baseURL = baseURL
            return this
        }

        fun CDNURL(cdnURL: String?): Builder {
            var cdnURL = cdnURL
            if (cdnURL != null && cdnURL.startsWith("https://")) {
                cdnURL = cdnURL.split("https://").toTypedArray()[1]
            }
            if (cdnURL != null && cdnURL.startsWith("http://")) {
                cdnURL = cdnURL.split("http://").toTypedArray()[1]
            }
            if (cdnURL!!.endsWith("/")) {
                cdnURL = cdnURL.substring(0, cdnURL.length - 1)
            }
            options.cdnURL = cdnURL
            return this
        }

        fun build(): ApiClientOptions {
            return options
        }
    }
}