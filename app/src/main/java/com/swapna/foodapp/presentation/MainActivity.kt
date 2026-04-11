package com.swapna.foodapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.swapna.foodapp.data.auth.ActivityProvider
import com.swapna.foodapp.presentation.navigation.AppNavGraph
import com.swapna.foodapp.presentation.ui.theme.FoodAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityProvider.setActivity(this)
        // ✅ Do NOT call setContent here
        // Test will call activity.setContent() instead
        // Production nav graph set via setContent below
        setContent {
            FoodAppTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityProvider.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        activityProvider.clearActivity()
    }
}