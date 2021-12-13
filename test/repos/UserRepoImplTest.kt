import database.MAX_EMAIL_LENGTH
import database.MAX_PASSWORD_LENGTH
import database.MAX_POST_LENGTH
import database.MAX_USERNAME_LENGTH
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import models.User
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import repos.PostRepoImpl
import repos.UserRepoImpl
import javax.sql.DataSource
import kotlin.NullPointerException
import kotlin.test.assertEquals

class UserRepoImplTest {
    val userRepo = UserRepoImpl()

    @Test
    fun storeUserTest() {
        runBlocking {
            val testUser = userRepo.storeUser("a", "k", "c")
                ?: throw NullPointerException("Store user null")
            val databaseResult = transaction {
                Users.select { Users.uid.eq(testUser.uid) }.firstOrNull()
            } ?: throw NullPointerException("Database result null")
            assertEquals(testUser, User(
                uid = databaseResult[database.Users.uid],
                username = databaseResult[database.Users.username],
                email = databaseResult[database.Users.email],
                password = databaseResult[database.Users.password])
            )
        }
    }

    @Test
    fun storeUserDuplicate() {
        assertThrows<Throwable>(""
        ) {
            runBlocking {
                userRepo.storeUser("a", "k", "c")
                    ?: throw NullPointerException("Store user null")
            }
        }
    }

    @Test
    fun getUserTest() {
        runBlocking {
            val testUser = userRepo.storeUser("a", "y", "c")
                ?: throw NullPointerException("Store user null")
            assertEquals(testUser, userRepo.getUser(testUser.uid))
        }
    }

    companion object {
        private val embeddedPostgres: EmbeddedPostgres = EmbeddedPostgres.builder()
            .setPort(5432)
            .start()
        private val dataSource: DataSource = embeddedPostgres.postgresDatabase

        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            Database.connect(dataSource)
            transaction {
                SchemaUtils.create(Users)
                SchemaUtils.create(Posts)
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            embeddedPostgres.close()
        }
    }
}

object Posts : Table() {
    val postId: Column<Int> = integer("task_id").autoIncrement()
    val uid: Column<Int> = integer("uid").references(Users.uid)
    val text = varchar("text", MAX_POST_LENGTH)
}

object Users : Table() {
    val uid: Column<Int> = integer("uid").autoIncrement().primaryKey()
    val username = varchar("username", MAX_USERNAME_LENGTH)
    val email = varchar("email", MAX_EMAIL_LENGTH).uniqueIndex()
    val password = varchar("password", MAX_PASSWORD_LENGTH)
}
