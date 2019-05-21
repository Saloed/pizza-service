package ru.spbstu.architectures.pizzaService.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import ru.spbstu.architectures.DbConfig

object Db {
    private var initialized = false
//    private val pool = ComboPooledDataSource()
    private lateinit var myDatabase: Database

    val database: Database
        get() = if (initialized) myDatabase else throw IllegalStateException("Database is not initialized")

    fun init(dbConfig: DbConfig) {
        if (initialized) return
        val url = "jdbc:postgresql://${dbConfig.host}:5432/${dbConfig.name}"
//        pool.apply {
//            driverClass = "org.postgresql.Driver"
//            jdbcUrl = url
//            user = dbConfig.user
//            password = dbConfig.password
//        }

//        myDatabase = Database.connect(pool)
        myDatabase = Database.connect(url, "org.postgresql.Driver", dbConfig.user, dbConfig.password)
        initialized = true
    }

    fun close() {
        if (!initialized) return
//        pool.close()
        initialized = false
    }

    fun <T> transaction(statement: Transaction.() -> T): T {
        return transaction(database, statement)
    }
}