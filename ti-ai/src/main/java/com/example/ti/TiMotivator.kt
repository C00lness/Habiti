package com.example.ti

import android.annotation.SuppressLint
import android.content.Context

object TiMotivator {
    private lateinit var appContext: Context
    fun init(context: Context) {  // ← добавим метод инициализации
        appContext = context.applicationContext
    }
    fun getMessage(messageContext: MessageContext): String {
        return when (messageContext) {
            is MessageContext.Morning -> getMorningMessage(messageContext.habitName, )
            is MessageContext.Evening -> getEveningMessage(messageContext.habitName)
            is MessageContext.Streak -> getStreakMessage(messageContext.habitName, messageContext.streak)
            is MessageContext.Missed -> getMissedMessage(messageContext.habitName)
            is MessageContext.Completed -> getCompletedMessage(messageContext.habitName)
        }
    }

    private fun getMorningMessage(habitName: String): String {
        return String.format(appContext.resources.getStringArray(R.array.morning_messages).random(), habitName)
    }

    private fun getEveningMessage(habitName: String): String {
        return String.format(appContext.resources.getStringArray(R.array.evening_messages).random(), habitName)
    }

    @SuppressLint("StringFormatInvalid", "StringFormatMatches")
    private fun getStreakMessage(habitName: String, streak: Int): String {
        return when {
            streak == 1 -> appContext.resources.getString(R.string.streak_message_1, streak)
            streak == 3 -> appContext.resources.getString(R.string.streak_message_2)
            streak == 7 -> appContext.resources.getString(R.string.streak_message_3)
            streak == 30 -> appContext.resources.getString(R.string.streak_message_4)
            streak % 10 == 0 -> appContext.resources.getString(R.string.streak_message_5, streak)
            streak % 7 == 0 -> appContext.resources.getString(R.string.streak_message_6, habitName)
            else -> String.format(appContext.resources.getStringArray(R.array.streak_messages_else).random(), streak)
        }
    }

    private fun getMissedMessage(habitName: String): String {
        return appContext.resources.getStringArray(R.array.missed_messages).random()
    }

    private fun getCompletedMessage(habitName: String): String {
        return appContext.resources.getStringArray(R.array.complete_messages).random()
    }
}