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
import com.lirouhabit.app.ui.screens.IntroAppsScreen
import com.lirouhabit.app.ui.screens.IntroDreamScreen
import com.lirouhabit.app.ui.screens.IntroScreen
import com.lirouhabit.app.ui.screens.OnboardingScreen
import com.lirouhabit.app.ui.screens.SettingsScreen
import com.lirouhabit.app.ui.screens.StatsScreen
import com.lirouhabit.app.ui.screens.VicesScreen
import com.lirouhabit.app.ui.vm.AppViewModel

object Routes {
    const val INTRO = "intro"
    const val INTRO_DREAM = "intro_dream"
    const val INTRO_APPS = "intro_apps"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val DREAMS = "dreams"
    const val VICES = "vices"
    const val APPS = "apps"
    const val STATS = "stats"
    const val SETTINGS = "settings"
}

/**
 * Fluxo de primeiro uso: apresentação de impacto → maior sonho → apps vilões →
 * permissões → home. Depois disso, sempre direto na home.
 */
@Composable
fun AppNav(vm: AppViewModel) {
    val nav = rememberNavController()
    val onboardingDone by vm.onboardingDone.collectAsState()

    NavHost(
        navController = nav,
        startDestination = if (onboardingDone) Routes.HOME else Routes.INTRO
    ) {
        composable(Routes.INTRO) {
            IntroScreen(onNext = { nav.navigate(Routes.INTRO_DREAM) })
        }
        composable(Routes.INTRO_DREAM) {
            IntroDreamScreen(vm = vm, onNext = { nav.navigate(Routes.INTRO_APPS) })
        }
        composable(Routes.INTRO_APPS) {
            IntroAppsScreen(vm = vm, onNext = { nav.navigate(Routes.ONBOARDING) })
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                vm = vm,
                onDone = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
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
