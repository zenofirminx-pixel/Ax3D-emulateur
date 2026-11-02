package com.infinity.chat

data class ChatMessage(
    val role: String,
    var content: String,
    val streaming: Boolean = false
)
