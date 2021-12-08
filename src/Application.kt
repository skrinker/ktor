package com.example

import auth.JwtService
import auth.UserSession
import auth.hash
import database.DatabaseFactory
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.locations.Locations
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import repos.PostRepoImpl
import repos.UserRepoImpl
import routes.registerPostRoutes
import routes.registerUserRoutes
import kotlin.collections.set

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
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
