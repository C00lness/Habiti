package com.habiti.ti.mentor

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.habiti.core.ai.MentorType
import com.habiti.core.ai.UserPreferences
import com.habiti.ti.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habiti_mentor")

object MentorPreferences {
    private const val KEY_MENTOR_TYPE = "mentor_type"
    private const val KEY_MENTOR_NAME = "mentor_name"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    fun getUserPreferences(context: Context): Flow<UserPreferences> {
        return context.dataStore.data.map { prefs ->
            UserPreferences(
                mentorType = when (prefs[stringPreferencesKey(KEY_MENTOR_TYPE)]) {
                    MentorType.MALE.name -> MentorType.MALE
                    MentorType.FEMALE.name -> MentorType.FEMALE
                    else -> MentorType.MALE
                },
                mentorName = prefs[stringPreferencesKey(KEY_MENTOR_NAME)] ?: context.getString(R.string.mentor_name_default),
                isOnboardingCompleted = prefs[booleanPreferencesKey(KEY_ONBOARDING_COMPLETED)] ?: false
            )
        }
    }

    suspend fun saveUserPreferences(context: Context, prefs: UserPreferences) {
        context.dataStore.edit { editor ->
            editor[stringPreferencesKey(KEY_MENTOR_TYPE)] = prefs.mentorType.name
            editor[stringPreferencesKey(KEY_MENTOR_NAME)] = prefs.mentorName
            editor[booleanPreferencesKey(KEY_ONBOARDING_COMPLETED)] = prefs.isOnboardingCompleted
        }
    }
}