package database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

const val MAX_USERNAME_LENGTH = 256
const val MAX_EMAIL_LENGTH = 256
const val MAX_PASSWORD_LENGTH = 64

object Users : Table() {
    val uid: Column<Int> = integer("uid").autoIncrement()
    val username = varchar("username", MAX_USERNAME_LENGTH)
    val email = varchar("email", MAX_EMAIL_LENGTH).uniqueIndex()
    val password = varchar("password", MAX_PASSWORD_LENGTH)
}
