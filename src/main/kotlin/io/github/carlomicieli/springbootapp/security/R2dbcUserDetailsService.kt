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

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class R2dbcUserDetailsService(
    private val usersRepository: UsersRepository,
    private val encoder: PasswordEncoder
) : ReactiveUserDetailsService {

    override fun findByUsername(username: String): Mono<UserDetails> =
        usersRepository.findByUsername(username)
            .map { CustomUserDetails(it) }

    fun registerUser(username: String, password: String): Mono<User> {
        val user = User(
            username,
            password = encoder.encode(password),
            expired = false,
            locked = false,
            role = "USER",
            version = 0
        )
        return usersRepository.save(user)
    }
}

@Table("users")
data class User(
    @Id
    val username: String,
    val password: String,

    @Column("is_expired")
    val expired: Boolean = false,

    @Column("is_locked")
    val locked: Boolean = false,

    val role: String = "USER",

    @Version
    val version: Int = 0
)

@Repository
interface UsersRepository : ReactiveCrudRepository<User, String> {
    fun findByUsername(username: String): Mono<User>
}

class CustomUserDetails(private val user: User) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority(user.role))

    override fun getPassword() = user.password

    override fun getUsername() = user.username

    override fun isEnabled() = !user.expired

    override fun isCredentialsNonExpired() = !user.expired

    override fun isAccountNonExpired() = !user.expired

    override fun isAccountNonLocked() = !user.locked
}
