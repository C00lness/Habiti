package com.habiti.core.ai

data class UserPreferences(
    val mentorType: MentorType = MentorType.MALE,
    val mentorName: String = "",
    val isOnboardingCompleted: Boolean = false
)