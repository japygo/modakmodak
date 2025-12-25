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
    private val timerPresetDao: TimerPresetDao
) {
    val userFlow: Flow<User?> = userDao.getUser()
    val shopItemsFlow: Flow<List<ShopItem>> = shopDao.getAllShopItems()
    val inventoryFlow: Flow<List<Inventory>> = inventoryDao.getAllInventory()
    val timerPresetsFlow: Flow<List<TimerPreset>> = timerPresetDao.getAllPresets()

    suspend fun createUserIfNotExists(seedLogs: Boolean = true) {
        val user = userDao.getUser().firstOrNull()
        if (user == null) {
            // Initial User for new players (with test coins for shop testing)
            userDao.insertUser(User(currentCoin = 1000, fireLevel = 1, fireExp = 0))
        } else if (user.currentCoin < 100) {
            // Add test coins to existing users for shop testing
            userDao.insertUser(user.copy(currentCoin = user.currentCoin + 1000))
        }
        
        // Initialize/Update Shop Items to ensure latest icons/names
        shopDao.insertShopItems(InitialShopItems)

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
                seedDummyLogs()
            }
        }
    }

    private suspend fun seedDummyLogs() {
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

    suspend fun addExp(amount: Int) {
        val currentUser = userDao.getUser().firstOrNull() ?: User()
        val newExp = currentUser.fireExp + amount
        val newLevel = calculateFireLevel(newExp)
        val updatedUser = currentUser.copy(fireExp = newExp, fireLevel = newLevel)
        userDao.insertUser(updatedUser) // Insert acts as replace
    }
    
    suspend fun addCoins(amount: Int) {
        val currentUser = userDao.getUser().firstOrNull() ?: User()
        val updatedUser = currentUser.copy(currentCoin = currentUser.currentCoin + amount)
        userDao.insertUser(updatedUser)
    }

    suspend fun logSession(duration: Int, isSuccess: Boolean, earnedCoin: Int, tag: String?) {
        val log = StudyLog(
            date = System.currentTimeMillis(),
            durationSeconds = duration,
            isSuccess = isSuccess,
            earnedCoin = earnedCoin,
            sessionType = "focus",
            tag = tag
        )
        studyLogDao.insertLog(log)
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
            if (shopItem != null && shopItem.type == "EXP") {
                addExp(shopItem.value * quantity)
            }
            return true
        }
        return false
    }

    private fun calculateFireLevel(exp: Int): Int {
        return when {
            exp < 300 -> 1 // Matchstick
            exp < 1000 -> 2 // Candle
            exp < 3000 -> 3 // Torch
            exp < 8000 -> 4 // Bonfire
            else -> 5 // Campfire
        }
    }

    suspend fun resetAllData() {
        userDao.deleteAll()
        studyLogDao.deleteAll()
        inventoryDao.deleteAll()
        timerPresetDao.deleteAll()
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
}
