package com.example.ti

data class TiMessage(
    val text: String,
    val type: TiMessageType = TiMessageType.INFO
)