package ru.spbstu.architectures.pizzaService.db.manager

import org.jetbrains.exposed.sql.*
import ru.spbstu.architectures.pizzaService.db.Db
import ru.spbstu.architectures.pizzaService.db.table.ClientTable
import ru.spbstu.architectures.pizzaService.db.table.UserTable
import ru.spbstu.architectures.pizzaService.models.Client
import ru.spbstu.architectures.pizzaService.models.ModelManager
import ru.spbstu.architectures.pizzaService.models.UserRoleType
import ru.spbstu.architectures.pizzaService.utils.intColumn

object ClientModelManager : ModelManager<Client> {

    override suspend fun update(model: Client): Client {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun list(where: SqlExpressionBuilder.() -> Op<Boolean>) = Db.transaction {
        (ClientTable innerJoin UserTable)
            .select(where)
            .map { it.buildClient() }
    }

    fun ResultRow.buildClient() = Client(
        this[UserTable.id],
        this[UserTable.login],
        this[UserTable.password],
        this[ClientTable.address],
        this[ClientTable.phone]
    )

    override suspend fun get(id: Int) = list {
        ClientTable.id.eq(id)
    }.singleOrNull()

    override suspend fun create(model: Client) = Db.transaction {
        val userId = UserModelManager.create(
            model.login,
            model.password,
            UserRoleType.Client
        )
        ClientTable.insert {
            it[ClientTable.id] = userId
            it[ClientTable.address] = model.address
            it[ClientTable.phone] = model.phone
        }
    }.let { Client(it[ClientTable.id], model.login, model.password, model.address, model.phone) }


}

suspend fun ModelManager<Client>.orders(client: Client) =
    OrderModelManager.list {
        intColumn("order", "client_id").eq(client.id)
    }

suspend fun ModelManager<Client>.getForIds(ids: List<Int>) = list {
    ClientTable.id inList ids
}
