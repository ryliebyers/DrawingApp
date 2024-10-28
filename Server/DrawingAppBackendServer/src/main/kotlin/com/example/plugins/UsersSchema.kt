package com.example.plugins

import com.example.ExposedUser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserService(private val database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", 50)
        val age = integer("age")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    fun create(user: ExposedUser): Int = transaction {
        Users.insert {
            it[name] = user.name
            it[age] = user.age
        }[Users.id]
    }

    fun read(id: Int): ExposedUser? = transaction {
        Users.select { Users.id eq id }
            .map {
                ExposedUser(
                    id = it[Users.id],
                    name = it[Users.name],
                    age = it[Users.age]
                )
            }
            .singleOrNull()
    }

    fun update(id: Int, user: ExposedUser) = transaction {
        Users.update({ Users.id eq id }) {
            it[name] = user.name
            it[age] = user.age
        }
    }

    fun delete(id: Int) = transaction {
        Users.deleteWhere { Users.id eq id }
    }
}
