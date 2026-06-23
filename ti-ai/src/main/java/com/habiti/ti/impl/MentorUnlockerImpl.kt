package com.habiti.ti.impl

import com.habiti.core.ai.MentorUnlocker
import com.habiti.ti.mentor.PromoPreferences

class MentorUnlockerImpl(
    private val promoPrefs: PromoPreferences
) : MentorUnlocker {
    override fun unlockDancingWoman() {
        promoPrefs.isDancingWomanUnlocked = true
    }

    override fun isDancingWomanUnlocked(): Boolean {
        return promoPrefs.isDancingWomanUnlocked
    }
}