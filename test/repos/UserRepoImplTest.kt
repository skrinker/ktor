import database.MAX_EMAIL_LENGTH
import database.MAX_PASSWORD_LENGTH
import database.MAX_POST_LENGTH
import database.MAX_USERNAME_LENGTH
import database.Users.autoIncrement
import database.Users.uniqueIndex
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import models.User
import org.jetbrains.exposed.exceptions.ExposedSQLException
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
import repos.UserRepoImpl
import javax.sql.DataSource
import kotlin.NullPointerException
import kotlin.test.assertEquals

class UserRepoImplTest {
    private val userRepo = UserRepoImpl()

    @Test
    fun storeUserTest() {
        runBlocking {
            val testUser = userRepo.storeUser("a", "b", "c")
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
        assertThrows<ExposedSQLException> {
            runBlocking {
                userRepo.storeUser("a", "c", "c")
                userRepo.storeUser("a", "c", "c")
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

    @Test
    fun getUserByEmail() {
        runBlocking {
            val testUser = userRepo.storeUser("a", "r", "c")
                ?: throw NullPointerException("Store user null")
            assertEquals(testUser, userRepo.getUserByEmail(testUser.email))
        }
    }

    companion object {
        private val embeddedPostgres: EmbeddedPostgres = EmbeddedPostgres
            .builder()
            .setPort(5432)
            .start()
        private val dataSource: DataSource = embeddedPostgres.postgresDatabase

        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            Database.connect(dataSource)
            transaction {
                SchemaUtils.create(Users, Posts)
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            transaction {
                SchemaUtils.drop(Users, Posts)
            }
            embeddedPostgres.close()
        }
    }
}

object Posts : Table() {
    val postId: Column<Int> = integer("post_id").autoIncrement()
    val uid: Column<Int> = integer("uid").references(Users.uid)
    val text = varchar("text", MAX_POST_LENGTH)
}

object Users : Table() {
    val uid: Column<Int> = integer("uid").autoIncrement()
    override val primaryKey: PrimaryKey = PrimaryKey(uid)
    val username = varchar("username", MAX_USERNAME_LENGTH)
    val email = varchar("email", MAX_EMAIL_LENGTH).uniqueIndex()
    val password = varchar("password", MAX_PASSWORD_LENGTH)
}
