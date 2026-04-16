package com.swapna.foodapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.swapna.foodapp.presentation.auth.AuthViewModel
import com.swapna.foodapp.presentation.auth.LoginScreen
import com.swapna.foodapp.presentation.cart.CartScreen
import com.swapna.foodapp.presentation.home.HomeScreen
import com.swapna.foodapp.presentation.product.ProductDetailScreen
import com.swapna.foodapp.presentation.profile.ProfileScreen
import com.swapna.foodapp.presentation.restaurant.RestaurantScreen
import com.swapna.foodapp.presentation.search.SearchScreen
import com.swapna.foodapp.presentation.splash.SplashScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = AppRoutes.SPLASH,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(AppRoutes.SPLASH) {
            SplashScreen(navController)
        }

        composable(AppRoutes.LOGIN) {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                viewModel = viewModel,
            )
        }
        composable(AppRoutes.HOME) {
            HomeScreen(navController)
        }

        composable(AppRoutes.SEARCH) {
            SearchScreen(navController)
        }

        composable(
            route = AppRoutes.SEARCH_WITH_QUERY,
            arguments = listOf(
                navArgument(AppRoutes.ARG_SEARCH_QUERY) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments
                ?.getString(AppRoutes.ARG_SEARCH_QUERY) ?: ""
            SearchScreen(
                navController = navController,
                initialQuery = query,   // ← pre-fill search with category name
            )
        }

        composable(AppRoutes.CART) {
            CartScreen(navController)
        }

        composable(AppRoutes.PROFILE) {
            ProfileScreen(navController)
        }

        // Restaurant screen receives restaurantId
        // Called via: navController.navigate(AppRoutes.restaurant("101"))
        // Verify this exists in AppNavGraph.kt
        composable(
            route = AppRoutes.RESTAURANT,  // "restaurant/{restaurantId}"
            arguments = listOf(
                navArgument(AppRoutes.ARG_RESTAURANT_ID) {
                    type     = NavType.StringType
                    nullable = false
                }
            ),
        ) { backStackEntry ->
            RestaurantScreen(navController = navController)
        }

        // Product screen receives menuItemId
        // Called via: navController.navigate(AppRoutes.product("d001"))
        composable(
            route = AppRoutes.PRODUCT,        // "product/{menuItemId}"
            arguments = listOf(
                navArgument(AppRoutes.ARG_RESTAURANT_ID) {
                    type = NavType.StringType
                },
                navArgument(AppRoutes.ARG_MENU_ITEM_ID) {
                    type = NavType.StringType
                },
            ),
        ) {
            ProductDetailScreen(navController)
        }

        composable(AppRoutes.PROFILE) {
            ProfileScreen(navController = navController)
        }


    }
}