package com.habiti.habits.impl.common

public fun isMilestoneStreak(streak: Int): Boolean {
    return streak == 1 ||
            streak == 3 ||
            streak == 7 ||
            streak == 14 ||
            streak == 21 ||
            streak == 30 ||
            streak % 10 == 0
}