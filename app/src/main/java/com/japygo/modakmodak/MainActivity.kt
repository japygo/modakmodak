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
import androidx.compose.ui.Modifier
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
        // Handle result
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Observe language changes to recreate activity
        val settingsRepository = com.japygo.modakmodak.data.repository.SettingsRepository(this)
        lifecycleScope.launch {
            // Skip the initial emission to avoid immediate recreation loop if logic isn't perfect,
            // or better: compare with current configuration.
            settingsRepository.appLanguage
                .flowWithLifecycle(lifecycle, androidx.lifecycle.Lifecycle.State.STARTED)
                .collect { language ->
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
        setContent {
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