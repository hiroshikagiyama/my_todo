package com.toyota.my_todo

import java.util.UUID

data class TodoItem(
    val id: UUID = UUID.randomUUID(),
    var content: String,
    var isCompleted: Boolean = false
)