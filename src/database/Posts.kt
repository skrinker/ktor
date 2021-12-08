package database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

const val MAX_POST_LENGTH = 1024

object Posts : Table() {
    val postId: Column<Int> = integer("task_id").autoIncrement()
    val uid: Column<Int> = integer("uid").references(Users.uid)
    val text = varchar("text", MAX_POST_LENGTH)
}
