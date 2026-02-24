package com.japygo.modakmodak

import android.app.Application
import com.japygo.modakmodak.data.ModakDatabase
import com.japygo.modakmodak.data.repository.ModakRepository
import com.japygo.modakmodak.data.repository.SettingsRepository
import com.japygo.modakmodak.utils.NotificationHelper
import com.japygo.modakmodak.utils.AsmrManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModakApplication : Application() {
    lateinit var database: ModakDatabase
    lateinit var repository: ModakRepository
    lateinit var settingsRepository: SettingsRepository
    lateinit var notificationHelper: NotificationHelper
    lateinit var asmrManager: AsmrManager

    override fun onCreate() {
        super.onCreate()
        database = ModakDatabase.getDatabase(this)
        settingsRepository = SettingsRepository(this)
        notificationHelper = NotificationHelper(this)
        asmrManager = AsmrManager(this)
        repository = ModakRepository(
            database.userDao(),
            database.studyLogDao(),
            database.shopDao(),
            database.inventoryDao(),
            database.timerPresetDao(),
            settingsRepository
        )
        // Ensure user exists
        CoroutineScope(Dispatchers.IO).launch {
            repository.createUserIfNotExists(seedLogs = false)
        }
        
        // Initialize AdMob
        com.japygo.modakmodak.utils.AdMobManager.initialize(this)
    }
    
    private fun setupWorkManager() {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.japygo.modakmodak.worker.NotificationWorker>(
            4, java.util.concurrent.TimeUnit.HOURS // Run checks every 4 hours
        ).build()

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ModakNotificationWorker",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
            workRequest
        )
    }
}
