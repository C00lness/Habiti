package com.habiti.ti.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.habiti.core.ai.MentorType
import com.habiti.core.ai.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mentor_prefs")

object MentorPreferences {
    private val MENTOR_TYPE_KEY = stringPreferencesKey("mentor_type")
    private val MENTOR_NAME_KEY = stringPreferencesKey("mentor_name")
    private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")

    fun getUserPreferences(context: Context): Flow<UserPreferences> {
        return context.dataStore.data.map { prefs ->
            UserPreferences(
                mentorType = when (prefs[MENTOR_TYPE_KEY]) {
                    MentorType.MALE.name -> MentorType.MALE
                    MentorType.FEMALE.name -> MentorType.FEMALE
                    else -> MentorType.MALE
                },
                mentorName = prefs[MENTOR_NAME_KEY] ?: "Наставник",
                isOnboardingCompleted = prefs[ONBOARDING_COMPLETED_KEY] ?: false
            )
        }
    }

    suspend fun saveUserPreferences(context: Context, prefs: UserPreferences) {
        context.dataStore.edit { editor ->
            editor[MENTOR_TYPE_KEY] = prefs.mentorType.name
            editor[MENTOR_NAME_KEY] = prefs.mentorName
            editor[ONBOARDING_COMPLETED_KEY] = prefs.isOnboardingCompleted
        }
    }
}