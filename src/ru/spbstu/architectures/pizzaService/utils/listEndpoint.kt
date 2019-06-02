package ru.spbstu.architectures.pizzaService.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.response.contentRange
import io.ktor.response.respond
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.memberProperties

enum class SortOrder {
    ASC, DESC
}

data class Sort(val field: String, val order: SortOrder)

data class ListQueryParams<T>(val filter: T?, val range: IntRange?, val sort: List<Sort>?)

inline fun <reified T> ApplicationCall.getListQueryParams(): ListQueryParams<T> {
    val params = request.queryParameters
    val gson = Gson()
    val stringListType = TypeToken.getParameterized(List::class.java, String::class.java).type
    val intListType = TypeToken.getParameterized(List::class.java, Integer::class.java).type
    val filter = params["filter"]?.let { gson.fromJson<T>(it, T::class.java) }
    val range = params["range"]?.let { gson.fromJson<List<Int>>(it, intListType) }
        ?.let { (from, to) -> IntRange(from, to) }
    val sort = params["sort"]?.let { gson.fromJson<List<String>>(it, stringListType) }
        ?.chunked(2) { (field, order) ->
            Sort(field, SortOrder.valueOf(order))
        }

    return ListQueryParams(filter, range, sort)
}

suspend inline fun <reified T> ApplicationCall.responseListRange(data: List<T>, range: IntRange?) = when {
    data.isEmpty() -> {
        response.contentRange(0..0L, 0)
        respond(data)
    }
    range == null -> {
        response.contentRange(data.indices.toLong(), data.size.toLong())
        respond(data)
    }
    else -> {
        val realRange = range.intersect(data.indices)
        response.contentRange(realRange.toLong(), data.size.toLong())
        val result = data.subList(realRange.first, realRange.endInclusive + 1)
        respond(result)
    }
}
//
//inline fun <reified T : Any> Sort.buildComparator(): Comparator<T> {
//    val property = T::class.memberProperties.find { it.name == field }
//        ?: throw IllegalArgumentException("Unknown sort key: $field")
//
//}
//
//inline fun <reified T : Any> ApplicationCall.responseListSort(data: List<T>, sort: Sort): List<T> {
//    val comparator = sort.buildComparator<T>()
//    return data.sortedWith(comparator)
//}
//
//inline fun <reified T : Any> ApplicationCall.responseListSort(data: List<T>, sorters: List<Sort>?): List<T> {
//    if (sorters == null || sorters.isEmpty()) return data
//    var newData = data
//    for (sort in sorters) {
//        newData = responseListSort(newData, sort)
//    }
//    return newData
//}
