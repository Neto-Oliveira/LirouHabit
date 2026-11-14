package com.lirouhabit.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lirouhabit.app.ui.screens.AppsScreen
import com.lirouhabit.app.ui.screens.DreamsScreen
import com.lirouhabit.app.ui.screens.HomeScreen
import com.lirouhabit.app.ui.screens.OnboardingScreen
import com.lirouhabit.app.ui.screens.SettingsScreen
import com.lirouhabit.app.ui.screens.StatsScreen
import com.lirouhabit.app.ui.screens.VicesScreen
import com.lirouhabit.app.ui.vm.AppViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val DREAMS = "dreams"
    const val VICES = "vices"
    const val APPS = "apps"
    const val STATS = "stats"
    const val SETTINGS = "settings"
}

@Composable
fun AppNav(vm: AppViewModel) {
    val nav = rememberNavController()
    val onboardingDone by vm.onboardingDone.collectAsState()

    NavHost(
        navController = nav,
        startDestination = if (onboardingDone) Routes.HOME else Routes.ONBOARDING
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                vm = vm,
                onDone = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) { HomeScreen(vm = vm, nav = nav) }
        composable(Routes.DREAMS) { DreamsScreen(vm = vm, onBack = { nav.popBackStack() }) }
        composable(Routes.VICES) { VicesScreen(vm = vm, onBack = { nav.popBackStack() }) }
        composable(Routes.APPS) { AppsScreen(vm = vm, onBack = { nav.popBackStack() }) }
        composable(Routes.STATS) { StatsScreen(vm = vm, onBack = { nav.popBackStack() }) }
        composable(Routes.SETTINGS) { SettingsScreen(vm = vm, nav = nav, onBack = { nav.popBackStack() }) }
    }
}
