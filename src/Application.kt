package dev.hashnode.danielwaiguru

import dev.hashnode.danielwaiguru.auth.JwtService
import dev.hashnode.danielwaiguru.auth.UserSession
import dev.hashnode.danielwaiguru.auth.hash
import dev.hashnode.danielwaiguru.database.DatabaseFactory
import dev.hashnode.danielwaiguru.repos.PostRepoImpl
import dev.hashnode.danielwaiguru.repos.UserRepoImpl
import dev.hashnode.danielwaiguru.routes.registerPostRoutes
import dev.hashnode.danielwaiguru.routes.registerUserRoutes
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.locations.*
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import kotlin.collections.set


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@OptIn(KtorExperimentalAPI::class)
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    DatabaseFactory.init()
    val userRepo = UserRepoImpl()
    val postRepo = PostRepoImpl()
    val jwtService = JwtService()
    val hashFunction = { s: String -> hash(s) }

    install(Locations) {
    }
    install(ContentNegotiation) {
        gson()
    }
    install(Sessions) {
        cookie<UserSession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        jwt("jwt") {
            verifier(jwtService.verifier) // 2
            realm = "Todo Server"
            validate {
                val payload = it.payload
                val claim = payload.getClaim("uid")
                val claimString = claim.asInt()
                val user = userRepo.getUser(claimString)
                user
            }
        }

    }
    registerUserRoutes(userRepo, jwtService, hashFunction)
    registerPostRoutes(userRepo, postRepo)
}


