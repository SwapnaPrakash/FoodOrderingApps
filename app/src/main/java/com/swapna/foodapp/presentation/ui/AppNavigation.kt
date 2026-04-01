package com.swapna.foodapp.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.swapna.foodapp.presentation.ui.Route.HomeScreen
import com.swapna.foodapp.presentation.ui.home.HomeScreen
import com.swapna.foodapp.utils.AppConstant

sealed class Route(val name:String){
    object HomeScreen : Route(AppConstant.HOME)
}

@Composable
fun FoodAppUI() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.HomeScreen.name){
        composable (route = Route.HomeScreen.name){
            HomeScreen()
        }
    }

}