package com.japygo.modakmodak

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.japygo.modakmodak.ui.breaktime.BreakScreen
import com.japygo.modakmodak.ui.breaktime.BreakViewModel
import com.japygo.modakmodak.ui.focus.FocusScreen
import com.japygo.modakmodak.ui.focus.FocusViewModel
import com.japygo.modakmodak.ui.home.HomeScreen
import com.japygo.modakmodak.ui.home.HomeViewModel
import com.japygo.modakmodak.ui.settings.SettingsScreen
import com.japygo.modakmodak.ui.settings.SettingsViewModel
import com.japygo.modakmodak.ui.shop.BagScreen
import com.japygo.modakmodak.ui.shop.ShopScreen
import com.japygo.modakmodak.ui.shop.ShopViewModel
import com.japygo.modakmodak.ui.stats.StatsScreen
import com.japygo.modakmodak.ui.stats.StatsViewModel

@Composable
fun ModakNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val context = LocalContext.current
            val application = context.applicationContext as ModakApplication
            val repository = application.repository

            val viewModel: HomeViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        HomeViewModel(
                            repository,
                            application.settingsRepository,
                            application.notificationHelper
                        )
                    }
                },
            )
            HomeScreen(navController = navController, viewModel = viewModel)
        }
        composable("focus/{duration}/{tag}") { backStackEntry ->
            val duration = backStackEntry.arguments?.getString("duration")?.toIntOrNull() ?: 25
            val tag = backStackEntry.arguments?.getString("tag")?.let { Uri.decode(it) }
            val context = LocalContext.current
            val application = context.applicationContext as ModakApplication
            val repository = application.repository

            val viewModel: FocusViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        FocusViewModel(
                            repository,
                            application.settingsRepository,
                            application.notificationHelper,
                            application.asmrManager
                        )
                    }
                },
            )

            // 진입과 동시에 전달받은 시간으로 타이머 시작
            LaunchedEffect(Unit) {
                viewModel.startTimer(duration, tag)
            }

            FocusScreen(navController = navController, viewModel = viewModel)
        }
        composable("shop") {
            val context = LocalContext.current
            val application = context.applicationContext as ModakApplication
            val repository = application.repository

            // 상점 진입 시 광고 로드 확인
            LaunchedEffect(Unit) {
                com.japygo.modakmodak.utils.AdMobManager.loadRewardedAd(context, com.japygo.modakmodak.utils.AdMobManager.AdType.SHOP)
            }

            val viewModel: ShopViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ShopViewModel(repository)
                    }
                },
            )
            ShopScreen(navController = navController, viewModel = viewModel)
        }
        composable("bag") {
            val context = LocalContext.current
            val application = context.applicationContext as ModakApplication
            val repository = application.repository

            val viewModel: ShopViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ShopViewModel(repository)
                    }
                },
            )
            BagScreen(navController = navController, viewModel = viewModel)
        }
        composable("stats") {
            val context = LocalContext.current
            val application = context.applicationContext as ModakApplication
            val repository = application.repository

            val viewModel: StatsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        StatsViewModel(repository)
                    }
                },
            )
            StatsScreen(navController = navController, viewModel = viewModel)
        }
        composable("settings") {
            val context = LocalContext.current
            val application = context.applicationContext as ModakApplication
            val repository = application.repository
            val settingsRepository = application.settingsRepository

            val viewModel: SettingsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        SettingsViewModel(settingsRepository, repository)
                    }
                },
            )
            SettingsScreen(navController = navController, viewModel = viewModel)
        }

        composable("break/{studyDuration}/{earnedCoins}/{earnedExp}/{streakDays}") { backStackEntry ->
            val studyDuration = backStackEntry.arguments?.getString("studyDuration")?.toIntOrNull() ?: 0
            val earnedCoins = backStackEntry.arguments?.getString("earnedCoins")?.toIntOrNull() ?: 0
            val earnedExp = backStackEntry.arguments?.getString("earnedExp")?.toIntOrNull() ?: 0
            val streakDays = backStackEntry.arguments?.getString("streakDays")?.toIntOrNull() ?: 0
            
            val context = LocalContext.current
            val application = context.applicationContext as ModakApplication

            val viewModel: BreakViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        BreakViewModel(
                            application.settingsRepository,
                            application.notificationHelper,
                        )
                    }
                },
            )

            // 학습 시간 설정
            LaunchedEffect(Unit) {
                viewModel.setStudyDuration(studyDuration)
            }

            BreakScreen(
                navController = navController, 
                viewModel = viewModel,
                earnedCoins = earnedCoins,
                earnedExp = earnedExp,
                streakDays = streakDays
            )
        }
        composable("reward/{earnedCoins}/{earnedExp}/{duration}/{streakDays}/{isBreakEnabled}") { backStackEntry ->
            val earnedCoins = backStackEntry.arguments?.getString("earnedCoins")?.toIntOrNull() ?: 0
            val earnedExp = backStackEntry.arguments?.getString("earnedExp")?.toIntOrNull() ?: 0
            val duration = backStackEntry.arguments?.getString("duration")?.toIntOrNull() ?: 0
            val streakDays = backStackEntry.arguments?.getString("streakDays")?.toIntOrNull() ?: 0
            val isBreakEnabled = backStackEntry.arguments?.getString("isBreakEnabled")?.toBoolean() ?: true
            
            val context = LocalContext.current
            val application = context.applicationContext as ModakApplication
            val repository = application.repository
            
            val viewModel: FocusViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        FocusViewModel(
                            repository,
                            application.settingsRepository,
                            application.notificationHelper,
                            application.asmrManager
                        )
                    }
                },
            )
            
            com.japygo.modakmodak.ui.focus.RewardScreen(
                navController = navController, 
                viewModel = viewModel,
                earnedCoins = earnedCoins,
                earnedExp = earnedExp,
                duration = duration,
                streakDays = streakDays,
                isBreakEnabled = isBreakEnabled
            )
        }
    }
}
