package repos

import database.DatabaseFactory.dbQuery
import database.Users
import models.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.update

class UserRepoImpl : UserRepo {
    override suspend fun storeUser(username: String, email: String, about: String, password: String): User? {
        var statement: InsertStatement<Number>? = null
        dbQuery {
            statement = Users.insert { user ->
                user[Users.username] = username
                user[Users.email] = email
                user[Users.about] = about
                user[Users.password] = password
            }
        }
        return rowToUser(statement?.resultedValues?.get(0))
    }
    private fun rowToUser(row: ResultRow?): User? {
        if (row == null) {
            return null
        }
        return User(
            uid = row[Users.uid],
            email = row[Users.email],
            username = row[Users.username],
            about = row[Users.about],
            password = row[Users.password]
        )
    }

    override suspend fun getUser(uid: Int): User? = dbQuery {
        Users.select { Users.uid.eq(uid) }.map { rowToUser(it) }.singleOrNull()
    }

    override suspend fun getUserByEmail(email: String): User? = dbQuery {
        Users.select { Users.email.eq(email) }
            .map { rowToUser(it) }.singleOrNull()
    }

    override suspend fun updateAbout(uid: Int, newAbout: String): Int = dbQuery {
        Users.update({ Users.uid.eq(uid) }) {
            it[about] = newAbout
        }
    }
}
