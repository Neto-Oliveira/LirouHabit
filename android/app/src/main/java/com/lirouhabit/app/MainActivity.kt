package com.lirouhabit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lirouhabit.app.ui.AppNav
import com.lirouhabit.app.ui.LirouHabitTheme
import com.lirouhabit.app.ui.vm.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LirouHabitTheme {
                val vm: AppViewModel = viewModel()
                AppNav(vm)
            }
        }
    }
}
