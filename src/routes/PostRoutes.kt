package routes

import auth.UserSession
import io.ktor.application.Application
import models.PostDomain
import repos.PostRepo
import repos.UserRepo
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.application.application
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.jetbrains.exposed.exceptions.ExposedSQLException

@Suppress("LongMethod")
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
                        HttpStatusCode.Unauthorized, "Unauthorized user"
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
                } catch (err: ExposedSQLException) {
                    application.log.error("Error adding a post", err)
                    call.respond(HttpStatusCode.BadRequest, "Failed to add a post")
                }
            }
            get {
                val user = call.sessions.get<UserSession>()?.let {
                    userRepo.getUser(it.uid)
                }
                if (user == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized, "Unauthorized user"
                    )
                    return@get
                }
                try {
                    val posts = postRepo.getPosts(user.uid)
                    call.respond(posts)
                } catch (err: ExposedSQLException) {
                    application.log.error("Failed to get all posts", err)
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
                    call.respond(HttpStatusCode.BadRequest, "Unauthorized user")
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
                } catch (err: ExposedSQLException) {
                    application.log.error("Failed to update a post", err)
                    call.respond(HttpStatusCode.BadRequest, "Post update failed")
                }
            }
            delete("/{id}") {
                val user = call.sessions.get<UserSession>()?.let {
                    userRepo.getUser(it.uid)
                }
                with(application) {
                    log.info(user.toString())
                }
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
                } catch (err: ExposedSQLException) {
                    application.log.error("Failed to delete", err)
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
