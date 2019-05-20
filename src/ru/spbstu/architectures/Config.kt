package ru.spbstu.architectures

import io.ktor.util.hex
import java.io.File
import java.util.*


data class DbConfig(val host: String, val name: String, val user: String, val password: String)

interface ConfigurationFacade {
    val secretKey: ByteArray
    val db: DbConfig
}

object ConfigurationFacadeDummy : ConfigurationFacade {
    override val secretKey = hex("6819b57a326945c1968f45236589")
    override val db: DbConfig by lazy {
        val dbProperties = Properties()
        dbProperties.load(File("resources/db.properties").inputStream())
        val host = dbProperties.getProperty("db.host")
        val name = dbProperties.getProperty("db.name")
        val user = dbProperties.getProperty("db.user")
        val password = dbProperties.getProperty("db.password")
        DbConfig(host, name, user, password)
    }
}