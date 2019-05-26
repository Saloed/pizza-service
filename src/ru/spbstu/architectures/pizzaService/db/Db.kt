package ru.spbstu.architectures.pizzaService.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import ru.spbstu.architectures.DbConfig

object Db {
    private var initialized = false
    private lateinit var myDatabase: Database

    val database: Database
        get() = if (initialized) myDatabase else throw IllegalStateException("Database is not initialized")

    fun init(dbConfig: DbConfig) {
        if (initialized) return
        val hikariConfig = configHikariPool(dbConfig)
        myDatabase = Database.connect(hikariConfig)
//        val url = "jdbc:postgresql://${dbConfig.host}:5432/${dbConfig.name}"
//        myDatabase = Database.connect(url, "org.postgresql.Driver", dbConfig.user, dbConfig.password)
        initialized = true
    }

    private fun configHikariPool(dbConfig: DbConfig): HikariDataSource {
        val url = "jdbc:postgresql://${dbConfig.host}:5432/${dbConfig.name}"
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = url
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.username = dbConfig.user
        config.password = dbConfig.password
        config.validate()
        return HikariDataSource(config)
    }

    fun close() {
        if (!initialized) return
        initialized = false
    }

    suspend fun <T> transaction(statement: Transaction.() -> T): T = withContext(Dispatchers.IO) {
        transaction(database, statement)
    }
}