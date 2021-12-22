package models

import io.ktor.auth.Principal
import java.io.Serializable

data class User(
    val uid: Int,
    val username: String,
    val email: String,
    val password: String
) : Serializable, Principal {
    companion object {
        const val serialVersionUID = 1L
    }
}
