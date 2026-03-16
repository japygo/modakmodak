package com.japygo.modakmodak

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.japygo.modakmodak.ui.theme.BackgroundDark
import com.japygo.modakmodak.ui.theme.ModakModakTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        val settingsRepository = com.japygo.modakmodak.data.repository.SettingsRepository(this)
        if (!isGranted) {
            // Permission Denied
            // User requested silent handling on first launch.
            // Just update the repository state.
            lifecycleScope.launch {
                settingsRepository.setNotificationEnabled(false)
            }
        } else {
             lifecycleScope.launch {
                settingsRepository.setNotificationEnabled(true)
            }
        }
    }



    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(
            com.japygo.modakmodak.utils.LocaleContextWrapper.wrap(
                newBase,
                kotlinx.coroutines.runBlocking {
                    com.japygo.modakmodak.data.repository.SettingsRepository(newBase).appLanguage.first()
                },
            ),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsRepository = com.japygo.modakmodak.data.repository.SettingsRepository(this)
        
        // Check for permission on launch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Check if we should specifically avoid asking (optional, but per request we just ask)
                // If the user has already permanently denied, this will immediately trigger the callback with false.
                // We might want to use a shared pref to track if "First Launch" logic is done to avoid nagging?
                // The prompt says "First execution: Request".
                // I will add a simplified check: If notification is explicitly disabled in settingsRepository, maybe don't ask? 
                // But the user might have disabled it because they were forced to.
                // For now, I'll follow the flow: Request if missing.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        lifecycleScope.launch {
            // Skip the initial emission to avoid immediate recreation loop if logic isn't perfect,
            // or better: compare with current configuration.
            settingsRepository.appLanguage.flowWithLifecycle(
                lifecycle,
                androidx.lifecycle.Lifecycle.State.STARTED,
            ).collect { language ->
                val currentLang = resources.configuration.locales[0].language
                if (currentLang != language) {
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
            }
        }

        enableEdgeToEdge(
        )
        val language = kotlinx.coroutines.runBlocking {
            settingsRepository.appLanguage.first()
        }

        setContent {
            val localizedContext = remember(language) {
                com.japygo.modakmodak.utils.LocaleContextWrapper.wrap(this@MainActivity, language)
            }

            CompositionLocalProvider(LocalContext provides localizedContext) {
                ModakModakTheme {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = BackgroundDark,
                    ) { innerPadding ->
                        // Avoiding unused innerPadding warning or using it if needed,
                        // but ModakNavGraph manages its own screens which have Scaffolds.
                        // We can pass it or just ignore it for the root host if we want full screen.
                        // Usually EdgeToEdge requires handling padding, but for now let's just host the graph.
                        // Adjusting to not apply padding to graph directly if child screens handle it,
                        // but `enableEdgeToEdge` usually means we need to handle system bars.
                        // Our HomeScreen has a Scaffold, so it should handle padding.
                        // Let's passed modifier with padding to be safe or handle in graph.
                        // For now, let's just call ModakNavGraph.
                        ModakNavGraph()
                    }
                }
            }
        }
        }



    override fun onDestroy() {
        super.onDestroy()
        com.japygo.modakmodak.utils.AdMobManager.cleanup(this)
    }
}