package com.habiti.ti.mentor

import android.content.Context
import androidx.core.content.edit

class PromoPreferences (context: Context) {
    private val prefs = context.getSharedPreferences("promo", Context.MODE_PRIVATE)
    private val MR_STRICK_UNLOCK = "mr_strick_unlocked"

    private val DANCING_WOMAN_UNLOCK = "dancing_woman_unlocked"

    var isMrStrickUnlocked: Boolean
        get() = prefs.getBoolean(MR_STRICK_UNLOCK, false)
        set(value) = prefs.edit { putBoolean(MR_STRICK_UNLOCK, value) }

    var isDancingWomanUnlocked: Boolean
        get() = prefs.getBoolean(DANCING_WOMAN_UNLOCK, false)
        set(value) = prefs.edit { putBoolean(DANCING_WOMAN_UNLOCK, value) }
}