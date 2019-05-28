package ru.spbstu.architectures.pizzaService.external

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import ru.spbstu.architectures.pizzaService.db.Db

data class CachedResourceStr(val updatedAt: DateTime, val resource: String)

object ApiCacheTable : Table() {
    val resource = varchar("resource", 255).primaryKey()
    val updatedAt = datetime("updated_at")
    val data = text("data")
}

object ExternalApiDbCache {
    suspend inline fun <reified T : Any> get(resource: String, onExpired: () -> List<T>): List<T> {
        val cachedData = Db.transaction {
            ApiCacheTable.select { ApiCacheTable.resource eq resource }
                .map { CachedResourceStr(it[ApiCacheTable.updatedAt], it[ApiCacheTable.data]) }
                .singleOrNull()
        }
        val gson = Gson()
        if (cachedData == null || cachedData.updatedAt.plusDays(2) < DateTime.now()) {
            val newData = onExpired()
            val dataStr = gson.toJson(newData)
            if (cachedData == null) {
                Db.transaction {
                    ApiCacheTable.insert {
                        it[updatedAt] = DateTime.now()
                        it[data] = dataStr
                        it[ApiCacheTable.resource] = resource
                    }
                }
            } else {
                Db.transaction {
                    ApiCacheTable.update {
                        it[updatedAt] = DateTime.now()
                        it[data] = dataStr
                    }
                }
            }
            return newData
        }
        val resultListType = TypeToken.getParameterized(List::class.java, T::class.java).type
        return gson.fromJson(cachedData.resource, resultListType)
    }
}
