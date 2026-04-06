package com.habiti.ti.impl

import android.content.Context
import com.habiti.ti.R
import com.habiti.core.ai.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TiMotivatorImpl(private val context: Context, userPreferencesFlow: Flow<UserPreferences>) : TiMotivator {

    private var mentorName: String = runBlocking { userPreferencesFlow.first().mentorName }
    private var mentorType: MentorType = runBlocking { userPreferencesFlow.first().mentorType }
    init {
        // Подписываемся на изменения в настройках
        CoroutineScope(Dispatchers.IO).launch {
            userPreferencesFlow.collect { prefs ->
                mentorName = prefs.mentorName
                mentorType = prefs.mentorType
            }
        }
    }
    override fun getMessage(context: MessageContext): TiMessage {
        val name = mentorName
        return when (context) {
            is MessageContext.Completed -> {
                val messages = this.context.resources.getStringArray(R.array.complete_messages)
                val randomMessage = messages.random()
                TiMessage("$name: $randomMessage 🎯", TiEmotion.HAPPY)
            }
            is MessageContext.Streak -> {
                val streak = context.streak
                val habitName = context.habitName
                val (text, emotion) = getStreakMessage(habitName, streak)
                TiMessage("$name: $text", emotion)
            }
            is MessageContext.Missed -> {
                val messages = this.context.resources.getStringArray(R.array.missed_messages)
                val randomMessage = messages.random()
                TiMessage("$name: ${context.habitName} $randomMessage 😿", TiEmotion.SAD)
            }
            is MessageContext.Morning -> {
                val messages = this.context.resources.getStringArray(R.array.morning_messages)
                val template = messages.random()
                val text = String.format(template, context.habitName)
                TiMessage("$name: $text ☀️", TiEmotion.HAPPY)
            }
            is MessageContext.Evening -> {
                val messages = this.context.resources.getStringArray(R.array.evening_messages)
                val template = messages.random()
                val text = String.format(template, context.habitName)
                TiMessage("$name: $text 🌙", TiEmotion.MOTIVATED)
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
            streak == 14 -> {
                val text = context.resources.getString(R.string.streak_message_4)
                text to TiEmotion.PROUD
            }
            streak == 30 -> {
                val text = context.resources.getString(R.string.streak_message_5)
                text to TiEmotion.PROUD
            }
            streak % 10 == 0 -> {
                val text = context.resources.getString(R.string.streak_message_6, streak)
                text to TiEmotion.MOTIVATED
            }
            streak % 7 == 0 -> {
                val text = context.resources.getString(R.string.streak_message_7, streak)
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