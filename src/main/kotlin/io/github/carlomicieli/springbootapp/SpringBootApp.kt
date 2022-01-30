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
package io.github.carlomicieli.springbootapp

import io.github.carlomicieli.springbootapp.security.JwtAuthenticationService
import io.github.carlomicieli.springbootapp.security.JwtConfiguration
import io.github.carlomicieli.springbootapp.security.Security
import io.github.carlomicieli.springbootapp.security.UsersRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import java.time.Clock

@SpringBootApplication
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtConfiguration::class)
class SpringBootApp

val beans = beans {
    bean<Clock>() {
        Clock.systemDefaultZone()
    }

    bean<Logger> {
        LoggerFactory.getLogger("log")
    }

    profile("local") {
        bean {
            CommandLineRunner {
                runBlocking {
                    val usersRepository = ref<UsersRepository>()
                    val jwtAuthenticationService = ref<JwtAuthenticationService>()

                    usersRepository.deleteAll().awaitSingleOrNull()

                    jwtAuthenticationService.register("user", "user")
                }
            }
        }
    }
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) {
        beans.initialize(context)
        Security.beans.initialize(context)
    }
}

fun main(args: Array<String>) {
    runApplication<SpringBootApp>(*args)
}
