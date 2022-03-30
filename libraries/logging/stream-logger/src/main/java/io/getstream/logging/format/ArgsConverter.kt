package io.getstream.logging.format

public object ArgsConverter {

    private const val SPACE = " "

    public fun convertArgs(vararg args: Any?): Array<Any?> = args.map { arg ->
        when (arg) {
            is ByteArray -> arg.toHex()
            is Array<*> -> arg.joinToString()
            else -> arg
        }
    }.toTypedArray()

    private fun ByteArray.toHex() = joinToString(SPACE) { "%02X".format(it) }

    private fun Array<*>.joinToString() = joinToString(SPACE) { it.toString() }
}
