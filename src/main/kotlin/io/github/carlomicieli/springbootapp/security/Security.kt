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

import org.springframework.context.support.beans
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import reactor.core.publisher.Mono

object Security {
    val beans = beans {

        bean<PasswordEncoder> {
            BCryptPasswordEncoder()
        }

        bean {
            val converter = ref<JwtServerAuthenticationConverter>()
            val authManager = ref<JwtAuthenticationManager>()
            val filter = AuthenticationWebFilter(authManager)
            filter.setServerAuthenticationConverter(converter)

            val http = ref<ServerHttpSecurity>()

            http {
                authorizeExchange {
                    authorize("/auth/login", permitAll)
                    authorize("/auth/register", permitAll)
                }
                exceptionHandling {
                    authenticationEntryPoint = ServerAuthenticationEntryPoint { exchange, _ ->
                        Mono.fromRunnable {
                            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                            exchange.response.headers.set(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                        }
                    }
                }
                addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION)
                httpBasic { disable() }
                formLogin { disable() }
                csrf { disable() }
            }
        }
    }
}
