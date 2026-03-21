package com.habiti.core.ai

data class TiMessage(
    val text: String,
    val emotion: TiEmotion = TiEmotion.HAPPY
)
