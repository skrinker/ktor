package dev.hashnode.danielwaiguru.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Posts: Table() {
    val postId: Column<Int> = integer("task_id").autoIncrement()
    val uid: Column<Int> = integer("uid").references(Users.uid)
    val text = varchar("text", 1024)
}