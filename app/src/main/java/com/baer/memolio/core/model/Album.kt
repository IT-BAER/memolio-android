package com.baer.memolio.core.model

data class Album(
    val id: String,
    val name: String,
    val coverPhotoId: String?,
    val createdAt: Long,
    val sortOrder: Int
)
