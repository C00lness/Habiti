package com.habiti.ti.cat

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.habiti.ti.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("habiti_cat")

object CatNamePreferences {

    private fun getCatNameKey(context: Context): Preferences.Key<String> {
        return stringPreferencesKey(context.getString(R.string.cat_name))
    }

    fun getCatName(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[getCatNameKey(context)] ?: context.getString(R.string.cat_name_default)
        }
    }

    suspend fun setCatName(context: Context, name: String) {
        context.dataStore.edit { preferences ->
            preferences[getCatNameKey(context)] = name
        }
    }
}