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

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.temporal.ChronoUnit
import java.util.Date

@Component
class JwtSupport(private val clock: Clock, private val jwtConfiguration: JwtConfiguration) {

    private val issuer: String = "trenako.com"
    private val algorithmHS: Algorithm = Algorithm.HMAC256(jwtConfiguration.secret)
    private val verifier: JWTVerifier = JWT.require(algorithmHS)
        .withIssuer(issuer)
        .build()

    fun generate(username: String, roles: Array<String> = arrayOf()): BearerToken {
        val token = JWT.create()
            .withIssuer(issuer)
            .withSubject(username)
            .withIssuedAt(now())
            .withExpiresAt(nowPlus(jwtConfiguration.minutes, ChronoUnit.MINUTES))
            .withArrayClaim("roles", roles)
            .sign(algorithmHS)
        return BearerToken(token)
    }

    fun getUsername(token: BearerToken): String {
        val jwt: DecodedJWT = verifier.verify(token.value)
        return jwt.subject
    }

    fun isValid(token: BearerToken, user: UserDetails?): Boolean =
        try {
            val jwt: DecodedJWT = verifier.verify(token.value)
            jwt.subject == user?.username
        } catch (tokenExpiredException: TokenExpiredException) {
            false
        }

    private fun now(): Date = Date.from(clock.instant())

    private fun nowPlus(amountToAdd: Long, unit: ChronoUnit): Date = Date.from(clock.instant().plus(amountToAdd, unit))
}
