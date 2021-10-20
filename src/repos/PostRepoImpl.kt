package dev.hashnode.danielwaiguru.repos

import dev.hashnode.danielwaiguru.database.DatabaseFactory.dbQuery
import dev.hashnode.danielwaiguru.database.Posts
import dev.hashnode.danielwaiguru.models.Post
import dev.hashnode.danielwaiguru.models.PostDomain
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement

class PostRepoImpl: PostRepo {
    override suspend fun publishPost(uid: Int, text: String): Post? {
        var statement: InsertStatement<Number>? = null
        dbQuery {
            statement = Posts.insert { post ->
                post[Posts.uid] = uid
                post[Posts.text] = text
            }
        }
        return rowToPost(statement?.resultedValues?.get(0))
    }

    override suspend fun getPosts(uid: Int): List<Post> = dbQuery {
        Posts.select { Posts.uid.eq(uid) }.mapNotNull { rowToPost(it) }
    }

    override suspend fun deletePost(userId: Int, postId: Int): Int = dbQuery {
        Posts.deleteWhere { (Posts.uid.eq(userId)) and (Posts.postId.eq(postId)) }
    }

    override suspend fun updatePost(userId: Int, postId: Int, post: PostDomain): Int = dbQuery {
        Posts.update({ Posts.uid.eq(userId) and Posts.postId.eq(postId) }) {
            it[text] = post.text
        }
    }

    private fun rowToPost(row: ResultRow?): Post? {
        if (row == null) {
            return null
        }
        return Post(
            postId = row[Posts.postId],
            uid = row[Posts.uid],
            text = row[Posts.text]
        )
    }
}