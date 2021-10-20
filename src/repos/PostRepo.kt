package dev.hashnode.danielwaiguru.repos

import dev.hashnode.danielwaiguru.models.Post
import dev.hashnode.danielwaiguru.models.PostDomain

interface PostRepo {
    suspend fun publishPost(uid: Int, text: String): Post?
    suspend fun getPosts(uid: Int): List<Post>
    suspend fun deletePost(userId: Int, postId: Int): Int
    suspend fun updatePost(userId: Int, postId: Int, post: PostDomain): Int
}