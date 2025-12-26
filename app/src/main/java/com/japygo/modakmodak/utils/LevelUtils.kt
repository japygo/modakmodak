package com.japygo.modakmodak.utils

/**
 * Level and Experience related utility functions.
 * Level thresholds are synchronized with [com.japygo.modakmodak.data.repository.ModakRepository.calculateFireLevel]
 */
object LevelUtils {

    /**
     * Experience thresholds for each level.
     * Level 1: 0 - 299
     * Level 2: 300 - 999
     * Level 3: 1000 - 2999
     * Level 4: 3000 - 7999
     * Level 5: 8000+
     */
    private val thresholds = listOf(0, 300, 1000, 3000, 8000)

    /**
     * Calculates the progress within the current level (0.0 to 1.0).
     */
    fun getLevelProgress(exp: Int): Float {
        val level = calculateLevel(exp)
        if (level >= thresholds.size) return 1.0f // Max level

        val currentThreshold = thresholds[level - 1]
        val nextThreshold = thresholds[level]
        
        val totalNeededInThisLevel = nextThreshold - currentThreshold
        val gainedInThisLevel = exp - currentThreshold
        
        return (gainedInThisLevel.toFloat() / totalNeededInThisLevel).coerceIn(0f, 1f)
    }

    /**
     * Calculates level based on total exp.
     * Sync with ModakRepository.calculateFireLevel
     */
    fun calculateLevel(exp: Int): Int {
        return when {
            exp < 300 -> 1
            exp < 1000 -> 2
            exp < 3000 -> 3
            exp < 8000 -> 4
            else -> 5
        }
    }
}
