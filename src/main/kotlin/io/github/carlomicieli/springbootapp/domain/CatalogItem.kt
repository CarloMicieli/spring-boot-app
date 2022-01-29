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
package io.github.carlomicieli.springbootapp.domain

import io.github.carlomicieli.springbootapp.infrastructure.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Serializable
@Table("catalog_items")
data class CatalogItem(
    @Id
    @SerialName("id")
    @Serializable(with = UUIDSerializer::class)
    val catalogItemId: UUID,

    @NotBlank
    @Size(max = 50)
    val brand: String,

    @NotBlank
    @Size(max = 10)
    @SerialName("item_number")
    val itemNumber: String,

    @NotBlank
    @Size(max = 50)
    val slug: String,

    @NotBlank
    @Size(max = 50)
    val scale: String,

    @NotBlank
    @Size(max = 1000)
    val description: String,

    @NotBlank
    @Size(max = 10)
    val epoch: String,

    val category: String,

    @Version
    val version: Int = 0
)
