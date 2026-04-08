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
       //clear activityProvider.setActivity(this)
        setContent {
            FoodAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    // Entry point — all navigation starts here
                    AppNavGraph(navController = navController)

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register Activity for Firebase reCAPTCHA
        activityProvider.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        // Clear to avoid memory leak
        activityProvider.clearActivity()
    }
}