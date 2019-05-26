package ru.spbstu.architectures.pizzaService.utils

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.joda.time.DateTime
import java.sql.ResultSet
import java.sql.Timestamp

fun <T : Any> String.execAndMap(transform: (ResultSet) -> T): List<T> {
    val result = arrayListOf<T>()
    TransactionManager.current().exec(this) { rs ->
        while (rs.next()) {
            result += transform(rs)
        }
    }
    return result
}

fun <T> SqlExpressionBuilder.column(table: String, name: String, type: IColumnType) =
    Column<T>(Table(table), name, type)


fun SqlExpressionBuilder.intColumn(table: String, name: String) = Column<Int>(Table(table), name, IntegerColumnType())
fun SqlExpressionBuilder.boolColumn(table: String, name: String) =
    Column<Boolean>(Table(table), name, BooleanColumnType())

fun SqlExpressionBuilder.stringColumn(table: String, name: String) =
    Column<String>(Table(table), name, VarCharColumnType())


fun (SqlExpressionBuilder.() -> Op<Boolean>).toSQL() = this(SqlExpressionBuilder).toSQL(QueryBuilder(false))

fun Timestamp.toDateTime() = DateTime(time)
fun DateTime.toTimestamp() = Timestamp(millis)
