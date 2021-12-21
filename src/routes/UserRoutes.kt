package routes

import auth.JwtService
import auth.UserSession
import repos.UserRepo
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.application.application
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set

fun Route.users(db: UserRepo, jwtService: JwtService, hashFunction: (String) -> String) {
    route("/$API_VERSION/user") {
        post("/register") {
            val signupParameters = call.receive<Parameters>()
            val password = signupParameters["password"]
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, "Missing Fields")
            val displayName = signupParameters["username"]
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, "Missing Fields")
            val email = signupParameters["email"]
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, "Missing Fields")
            val hash = hashFunction(password)
            try {
                val newUser = db.storeUser(displayName, email, password = hash)
                newUser?.uid?.let {
                    call.sessions.set(UserSession(it))
                    call.respondText(
                        jwtService.generateToken(newUser),
                        status = HttpStatusCode.Created
                    )
                }
            } catch (err: Throwable) {
                application.log.error("Failed to register user", err)
                call.respond(HttpStatusCode.BadRequest, "User registration failed")
            }
        }

        post("/login") {
            val signInParameters = call.receive<Parameters>()
            val email = signInParameters["email"] ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
            val password = signInParameters["password"] ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
            val hash = hashFunction(password)
            try {
                val currentUser = db.getUserByEmail(email)
                currentUser?.uid?.let {
                    if (currentUser.password == hash) {
                        call.sessions.set(UserSession(it))
                        call.respondText(jwtService.generateToken(currentUser))
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest, "Invalid credentials"
                        )
                    }
                }
            } catch (err: Throwable) {
                application.log.error("Failed to login", err)
                call.respond(HttpStatusCode.BadRequest, "User login failed")
            }
        }

        put("/about") {
            val aboutParams = call.receive<Parameters>()
            val text = aboutParams["text"] ?: return@put call.respond(
                HttpStatusCode.BadRequest, "Text field is required")
            val user = call.sessions.get<UserSession>()?.let {
                db.getUser(it.uid)
            }
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "Unauthorized user")
                return@put
            }
            try {
                application.log.info(user.toString())

                val updatedUserId = db.updateAbout(
                    user.uid,
                    text)
                if (updatedUserId < 1) {
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
            } catch (err: Exception) {
                application.log.error("Failed to update about", err)
                call.respond(HttpStatusCode.BadRequest, "About update failed")
            }
        }
    }
}

fun Application.registerUserRoutes(db: UserRepo, jwtService: JwtService, hashFunction: (String) -> String) {
    routing {
        users(db, jwtService, hashFunction)
    }
}

const val API_VERSION = "v1"
