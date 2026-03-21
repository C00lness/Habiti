package com.habiti.ti.impl

import android.content.Context
import com.habiti.ti.R
import com.habiti.core.ai.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TiMotivatorImpl(
    private val context: Context,
    catNameFlow: Flow<String>
) : TiMotivator {

    private var catName: String = runBlocking { catNameFlow.first() }

    override fun getMessage(context: MessageContext): TiMessage {
        return when (context) {
            is MessageContext.Completed -> {
                val messages = this.context.resources.getStringArray(R.array.complete_messages)
                val randomMessage = messages.random()
                TiMessage("$catName: $randomMessage 🐾", TiEmotion.HAPPY)
            }
            is MessageContext.Streak -> {
                val streak = context.streak
                val habitName = context.habitName
                val (text, emotion) = getStreakMessage(habitName, streak)
                TiMessage("$catName: $text", emotion)
            }
            is MessageContext.Missed -> {
                val messages = this.context.resources.getStringArray(R.array.missed_messages)
                val randomMessage = messages.random()
                TiMessage("$catName: $randomMessage 😿", TiEmotion.SAD)
            }
            is MessageContext.Morning -> {
                val messages = this.context.resources.getStringArray(R.array.morning_messages)
                val template = messages.random()
                val text = String.format(template, context.habitName)
                TiMessage("$catName: $text ☀️", TiEmotion.HAPPY)
            }
            is MessageContext.Evening -> {
                val messages = this.context.resources.getStringArray(R.array.evening_messages)
                val template = messages.random()
                val text = String.format(template, context.habitName)
                TiMessage("$catName: $text 🌙", TiEmotion.MOTIVATED)
            }
        }
    }

    private fun getStreakMessage(habitName: String, streak: Int): Pair<String, TiEmotion> {
        return when {
            streak == 1 -> {
                val text = context.resources.getString(R.string.streak_message_1, habitName)
                text to TiEmotion.SURPRISED
            }
            streak == 3 -> {
                val text = context.resources.getString(R.string.streak_message_2)
                text to TiEmotion.HAPPY
            }
            streak == 7 -> {
                val text = context.resources.getString(R.string.streak_message_3)
                text to TiEmotion.PROUD
            }
            streak == 30 -> {
                val text = context.resources.getString(R.string.streak_message_4)
                text to TiEmotion.PROUD
            }
            streak % 10 == 0 -> {
                val text = context.resources.getString(R.string.streak_message_5, streak)
                text to TiEmotion.MOTIVATED
            }
            streak % 7 == 0 -> {
                val text = context.resources.getString(R.string.streak_message_6, streak)
                text to TiEmotion.MOTIVATED
            }
            else -> {
                val messages = context.resources.getStringArray(R.array.streak_messages_else)
                val template = messages.random()
                val text = String.format(template, streak)
                text to TiEmotion.MOTIVATED
            }
        }
    }
}