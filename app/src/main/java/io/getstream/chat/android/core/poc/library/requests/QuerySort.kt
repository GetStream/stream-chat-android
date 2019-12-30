package io.getstream.chat.android.core.poc.library.requests


class QuerySort {

    private val data = mutableListOf<Sort>()

    constructor(field: Field, direction: Direction) {
        data.add(Sort(field, direction))
    }

    constructor()


    fun add(field: Field, direction: Direction = Direction.ASC): QuerySort {
        data.add(Sort(field, direction))
        return this
    }

    data class Sort(val field: Field, val direction: Direction)
}
