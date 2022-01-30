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
package io.github.carlomicieli.springbootapp.infrastructure.logging

import org.slf4j.Logger
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.util.StringUtils
import reactor.core.publisher.Flux
import java.io.ByteArrayOutputStream
import java.nio.channels.Channels
import java.util.Optional
import java.util.stream.Collectors

class LoggingRequestDecorator internal constructor(log: Logger, delegate: ServerHttpRequest) : ServerHttpRequestDecorator(delegate) {

    private val body: Flux<DataBuffer>?

    override fun getBody(): Flux<DataBuffer> {
        return body!!
    }

    init {
        if (log.isDebugEnabled) {
            val path = delegate.uri.path
            val query = delegate.uri.query
            val method = Optional.ofNullable(delegate.method).orElse(HttpMethod.GET).name
            val headers = delegate.headers.asString()
            log.debug(
                "{} {}\n {}", method, path + (if (StringUtils.hasText(query)) "?$query" else ""), headers
            )
            body = super.getBody().doOnNext { buffer: DataBuffer ->
                val bodyStream = ByteArrayOutputStream()
                Channels.newChannel(bodyStream).write(buffer.asByteBuffer().asReadOnlyBuffer())
                log.debug("{}: {}", "request", String(bodyStream.toByteArray()))
            }
        } else {
            body = super.getBody()
        }
    }
}

fun HttpHeaders.asString(): String {
    return entries
        .stream()
        .map { entry: Map.Entry<String, List<String?>?> ->
            " ${entry.key}: [" + java.lang.String.join(
                ";",
                entry.value
            ) + "]"
        }
        .collect(Collectors.joining("\n"))
}
