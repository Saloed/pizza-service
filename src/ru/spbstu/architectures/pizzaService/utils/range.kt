package ru.spbstu.architectures.pizzaService.utils

import kotlin.math.max
import kotlin.math.min

fun IntRange.intersect(other: IntRange): IntRange {
    val from = listOf(max(first, other.first), endInclusive, other.endInclusive).min()!!
    val to = listOf(min(endInclusive, other.endInclusive), first, other.first).max()!!
    return IntRange(from, to)
}

fun IntRange.toLong() = LongRange(first.toLong(), endInclusive.toLong())
