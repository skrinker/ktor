package repos

import database.MAX_EMAIL_LENGTH
import database.MAX_PASSWORD_LENGTH
import database.MAX_POST_LENGTH
import database.MAX_USERNAME_LENGTH
import database.Users.autoIncrement
import database.Users.uniqueIndex
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.runBlocking
import models.Post
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import javax.sql.DataSource
import kotlin.test.assertEquals

class PostRepoImplTest {
    private val userRepo = UserRepoImpl()
    private val postRepo = PostRepoImpl()
    private val storedUser = runBlocking {
        userRepo.storeUser("a", "b", "c")
            ?: throw NullPointerException("Store user null")
    }

    @Test
    fun publishPostTest() {
        runBlocking {
            val publishedPost = postRepo.publishPost(storedUser.uid, "text")
            val databaseResult = transaction {
                Posts.select { Posts.uid.eq(storedUser.uid) }.firstOrNull()
            } ?: throw NullPointerException("Database result null")
            assertEquals(publishedPost, Post(
                uid = databaseResult[database.Posts.uid],
                postId = databaseResult[database.Posts.postId],
                text = databaseResult[database.Posts.text])
            )
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
