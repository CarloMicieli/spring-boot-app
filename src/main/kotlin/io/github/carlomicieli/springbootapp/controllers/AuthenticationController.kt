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
package io.github.carlomicieli.springbootapp.controllers

import io.github.carlomicieli.springbootapp.security.AuthenticatedUser
import io.github.carlomicieli.springbootapp.security.JwtAuthenticationService
import io.github.carlomicieli.springbootapp.security.Unauthorized
import kotlinx.serialization.Serializable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@RestController
@RequestMapping("/auth")
class AuthenticationController(private val jwtAuthenticationService: JwtAuthenticationService) {

    @GetMapping
    suspend fun auth(@AuthenticationPrincipal principal: Principal) =
        ResponseEntity.ok(jwtAuthenticationService.generateToken(principal.name))

    @PostMapping("/register")
    suspend fun register(@Valid @RequestBody profile: Profile): ResponseEntity<JwtToken> {
        val token = jwtAuthenticationService.register(profile.username, profile.password)
        return ResponseEntity.accepted().header(HttpHeaders.AUTHORIZATION, token).build()
    }

    @PostMapping("/login")
    suspend fun login(@Valid @RequestBody login: Login): ResponseEntity<JwtToken> =
        when (val result = jwtAuthenticationService.authenticate(login.username, login.password)) {
            is AuthenticatedUser -> ResponseEntity.ok(JwtToken(result.jwtToken))
            is Unauthorized -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
}

@Serializable
data class Profile(
    @NotBlank
    @Size(max = 50)
    val username: String,

    @NotBlank
    @Size(max = 50)
    val password: String
)

@Serializable
data class JwtToken(val token: String)

@Serializable
data class Login(
    @NotBlank
    @Size(max = 50)
    val username: String,

    @NotBlank
    @Size(max = 50)
    val password: String
)
