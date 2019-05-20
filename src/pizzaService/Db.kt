package ru.spbstu.architectures.pizzaService

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import ru.spbstu.architectures.DbConfig
import java.lang.IllegalStateException
import java.sql.Driver

object Db {
    private var initialized = false
    private val pool = ComboPooledDataSource()
    private lateinit var myDatabase: Database

    val database: Database
        get() = if (initialized) myDatabase else throw IllegalStateException("Database is not initialized")

    fun init(dbConfig: DbConfig) {
        if (initialized) return
        pool.apply {
            driverClass = Driver::class.java.name
            jdbcUrl = "jdbc:postgresql://${dbConfig.host}:5432/${dbConfig.name}"
            user = dbConfig.user
            password = dbConfig.password
        }
        myDatabase = Database.connect(pool)
        initialized = true
    }

    fun close() {
        if (!initialized) return
        pool.close()
        initialized = false
    }

    fun <T> transaction(statement: Transaction.() -> T): T {
        return transaction(database, statement)
    }
}