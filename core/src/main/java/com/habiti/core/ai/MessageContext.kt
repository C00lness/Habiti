package com.habiti.core.ai

sealed class MessageContext {
    data class Morning(val habitName: String) : MessageContext()
    data class Evening(val habitName: String) : MessageContext()
    data class Streak(val habitName: String, val streak: Int) : MessageContext()
    data class Missed(val habitName: String) : MessageContext()
    data class Completed(val habitName: String) : MessageContext()
}