package models

data class Post(
    val uid: Int,
    val postId: Int,
    val text: String
)

interface PostRepo {
    suspend fun publishPost(uid: Int, text: String): Post?
    suspend fun getPosts(uid: Int): List<Post>
    suspend fun deletePost(userId: Int, postId: Int): Int
    suspend fun updatePost(userId: Int, postId: Int, post: PostDomain): Int
}
