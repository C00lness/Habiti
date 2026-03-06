package com.habiti.habits.impl.domain

data class Habit(
    val id: String,
    val name: String,
    val icon: String,
    val color: Long,
    val current: Int,
    val target: Int,
    val streak: Int
)