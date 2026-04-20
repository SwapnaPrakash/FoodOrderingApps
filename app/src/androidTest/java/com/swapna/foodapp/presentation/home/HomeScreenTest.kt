package com.swapna.foodapp.presentation.home

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.fakes.FakeCartRepository
import com.swapna.foodapp.fakes.FakeConnectivityObserver
import com.swapna.foodapp.fakes.FakeLocationManager
import com.swapna.foodapp.fakes.FakeRestaurantRepository
import com.swapna.foodapp.fakes.FakeUserRepository
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.HomeData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.swapna.foodapp.utils.AppConstants.RETRY
import com.swapna.foodapp.utils.AppConstants.TRY_AGAIN
import com.swapna.foodapp.utils.AppConstants.WRONG

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule(order = 0)
    val screenOnRule = object : org.junit.rules.ExternalResource() {
        override fun before() {
            androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation
                .executeShellCommand("input keyevent KEYCODE_WAKEUP")

            androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation
                .executeShellCommand("wm dismiss-keyguard")
        }
    }

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var fakeRestaurantRepo: FakeRestaurantRepository
    private lateinit var fakeCartRepo:       FakeCartRepository
    private lateinit var fakeUserRepo:       FakeUserRepository
    private lateinit var fakeConnectivity:   FakeConnectivityObserver
    private lateinit var fakeLocationMgr:    FakeLocationManager

    @Before
    fun setUp() {
        fakeRestaurantRepo = FakeRestaurantRepository()
        fakeCartRepo       = FakeCartRepository()
        fakeUserRepo       = FakeUserRepository()
        fakeConnectivity   = FakeConnectivityObserver()
        fakeLocationMgr    = FakeLocationManager()
    }

    // WHY two buildViewModel variants?
    // buildViewModel()      → uses real GetHomeDataUseCase + FakeRestaurantRepo
    //                          for happy path tests (restaurants, categories etc.)
    // buildViewModelError() → uses mockk on GetHomeDataUseCase directly
    //                          for error tests — bypasses use case combine() logic
    //                          which swallows Result.failure via getOrDefault()
    private fun buildViewModel() = HomeViewModel(
        getHomeDataUseCase   = GetHomeDataUseCase(fakeRestaurantRepo),
        cartRepository       = fakeCartRepo,
        userRepository       = fakeUserRepo,
        connectivityObserver = fakeConnectivity.mock,
        locationManager      = fakeLocationMgr.mock,
    )

    // ✅ FIX: Mock GetHomeDataUseCase to emit Result.failure directly
    // WHY not Result.failure from FakeRestaurantRepository?
    // GetHomeDataUseCase.combine() calls getOrDefault(emptyList()) on each result
    // Result.failure → getOrDefault → emptyList → Result.success(HomeData)
    // Error state in HomeViewModel is NEVER reached this way
    // Mocking the use case skips combine() entirely → directly emits failure
    private fun buildViewModelWithError(message: String): HomeViewModel {
        val useCase = mockk<GetHomeDataUseCase>()
        every { useCase(any()) } returns flowOf(
            Result.failure(Exception(message))
        )
        return HomeViewModel(
            getHomeDataUseCase   = useCase,
            cartRepository       = fakeCartRepo,
            userRepository       = fakeUserRepo,
            connectivityObserver = fakeConnectivity.mock,
            locationManager      = fakeLocationMgr.mock,
        )
    }

    private fun buildViewModelRetryable(): Pair<HomeViewModel, GetHomeDataUseCase> {
        var callCount = 0
        val useCase = mockk<GetHomeDataUseCase>()

        every { useCase(any()) } answers {
            callCount++
            if (callCount == 1) {
                flowOf(Result.failure(Exception("Network error")))
            } else {
                flowOf(Result.success(
                    HomeData(
                        collections    = emptyList(),
                        categories     = emptyList(),
                        restaurants    = listOf(fakeRestaurant("r1", "Meghana Foods")),
                        filterStatus   = FilterStatus.NO_FILTER,
                        requestedArea  = "",
                        availableAreas = emptyList(),
                    )
                ))
            }
        }

        val vm = HomeViewModel(
            getHomeDataUseCase   = useCase,
            cartRepository       = fakeCartRepo,
            userRepository       = fakeUserRepo,
            connectivityObserver = fakeConnectivity.mock,
            locationManager      = fakeLocationMgr.mock,
        )
        return vm to useCase
    }

    private fun setContent(vm: HomeViewModel = buildViewModel()) {
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    navController = rememberNavController(),
                    viewModel     = vm,
                )
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — TopBar
    // ══════════════════════════════════════════════════════════

    @Test
    fun homeScreen_deliveringTo_label_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(AppConstants.DELIVERING_TO)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_defaultLocation_isDisplayed_in_topBar() {
        setContent()
        composeTestRule
            .onNodeWithText(AppConstants.DEFAULT_LOCATION)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_searchHint_isDisplayed_in_topBar() {
        setContent()
        composeTestRule
            .onNodeWithText(AppConstants.SEARCH_HINT)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_cartIcon_isDisplayed_in_topBar() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(AppConstants.GO_TO_CART)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_locationIcon_isDisplayed_in_topBar() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(AppConstants.LOCATION_ICON_DESC)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Cart Badge
    // ══════════════════════════════════════════════════════════

    @Test
    fun homeScreen_cartBadge_not_shown_when_cart_empty() {
        setContent()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun homeScreen_cartBadge_shows_3_when_3_items_in_cart() {
        fakeCartRepo.setItemCount(3)
        setContent()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun homeScreen_cartBadge_shows_1_when_one_item_in_cart() {
        fakeCartRepo.setItemCount(1)
        setContent()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Restaurants Section
    // ══════════════════════════════════════════════════════════

    @Test
    fun homeScreen_restaurantsNearYou_title_isDisplayed() {
        fakeRestaurantRepo.nearbyRestaurantsResult =
            Result.success(listOf(fakeRestaurant("r1", "Meghana Foods")))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Restaurants Near You")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_restaurantName_isDisplayed() {
        fakeRestaurantRepo.nearbyRestaurantsResult =
            Result.success(listOf(fakeRestaurant("r1", "Meghana Foods")))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Meghana Foods").assertIsDisplayed()
    }

    @Test
    fun homeScreen_multiple_restaurants_all_names_shown() {
        fakeRestaurantRepo.nearbyRestaurantsResult = Result.success(listOf(
            fakeRestaurant("r1", "Meghana Foods"),
            fakeRestaurant("r2", "Pizza Hut"),
        ))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Meghana Foods").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pizza Hut").assertIsDisplayed()
    }

    @Test
    fun homeScreen_notServiceable_message_shown_for_non_service_area() {
        // DEFAULT_LOCATION with empty repo triggers NOT_SERVICEABLE naturally
        // This is the state we saw in the semantic tree
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("not in", substring = true, ignoreCase = true)
            .assertIsDisplayed()

        // Change Delivery Location button shown
        composeTestRule
            .onNodeWithText("Change Delivery Location")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_empty_restaurants_shows_no_restaurants_found() {
        // WHY mock use case here?
        // buildViewModel() uses real GetHomeDataUseCase with DEFAULT_LOCATION
        // DEFAULT_LOCATION = "Koramangala, Bengaluru" → filter finds no restaurants
        // → FilterStatus.NOT_SERVICEABLE → "We're not in Koramangala yet" shown
        // NOT EmptyRestaurantsCard
        // We need FilterStatus.NO_FILTER + empty list to trigger EmptyRestaurantsCard
        val useCase = mockk<GetHomeDataUseCase>()
        every { useCase(any()) } returns flowOf(
            Result.success(
                HomeData(
                    collections    = emptyList(),
                    categories     = emptyList(),
                    restaurants    = emptyList(),           // ← empty
                    filterStatus   = FilterStatus.NO_FILTER, // ← must be NO_FILTER
                    requestedArea  = "",
                    availableAreas = emptyList(),
                )
            )
        )

        val vm = HomeViewModel(
            getHomeDataUseCase   = useCase,
            cartRepository       = fakeCartRepo,
            userRepository       = fakeUserRepo,
            connectivityObserver = fakeConnectivity.mock,
            locationManager      = fakeLocationMgr.mock,
        )

        setContent(vm)
        composeTestRule.waitForIdle()

        assert(!vm.uiState.value.isLoading) { "Still loading" }
        assert(vm.uiState.value.filterStatus == FilterStatus.NO_FILTER) {
            "Expected NO_FILTER but got ${vm.uiState.value.filterStatus}"
        }

        composeTestRule
            .onNodeWithText("No restaurants found", substring = true)
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Categories Section
    // ══════════════════════════════════════════════════════════

    @Test
    fun homeScreen_whatsOnYourMind_title_shown_when_categories_loaded() {
        fakeRestaurantRepo.categoriesResult =
            Result.success(listOf(fakeCategory(1, "Biryani")))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("What's on your mind?")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_category_name_isDisplayed() {
        fakeRestaurantRepo.categoriesResult =
            Result.success(listOf(fakeCategory(1, "Biryani")))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Biryani").assertIsDisplayed()
    }

    @Test
    fun homeScreen_categories_section_not_shown_when_empty() {
        fakeRestaurantRepo.categoriesResult = Result.success(emptyList())
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("What's on your mind?")
            .assertDoesNotExist()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Offers Section
    // ══════════════════════════════════════════════════════════

    @Test
    fun homeScreen_excitingOffers_title_shown_when_collections_loaded() {
        fakeRestaurantRepo.collectionsResult =
            Result.success(listOf(fakeCollection(1, "Trending")))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Exciting Offers 🔥")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_offers_section_not_shown_when_empty() {
        fakeRestaurantRepo.collectionsResult = Result.success(emptyList())
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Exciting Offers 🔥")
            .assertDoesNotExist()
    }

    @Test
    fun homeScreen_collection_title_isDisplayed() {
        fakeRestaurantRepo.collectionsResult =
            Result.success(listOf(fakeCollection(1, "Trending Now")))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Trending Now").assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Error State
    // ✅ Uses buildViewModelWithError() — mocks use case directly
    // ══════════════════════════════════════════════════════════

    @Test
    fun homeScreen_error_message_shown_on_failure() {
        val vm = buildViewModelWithError("No internet connection")
        setContent(vm)
        composeTestRule.waitForIdle()

        // WHY WRONG not the exception message?
        // ErrorScreen shows AppConstants.WRONG as the title heading
        // The exception message is shown below it as body text
        // Both should be visible — check title first
        composeTestRule
            .onNodeWithText(WRONG, substring = true)
            .assertIsDisplayed()
    }
    @Test
    fun homeScreen_error_body_message_shown_on_failure() {
        val vm = buildViewModelWithError("No internet connection")
        setContent(vm)
        composeTestRule.waitForIdle()

        // Error body message shown below WRONG title
        composeTestRule
            .onNodeWithText("No internet connection", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_retry_button_shown_on_error() {
        val vm = buildViewModelWithError("Network error")
        setContent(vm)
        composeTestRule.waitForIdle()

        // WHY TRY_AGAIN not "Retry"?
        // ErrorScreen Button shows Text(TRY_AGAIN) — not "Retry"
        // RETRY is only the semantics contentDescription
        // onNodeWithText matches visible text = TRY_AGAIN
        composeTestRule.onNodeWithText(TRY_AGAIN).assertIsDisplayed()
    }

    @Test
    fun homeScreen_retry_click_reloads_data() {
        val (vm, _) = buildViewModelRetryable()
        setContent(vm)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TRY_AGAIN).assertIsDisplayed()

        composeTestRule.onNodeWithText(TRY_AGAIN).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Meghana Foods").assertIsDisplayed()
    }




    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Offline Banner
    // ══════════════════════════════════════════════════════════

    @Test
    fun homeScreen_offlineBanner_shown_when_network_unavailable() {
        fakeConnectivity.setOffline()
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("offline", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_offlineBanner_hidden_when_network_available() {
        fakeConnectivity.setOnline()
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("offline", substring = true, ignoreCase = true)
            .assertDoesNotExist()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Location Picker
    // ══════════════════════════════════════════════════════════

    @Test
    fun homeScreen_locationPicker_shown_on_locationBar_tap() {
        setContent()
        composeTestRule
            .onNodeWithText(AppConstants.DEFAULT_LOCATION)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Delivery Location")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_locationPicker_subtitle_shown_on_open() {
        setContent()
        composeTestRule
            .onNodeWithText(AppConstants.DEFAULT_LOCATION)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Where should we deliver?")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_locationPicker_dismissed_on_close_tap() {
        setContent()
        composeTestRule
            .onNodeWithText(AppConstants.DEFAULT_LOCATION)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("Close")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Delivery Location")
            .assertDoesNotExist()
    }

    @Test
    fun homeScreen_locationPicker_popularLocations_shown() {
        setContent()
        composeTestRule
            .onNodeWithText(AppConstants.DEFAULT_LOCATION)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onAllNodesWithText("Koramangala, Bengaluru")[0]
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_locationPicker_useCurrentLocation_shown() {
        setContent()
        composeTestRule
            .onNodeWithText(AppConstants.DEFAULT_LOCATION)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Use Current Location")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_locationPicker_noSavedAddresses_hint_shown() {
        fakeUserRepo.currentUser = null
        setContent()
        composeTestRule
            .onNodeWithText(AppConstants.DEFAULT_LOCATION)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("No saved addresses")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_locationPicker_savedAddress_Home_shown() {
        fakeUserRepo.currentUser = fakeUserWithAddress()
        setContent()
        composeTestRule.waitForIdle()

        // WHY click LOCATION_ICON_DESC not DEFAULT_LOCATION text?
        // User has selectedLocation = "Koramangala" → topBar shows "Koramangala"
        // DEFAULT_LOCATION text not present → click fails
        // Icon contentDescription is always LOCATION_ICON_DESC regardless
        composeTestRule
            .onNodeWithContentDescription(AppConstants.LOCATION_ICON_DESC)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Bottom Navigation
    // ══════════════════════════════════════════════════════════

    @Test
    fun homeScreen_bottomNav_delivery_tab_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText(AppConstants.DELIVERY).assertIsDisplayed()
    }

    @Test
    fun homeScreen_bottomNav_dining_tab_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText(AppConstants.DINING).assertIsDisplayed()
    }

    @Test
    fun homeScreen_bottomNav_profile_tab_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText(AppConstants.PROFILE).assertIsDisplayed()
    }

    @Test
    fun homeScreen_bottomNav_delivery_selected_by_default() {
        val vm = buildViewModel()
        setContent(vm)
        assert(vm.uiState.value.selectedTab == HomeViewModel.DeliveryTab.DELIVERY) {
            "Expected DELIVERY tab by default"
        }
    }

    @Test
    fun homeScreen_bottomNav_dining_click_updates_selectedTab() {
        val vm = buildViewModel()
        setContent(vm)
        composeTestRule.onNodeWithText(AppConstants.DINING).performClick()
        composeTestRule.waitForIdle()

        assert(vm.uiState.value.selectedTab == HomeViewModel.DeliveryTab.DINING) {
            "Expected DINING tab"
        }
    }

    @Test
    fun homeScreen_bottomNav_switching_back_to_delivery_from_dining() {
        val vm = buildViewModel()
        setContent(vm)
        composeTestRule.onNodeWithText(AppConstants.DINING).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(AppConstants.DELIVERY).performClick()
        composeTestRule.waitForIdle()

        assert(vm.uiState.value.selectedTab == HomeViewModel.DeliveryTab.DELIVERY) {
            "Expected DELIVERY after switching back"
        }
    }
}

// ── Local test data helpers ────────────────────────────────────

private fun fakeRestaurant(
    id:   String = "r1",
    name: String = "Meghana Foods",
) = Restaurant(
    id              = id,
    name            = name,
    imageUrl        = "",
    thumbUrl        = "",
    rating          = 4.5,
    ratingText      = "Excellent",
    ratingColor     = "#3F7E00",
    totalVotes      = 1000,
    avgDeliveryTime = 30,
    deliveryFee     = 0.0,
    minOrder        = 100,
    cuisines        = listOf("Biryani", "South Indian"),
    address         = "Koramangala, Bengaluru",
    locality        = "Koramangala",
    isOpen          = true,
    hasDelivery     = true,
    offers          = emptyList(),
    avgCostForTwo   = 400,
    distanceKm      = 1.0,
    phoneNumber     = "",
    openingHours    = "",
    highlights      = emptyList(),
    knownFor        = "",
)

private fun fakeCategory(id: Int, name: String) = FoodCategory(
    id       = id,
    name     = name,
    imageUrl = "",
)

private fun fakeCollection(id: Int, title: String) = Collections(
    id              = id,
    title           = title,
    description     = "",
    imageUrl        = "",
    restaurantCount = 10,
    discount        = "20% off",
)

private fun fakeUserWithAddress() = User(
    id               = "u1",
    name             = "Swapna",
    email            = "swapna@example.com",
    phone            = "+919876543210",
    profileImage     = "",
    addresses        = listOf(
        Address(
            id          = "a1",
            label       = "Home",
            fullAddress = "42, 3rd Floor, Koramangala",
            landmark    = "",
            latitude    = 12.93,
            longitude   = 77.62,
        )
    ),
    selectedLocation = "Koramangala",
)