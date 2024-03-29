package repos

import models.User

interface UserRepo {
    suspend fun storeUser(username: String, email: String, password: String): User?
    suspend fun getUser(uid: Int): User?
    suspend fun getUserByEmail(email: String): User?
}
