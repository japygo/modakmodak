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
    /**
     * Level Table Data Class
     */
    data class LevelInfo(
        val level: Int,
        val requiredExp: Int, // EXP required to reach NEXT level
        val cumulativeExp: Int, // Total exp to reach this level
        val name: String,
        val characterBaseSizeVar: Float // For reference, used in calculation
    )

    private val levelTable = listOf(
        LevelInfo(1, 200, 0, "작은 불씨", 0.58f),
        LevelInfo(2, 300, 200, "타오르는 불씨", 0.66f),
        LevelInfo(3, 400, 500, "촛불", 0.74f),
        LevelInfo(4, 500, 900, "큰 촛불", 0.82f),
        LevelInfo(5, 600, 1400, "횃불", 0.90f),
        LevelInfo(6, 700, 2000, "큰 횃불", 0.98f),
        LevelInfo(7, 800, 2700, "작은 모닥불", 1.06f),
        LevelInfo(8, 900, 3500, "모닥불", 1.14f),
        LevelInfo(9, 1000, 4400, "큰 모닥불", 1.22f),
        LevelInfo(10, 1200, 5400, "화덕", 1.30f),
        LevelInfo(11, 1400, 6600, "등불", 1.38f),
        LevelInfo(12, 1600, 8000, "횃불대", 1.46f),
        LevelInfo(13, 1800, 9600, "장작불", 1.54f),
        LevelInfo(14, 2000, 11400, "큰 장작불", 1.62f),
        LevelInfo(15, 2500, 13400, "화로", 1.70f),
        LevelInfo(16, 3000, 15900, "용광로", 1.78f),
        LevelInfo(17, 3500, 18900, "대형 용광로", 1.86f),
        LevelInfo(18, 4000, 22400, "불기둥", 1.94f),
        LevelInfo(19, 5000, 26400, "큰 불기둥", 2.02f),
        LevelInfo(20, 5000, 31400, "태양불꽃", 2.10f)
        // 20+ continues with 5000 interval
    )

    /**
     * Calculates the progress within the current level (0.0 to 1.0).
     */
    fun getLevelProgress(totalExp: Int): Float {
        val level = calculateLevel(totalExp)
        
        if (level > 20) {
            val baseExpForLevel20 = levelTable.last().cumulativeExp
            val levelsAfter20 = level - 20
            val currentLevelStartExp = baseExpForLevel20 + ((levelsAfter20 - 1) * 5000)
            val gained = totalExp - currentLevelStartExp
            return (gained.toFloat() / 5000f).coerceIn(0f, 1f)
        }

        val info = levelTable.find { it.level == level } ?: return 0f
        val nextInfo = levelTable.find { it.level == level + 1 }
        
        // If max level defined in table (though we handle > 20 above)
        if (nextInfo == null) return 1.0f 

        val currentThreshold = info.cumulativeExp
        val nextThreshold = nextInfo.cumulativeExp
        
        val totalNeeded = nextThreshold - currentThreshold
        val gained = totalExp - currentThreshold
        
        return (gained.toFloat() / totalNeeded).coerceIn(0f, 1f)
    }

    /**
     * Calculates level based on total exp.
     * Use binary search or iteration. Since list is small, iteration is fine.
     */
    fun calculateLevel(totalExp: Int): Int {
        // Check for max level in table logic first
        val maxDefined = levelTable.last()
        if (totalExp >= maxDefined.cumulativeExp + 5000) {
             // Logic for unlimited levels: Level 20 starts at 31400.
             // Level 21 starts at 31400 + 5000.
             val excess = totalExp - maxDefined.cumulativeExp
             return 20 + (excess / 5000)
        }

        // Iterate backwards finding the highest level we qualify for
        for (i in levelTable.indices.reversed()) {
            if (totalExp >= levelTable[i].cumulativeExp) {
                return levelTable[i].level
            }
        }
        return 1
    }

    fun getLevelName(level: Int): String {
        return if (level > 20) "태양불꽃 (${level - 20}성)" 
        else levelTable.find { it.level == level }?.name ?: "작은 불씨"
    }
    
    fun getCharacterScale(level: Int, progress: Float): Float {
        // Formula: 0.5f + level * 0.08f + progress * 0.08f -- wait, user request says:
        // scale: 0.5f + level * 0.08f + progress * 0.08f
        // Let's verify with the provided table values.
        // Level 1: 0.58x (Which is 0.5 + 1*0.08)
        // Level 20: 2.10x (Which is 0.5 + 20*0.08 = 2.1)
        // So the base formula is BaseScale = 0.5 + (Level * 0.08).
        // Plus progress * 0.08 for smooth transition.
        
        return 0.5f + (level * 0.08f) + (progress * 0.08f)
    }

    // Helper for penalty logic: Get Min Exp for a level
    fun getMinExpForLevel(level: Int): Int {
        if (level <= 1) return 0
        if (level > 20) {
            val baseExpForLevel20 = levelTable.last().cumulativeExp
            return baseExpForLevel20 + ((level - 20) * 5000)
        }
        return levelTable.find { it.level == level }?.cumulativeExp ?: 0
    }
    
    // Helper for penalty logic: Get Max Exp for a level (which is Min of next level - 1 basically, or just the threshold)
    // Actually penalty logic says "Set to previous level's MAX experience"
    // Which means if I drop from Lv 10 to Lv 9, I should be at Lv 9's max progress (just before Lv 10).
    // Which is "Min Exp of Level 10 - 1".
    fun getMaxExpForLevel(level: Int): Int {
         if (level > 20) {
            val baseExpForLevel20 = levelTable.last().cumulativeExp
            return baseExpForLevel20 + ((level - 20 + 1) * 5000) - 1
        }
        val nextLevel = levelTable.find { it.level == level + 1 }
        return if (nextLevel != null) nextLevel.cumulativeExp - 1 else levelTable.last().cumulativeExp + 4999
    }
}
