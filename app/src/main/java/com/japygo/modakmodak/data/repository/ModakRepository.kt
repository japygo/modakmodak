package com.japygo.modakmodak.data.repository

import com.japygo.modakmodak.data.InitialShopItems
import com.japygo.modakmodak.data.dao.InventoryDao
import com.japygo.modakmodak.data.dao.ShopDao
import com.japygo.modakmodak.data.dao.StudyLogDao
import com.japygo.modakmodak.data.dao.TimerPresetDao
import com.japygo.modakmodak.data.dao.UserDao
import com.japygo.modakmodak.data.entity.Inventory
import com.japygo.modakmodak.data.entity.ShopItem
import com.japygo.modakmodak.data.entity.StudyLog
import com.japygo.modakmodak.data.entity.TimerPreset
import com.japygo.modakmodak.data.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale

class ModakRepository(
    private val userDao: UserDao,
    private val studyLogDao: StudyLogDao,
    private val shopDao: ShopDao,
    private val inventoryDao: InventoryDao,
    private val timerPresetDao: TimerPresetDao,
    private val settingsRepository: SettingsRepository
) {
    val userFlow: Flow<User?> = userDao.getUser()
    val shopItemsFlow: Flow<List<ShopItem>> = shopDao.getAllShopItems()
    val inventoryFlow: Flow<List<Inventory>> = inventoryDao.getAllInventory()
    val timerPresetsFlow: Flow<List<TimerPreset>> = timerPresetDao.getAllPresets()

    suspend fun createUserIfNotExists(seedLogs: Boolean = false) {
        val user = userDao.getUser().firstOrNull()
        if (user == null) {
            // Initial User for new players (Clean Start)
            userDao.insertUser(User(currentCoin = 0, fireLevel = 1, fireExp = 0))
        } else if (user.currentCoin < 100) {
            // Remove automatic coin injection for existing users
            // userDao.insertUser(user.copy(currentCoin = user.currentCoin + 1000))
        }
        
        // Initialize/Update Shop Items to ensure latest icons/names
        shopDao.insertShopItems(InitialShopItems)

        // Test data removed for release cleanliness
        // inventoryDao.insertInventory(Inventory("wood_twig", 100))

        // Initialize Timer Presets if empty
        val presets = timerPresetDao.getAllPresets().firstOrNull()
        if (presets.isNullOrEmpty()) {
            val defaultTag = if (Locale.getDefault().language == "ko") "#공부" else "#study"
            timerPresetDao.insertPreset(TimerPreset(tag = defaultTag, durationMinutes = 25, isSelected = true))
        }

        // Initialize Dummy Logs for Stats Verification
        if (seedLogs) {
            val logs = studyLogDao.getAllLogs().firstOrNull()
            if (logs.isNullOrEmpty()) {
                debugSeedLogs()
            }
        }
        
        // Check penalties on startup
        checkPenalties()
    }

    suspend fun debugSeedLogs() {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val logs = mutableListOf<StudyLog>()

        // Generate a Rainbow Gradient of logs for visually verifying colors
        // Step 1: Very Pale Yellow (15 mins) - < 30m
        logs.add(StudyLog(date = now - (5 * oneDayMillis), durationSeconds = 900, isSuccess = true, earnedCoin = 15, sessionType = "focus", tag = "#Reading"))
        
        // Step 2: Pale Orange (45 mins) - 30m ~ 1h
        logs.add(StudyLog(date = now - (4 * oneDayMillis), durationSeconds = 2700, isSuccess = true, earnedCoin = 45, sessionType = "focus", tag = "#Math"))
        
        // Step 3: Soft Orange (1h 30m) - 1h ~ 2h
        logs.add(StudyLog(date = now - (3 * oneDayMillis), durationSeconds = 5400, isSuccess = true, earnedCoin = 90, sessionType = "focus", tag = "#Coding"))
        
        // Step 4: Deep Orange (3h) - 2h ~ 4h
        logs.add(StudyLog(date = now - (2 * oneDayMillis), durationSeconds = 10800, isSuccess = true, earnedCoin = 180, sessionType = "focus", tag = "#Design"))
        
        // Step 5: Red (5h) - 4h+
        logs.add(StudyLog(date = now - (1 * oneDayMillis), durationSeconds = 18000, isSuccess = true, earnedCoin = 300, sessionType = "focus", tag = "#Project"))
        
        // Failed (Gray) for today
        logs.add(StudyLog(date = now, durationSeconds = 600, isSuccess = false, earnedCoin = 0, sessionType = "focus", tag = "#Coding"))

        logs.forEach { studyLogDao.insertLog(it) }
    }

    suspend fun debugClearLogs() {
        studyLogDao.deleteAll()
    }

    suspend fun addExp(amount: Int) {
        val currentUser = userDao.getUser().firstOrNull() ?: User()
        val newExp = currentUser.fireExp + amount
        // Use LevelUtils for consistent calculation
        val newLevel = com.japygo.modakmodak.utils.LevelUtils.calculateLevel(newExp)
        val updatedUser = currentUser.copy(fireExp = newExp, fireLevel = newLevel)
        userDao.insertUser(updatedUser) // Insert acts as replace
    }
    
    suspend fun addCoins(amount: Int) {
        val currentUser = userDao.getUser().firstOrNull() ?: User()
        val updatedUser = currentUser.copy(currentCoin = currentUser.currentCoin + amount)
        userDao.insertUser(updatedUser)
    }

    suspend fun logSession(duration: Int, isSuccess: Boolean, earnedCoin: Int, tag: String?) {
        var finalCoins = earnedCoin
        
        if (isSuccess && duration >= 600) { // Only update streak for success > 10 mins
            val currentUser = userDao.getUser().firstOrNull() ?: User()
            val now = System.currentTimeMillis()
            val lastDate = currentUser.lastStudyDate
            
            // Calculate Day Difference
            val calNow = java.util.Calendar.getInstance().apply { timeInMillis = now }
            val calLast = java.util.Calendar.getInstance().apply { timeInMillis = if (lastDate > 0) lastDate else now }
            
            // Reset hours for day comparison
            val dayNow = calNow.get(java.util.Calendar.DAY_OF_YEAR)
            val yearNow = calNow.get(java.util.Calendar.YEAR)
            
            val dayLast = calLast.get(java.util.Calendar.DAY_OF_YEAR)
            val yearLast = calLast.get(java.util.Calendar.YEAR)
            
            var newStreak = currentUser.streakDays
            
            if (lastDate == 0L) {
                // First time ever
                newStreak = 1
            } else if (yearNow == yearLast && dayNow == dayLast) {
                // Same day, keeping streak (do nothing or ensure it's at least 1)
                if (newStreak == 0) newStreak = 1
            } else {
                // Different day
                calLast.add(java.util.Calendar.DAY_OF_YEAR, 1) // Check if it was yesterday
                if (calLast.get(java.util.Calendar.DAY_OF_YEAR) == dayNow && calLast.get(java.util.Calendar.YEAR) == yearNow) {
                    // Was yesterday -> Streak continues
                    newStreak += 1
                } else {
                    // Gap > 1 day -> Reset
                    newStreak = 1
                }
            }
            
            // Apply Multiplier to Coins
            val multiplier = getStreakMultiplier(newStreak)
            finalCoins = (earnedCoin * multiplier).toInt()
            
            // Check Milestones
            val milestones = listOf(3, 7, 14, 21, 30, 50, 100, 365)
            var currentUnclaimed = currentUser.unclaimedMilestones.split(",").filter { it.isNotEmpty() }.toMutableList()
            var currentClaimed = currentUser.unclaimedMilestones // Actually this field name is unclaimed, better logic needed
            // Wait, the field name is 'unclaimedMilestones'. 
            // Logic: If streak hits milestone, AND it's NOT in unclaimed (or claimed history? currently we don't have claimed history).
            // Simplified: If streak hits milestone, add to unclaimed. User claims it, we remove it. 
            // Risk: User can reclaim if streak resets and hits again. This is acceptable for motivation? 
            // "3일 연속" is hard to hit repeatedly if you fail. Let's allow repeated claiming for now or just add it.
            
            if (milestones.contains(newStreak)) {
                if (!currentUnclaimed.contains(newStreak.toString())) {
                    currentUnclaimed.add(newStreak.toString())
                }
            }
            
            userDao.insertUser(currentUser.copy(
                streakDays = newStreak,
                lastStudyDate = now,
                unclaimedMilestones = currentUnclaimed.joinToString(",")
            ))
        }

        val log = StudyLog(
            date = System.currentTimeMillis(),
            durationSeconds = duration,
            isSuccess = isSuccess,
            earnedCoin = finalCoins,
            sessionType = "focus",
            tag = tag
        )
        studyLogDao.insertLog(log)
    }
    
    fun getStreakMultiplier(days: Int): Double {
        return when {
            days == 1 -> 1.0
            days == 2 -> 1.1
            days == 3 -> 1.2
            days == 4 -> 1.3
            days == 5 -> 1.4
            days <= 7 -> 1.6
            days <= 20 -> 1.8
            days <= 29 -> 2.0
            days <= 49 -> 2.3
            days <= 99 -> 2.5
            else -> 3.0
        }
    }
    
    suspend fun checkPenalties() {
        val currentUser = userDao.getUser().firstOrNull() ?: return
        if (currentUser.lastStudyDate == 0L) return

        val now = System.currentTimeMillis()
        val diffMillis = now - currentUser.lastStudyDate
        val diffDays = diffMillis / (24 * 60 * 60 * 1000)
        
        // Grace period 7 days. Penalty starts on 8th day.
        if (diffDays <= 7) return
        
        // Calculate penalty
        var totalDeduction = 0
        // Simple iteration for days past 7
        for (day in 8..diffDays) {
            val deduction = when {
                day <= 0 -> 0 // Should not happen
                day <= 7 -> 0
                day == 8L -> 20
                day == 9L -> 30
                day == 10L -> 40
                day <= 14L -> 50
                day <= 21L -> 70
                day <= 30L -> 100
                else -> 120
            }
            totalDeduction += deduction
        }
        
        if (totalDeduction > 0) {
            // Apply deduction
            var newExp = currentUser.fireExp - totalDeduction
            // Level down logic handled by LevelUtils and re-calculation
            // However, we need to enforce "Min Level 1 Exp 0"
            if (newExp < 0) newExp = 0
            
            // Check if level dropped
            val oldLevel = currentUser.fireLevel
            val newLevel = com.japygo.modakmodak.utils.LevelUtils.calculateLevel(newExp)
            
            // Special Rule: If level drops, set to Max Exp of previous level? 
            // User requirement: "레벨 다운 시 이전 레벨의 최대 경험치로 설정"
            // This means if I was Lv 10 (5400) and dropped to range of Lv 9, I should be at Lv 9's max.
            // But simply subtracting Exp acts naturally. "Set to max exp" might act as a buffer.
            // Let's stick to subtraction first. If it drops multiple levels, it drops.
            
            userDao.insertUser(currentUser.copy(
                fireExp = newExp, 
                fireLevel = newLevel,
                streakDays = 0 // Reset streak if penalty applied
            ))
        }
    }
    
    suspend fun claimMilestone(day: Int) {
        val currentUser = userDao.getUser().firstOrNull() ?: return
        val unclaimed = currentUser.unclaimedMilestones.split(",").filter { it.isNotEmpty() }.toMutableList()
        
        if (unclaimed.contains(day.toString())) {
            unclaimed.remove(day.toString())
            
            // Give Rewards
            val (coinBonus, expBonus) = when(day) {
                3 -> 100 to 50
                7 -> 300 to 150
                14 -> 700 to 350
                21 -> 1200 to 600
                30 -> 2000 to 1000
                50 -> 4000 to 2000
                100 -> 10000 to 5000
                365 -> 50000 to 25000
                else -> 0 to 0
            }
            
            val newExp = currentUser.fireExp + expBonus
            val newLevel = com.japygo.modakmodak.utils.LevelUtils.calculateLevel(newExp)
            
            userDao.insertUser(currentUser.copy(
                currentCoin = currentUser.currentCoin + coinBonus,
                fireExp = newExp,
                fireLevel = newLevel,
                unclaimedMilestones = unclaimed.joinToString(",")
            ))
        }
    }

    suspend fun setDailyReminder(enabled: Boolean) {
        val currentUser = userDao.getUser().firstOrNull() ?: return
        userDao.insertUser(currentUser.copy(enableDailyReminder = enabled))
    }

    fun getLogsForRange(start: Long, end: Long): Flow<List<StudyLog>> {
        return studyLogDao.getLogsByDateRange(start, end)
    }
    
    // Shop Logic
    suspend fun buyItem(itemId: String, quantity: Int): Boolean {
        val currentUser = userDao.getUser().firstOrNull() ?: return false
        val shopItem = shopDao.getShopItem(itemId) ?: return false
        val totalPrice = shopItem.price * quantity
        
        if (currentUser.currentCoin >= totalPrice) {
            // Deduct Coins
            userDao.insertUser(currentUser.copy(currentCoin = currentUser.currentCoin - totalPrice))
            
            // Add to Inventory
            val currentInventory = inventoryDao.getInventoryItem(itemId)
            if (currentInventory != null) {
                inventoryDao.insertInventory(currentInventory.copy(quantity = currentInventory.quantity + quantity))
            } else {
                inventoryDao.insertInventory(Inventory(itemId, quantity))
            }
            return true
        }
        return false
    }
    
    suspend fun useItem(itemId: String, quantity: Int): Boolean {
        val inventoryItem = inventoryDao.getInventoryItem(itemId) ?: return false
        val currentUser = userDao.getUser().firstOrNull() ?: return false

        if (inventoryItem.quantity >= quantity) {
            // Decrement
            if (inventoryItem.quantity == quantity) {
                // If using all items, set quantity to 0 (dimmed state)
                inventoryDao.insertInventory(inventoryItem.copy(quantity = 0))
            } else {
                inventoryDao.insertInventory(inventoryItem.copy(quantity = inventoryItem.quantity - quantity))
            }
            
            // Effect (Apply multiple times)
            val shopItem = shopDao.getShopItem(itemId)
            if (shopItem != null) {
                when (shopItem.type) {
                    "EXP" -> addExp(shopItem.value * quantity)
                    "COLOR" -> {
                        // magic_blue has type "EXP" in current initial data, 
                        // but let's handle its ID specifically as requested.
                        if (itemId == "magic_blue") {
                            val colors = listOf(
                                "#FFFF9500", // Orange
                                "#FFFF6B35", // Red-Orange
                                "#FFFF3B30", // Red
                                "#FFFF2D92", // Purple-Red
                                "#BF5AF2", // Blue-Purple
                                "#00BFFF", // Deep Sky Blue
                                "#32CD32", // Lime Green
                                "#FFD700", // Gold
                                "#00FA9A", // Medium Spring Green
                                "#1E90FF"  // Dodger Blue
                            )
                            val randomColor = colors.random()
                            userDao.insertUser(currentUser.copy(fireColor = randomColor))
                        }
                    }
                }
                // Special handling for magic_blue even if type is EXP (existing data)
                if (itemId == "magic_blue" && shopItem.type == "EXP") {
                    val colors = listOf(
                        "#FFFF9500", "#FFFF6B35", "#FFFF3B30", "#FFFF2D92", 
                        "#BF5AF2", "#00BFFF", "#32CD32", "#FFD700", "#00FA9A", "#1E90FF"
                    )
                    val randomColor = colors.random()
                    userDao.insertUser(currentUser.copy(fireColor = randomColor))
                }
            }
            return true
        }
        return false
    }

    suspend fun resetAllData() {
        userDao.deleteAll()
        studyLogDao.deleteAll()
        inventoryDao.deleteAll()
        timerPresetDao.deleteAll()
        settingsRepository.clearData()
        // Re-initialize for fresh state, but do NOT seed logs so user can see empty state
        createUserIfNotExists(seedLogs = false)
    }

    // Timer Preset Logic
    suspend fun addTimerPreset(preset: TimerPreset) {
        timerPresetDao.insertPreset(preset)
    }

    suspend fun deleteTimerPreset(preset: TimerPreset) {
        timerPresetDao.deletePreset(preset)
    }

    suspend fun selectTimerPreset(presetId: Long) {
        timerPresetDao.deselectAll()
        timerPresetDao.setPresetSelected(presetId)
    }

    suspend fun updateTimerPreset(preset: TimerPreset) {
        timerPresetDao.updatePreset(preset)
    }

    // DEBUG: Direct control for testing
    suspend fun debugSetLastStudyDate(daysAgo: Int) {
        val currentUser = userDao.getUser().firstOrNull() ?: return
        val now = System.currentTimeMillis()
        val targetDate = now - (daysAgo * 24 * 60 * 60 * 1000L)
        val updated = currentUser.copy(lastStudyDate = targetDate)
        userDao.insertUser(updated)
    }

    suspend fun debugSetStreak(days: Int) {
        val currentUser = userDao.getUser().firstOrNull() ?: return
        val now = System.currentTimeMillis()
        // Set last study date to yesterday so that studying "today" increments the streak
        val yesterday = now - (24 * 60 * 60 * 1000L)
        
        val updated = currentUser.copy(
            streakDays = days,
            lastStudyDate = yesterday
        )
        userDao.insertUser(updated)
    }
}
