package com.habiti.core.ai

data class UserPreferences(
    val mentorType: MentorType = MentorType.MALE,
    val mentorName: String = "Наставник",
    val isOnboardingCompleted: Boolean = false
) {}

enum class MentorType {
    MALE, FEMALE
}