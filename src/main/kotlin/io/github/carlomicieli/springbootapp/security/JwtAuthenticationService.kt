/*
    MIT License

    Copyright (c) 2021-2022 (C) Carlo Micieli

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
package io.github.carlomicieli.springbootapp.security

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class JwtAuthenticationService(
    private val jwtSupport: JwtSupport,
    private val encoder: PasswordEncoder,
    private val usersRepository: UsersRepository,
    private val users: ReactiveUserDetailsService
) {

    suspend fun authenticate(username: String, password: String): Authentication {
        val user = users.findByUsername(username).awaitSingleOrNull()

        return if (user != null && encoder.matches(password, user.password)) {
            val roles = user.authorities.map { it.authority.lowercase() }.toTypedArray()
            AuthenticatedUser(jwtSupport.generate(user.username, roles).value)
        } else {
            Unauthorized
        }
    }

    suspend fun register(username: String, password: String): String {
        val created = usersRepository.save(User(username, encoder.encode(password), locked = false, expired = false, role = "USER")).awaitSingleOrNull()
        return if (created != null) jwtSupport.generate(username, arrayOf(created.role)).value else ""
    }

    fun generateToken(username: String): Authentication {
        return AuthenticatedUser(jwtSupport.generate(username).value)
    }
}

sealed interface Authentication
data class AuthenticatedUser(val jwtToken: String) : Authentication
object Unauthorized : Authentication
