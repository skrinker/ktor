package dev.hashnode.danielwaiguru.routes

import dev.hashnode.danielwaiguru.auth.UserSession
import dev.hashnode.danielwaiguru.models.PostDomain
import dev.hashnode.danielwaiguru.repos.PostRepo
import dev.hashnode.danielwaiguru.repos.UserRepo
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

fun Route.posts(postRepo: PostRepo, userRepo: UserRepo) {
    authenticate("jwt") {
        route("/$API_VERSION/posts") {
            post("/create") {
                val postParams = call.receive<Parameters>()
                val text = postParams["text"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Title field is required")
                val user = call.sessions.get<UserSession>()?.let {
                    userRepo.getUser(it.uid)
                }
                if (user == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized, "Un authorized user"
                    )
                    return@post
                }
                try {
                    val currentPost = postRepo.publishPost(
                        uid = user.uid,
                        text = text)
                    currentPost?.postId?.let {
                        call.respond(
                            HttpStatusCode.Created, currentPost
                        )
                    }
                } catch (e: Throwable) {
                    application.log.error("Error adding a post", e)
                    call.respond(HttpStatusCode.BadRequest, "Failed to add a post")
                }
            }
            get {
                val user = call.sessions.get<UserSession>()?.let {
                    userRepo.getUser(it.uid)
                }
                if (user == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized, "Un authorized user"
                    )
                    return@get
                }
                try {
                    val posts = postRepo.getPosts(user.uid)
                    call.respond(posts)
                } catch (e: Throwable) {
                    application.log.error("Failed to get all posts", e)
                    call.respond(
                        HttpStatusCode.BadRequest, "Failed to get posts"
                    )
                }
            }
            put("/{id}") {
                val postParams = call.receive<Parameters>()
                val text = postParams["text"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest, "Title field is required")
                val post = PostDomain(text)
                val user = call.sessions.get<UserSession>()?.let {
                    userRepo.getUser(it.uid)
                }
                if (user == null) {
                    call.respond(HttpStatusCode.BadRequest, "Un authorized user")
                    return@put
                }
                try {
                    application.log.info(user.toString())

                    val postId = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest, "Unknown post id"
                    )
                    application.log.info(postId)
                    val updatedPostId = postRepo.updatePost(
                        user.uid,
                        postId.toInt(),
                        post)
                    if (updatedPostId < 1) {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = "Update not successful"
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = "Update successful"
                        )
                    }

                } catch (e: Exception) {
                    application.log.error("Failed to update a post", e)
                    call.respond(HttpStatusCode.BadRequest, "Post update failed")
                }
            }
            delete("/{id}") {
                val user = call.sessions.get<UserSession>()?.let {
                    userRepo.getUser(it.uid)
                }
                application.log.info(user.toString())
                call.application.environment.log.info(user.toString())
                call.application.environment.log.debug(user.toString())
                if (user == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized, "Un authorized user"
                    )
                    return@delete
                }
                try {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest, "Unknown post id"
                    )
                    application.log.info(id)
                    val deletedPost = postRepo.deletePost(user.uid, id.toInt())
                    if (deletedPost < 1) {
                        call.respond(
                            HttpStatusCode.BadRequest, "Post deletion failed!"
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.OK, "Post id $id deleted"
                        )
                    }

                } catch (e: Throwable) {
                    application.log.error("Failed to delete", e)
                    call.respond(
                        HttpStatusCode.BadRequest, "Failed to delete a post"
                    )
                }
            }
        }
    }
}
fun Application.registerPostRoutes(userRepo: UserRepo, postRepo: PostRepo) {
    routing {
        posts(postRepo, userRepo)
    }
}