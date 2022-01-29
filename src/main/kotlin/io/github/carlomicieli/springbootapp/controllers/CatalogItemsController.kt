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

import io.github.carlomicieli.springbootapp.domain.CatalogItem
import io.github.carlomicieli.springbootapp.repositories.CatalogItemsRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.hateoas.Link
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api/catalog_items")
class CatalogItemsController(val catalogItemsRepository: CatalogItemsRepository) {

    @GetMapping
    fun getAllCatalogItems(): Flux<CatalogItem> = catalogItemsRepository.findAll()

    @GetMapping("/{id}")
    suspend fun getCatalogItemById(@PathVariable id: UUID): ResponseEntity<CatalogItem> {
        var catalogItem = catalogItemsRepository.findById(id).awaitSingleOrNull()
        if (catalogItem != null) {
            return ResponseEntity.ok(catalogItem)
        } else {
            return ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    suspend fun postCatalogItem(@Valid @RequestBody newCatalogItem: CatalogItem): ResponseEntity<Void> {
        val newId = catalogItemsRepository.save(newCatalogItem).map { it.catalogItemId }.awaitSingleOrNull()
        val link = Link.of("/api/catalog_item/{id}")
        return ResponseEntity.created(link.expand(newId?.toString() ?: "").toUri()).build()
    }
}
