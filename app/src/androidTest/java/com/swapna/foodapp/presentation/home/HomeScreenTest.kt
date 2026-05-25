package com.swapna.foodapp.presentation.home

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.RestaurantCollection
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.fakes.FakeCartRepository
import com.swapna.foodapp.fakes.FakeConnectivityObserver
import com.swapna.foodapp.fakes.FakeLocationManager
import com.swapna.foodapp.fakes.FakeRestaurantRepository
import com.swapna.foodapp.fakes.FakeUserRepository
import com.swapna.foodapp.utils.AndroidTestConstants
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_EXPECTED_DELIVERY_BACK
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_EXPECTED_DELIVERY_TAB
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_EXPECTED_DINING_TAB
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_EXPECTED_NO_FILTER
import com.swapna.foodapp.utils.AndroidTestConstants.ASSERT_STILL_LOADING
import com.swapna.foodapp.utils.AndroidTestConstants.CART_BADGE_ZERO
import com.swapna.foodapp.utils.AndroidTestConstants.CART_COUNT_ONE
import com.swapna.foodapp.utils.AndroidTestConstants.CART_COUNT_THREE
import com.swapna.foodapp.utils.AndroidTestConstants.ERR_NETWORK_ERROR
import com.swapna.foodapp.utils.AndroidTestConstants.ERR_NO_INTERNET_CONNECTION
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_CATEGORY_BIRYANI
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_CATEGORY_ID_1
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_CHANGE_DELIVERY_LOCATION
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_CLOSE_ICON_DESC
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_COLL_ID_1
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_COLL_TRENDING
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_COLL_TRENDING_NOW
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_DELIVERING_TO
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_DELIVERY_LOCATION_SUBTITLE
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_DELIVERY_LOCATION_TITLE
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ERROR_NOT_IN_SUBSTR
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_GO_TO_CART_DESC
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_LOCATION_ICON_DESC
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_NO_RESTAURANTS_FOUND
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_NO_SAVED_ADDRESSES
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_OFFLINE_SUBSTR
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_POPULAR_LOCATION
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_REST_ADDRESS
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_REST_MEGHANA
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_REST_PIZZA_HUT
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_REST_R1
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_REST_R2
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_SEARCH_HINT
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_SECTION_CATEGORIES
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_SECTION_OFFERS
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_SECTION_RESTAURANTS
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_USE_CURRENT_LOCATION
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.TRY_AGAIN
import com.swapna.foodapp.utils.AppConstants.WRONG
import com.swapna.foodapp.utils.HomeData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule(order = 0)
    val screenOnRule = object : org.junit.rules.ExternalResource() {
        override fun before() {
            val uiAutomation = androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation

            uiAutomation.executeShellCommand(AndroidTestConstants.CMD_WAKEUP)
            uiAutomation.executeShellCommand(AndroidTestConstants.CMD_DISMISS_KEYGUARD)
        }
    }

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var fakeRestaurantRepo: FakeRestaurantRepository
    private lateinit var fakeCartRepo: FakeCartRepository
    private lateinit var fakeUserRepo: FakeUserRepository
    private lateinit var fakeConnectivity: FakeConnectivityObserver
    private lateinit var fakeLocationMgr: FakeLocationManager

    @Before
    fun setUp() {
        fakeRestaurantRepo = FakeRestaurantRepository()
        fakeCartRepo = FakeCartRepository()
        fakeUserRepo = FakeUserRepository()
        fakeConnectivity = FakeConnectivityObserver()
        fakeLocationMgr = FakeLocationManager()
    }

    private fun buildViewModel() = HomeViewModel(
        getHomeDataUseCase = GetHomeDataUseCase(fakeRestaurantRepo),
        cartRepository = fakeCartRepo,
        userRepository = fakeUserRepo,
        connectivityObserver = fakeConnectivity.mock,
        locationManager = fakeLocationMgr.mock,
    )

    private fun buildViewModelWithError(message: String): HomeViewModel {
        val useCase = mockk<GetHomeDataUseCase>()
        every { useCase(any()) } returns flowOf(
            Result.failure(Exception(message))
        )
        return HomeViewModel(
            getHomeDataUseCase = useCase,
            cartRepository = fakeCartRepo,
            userRepository = fakeUserRepo,
            connectivityObserver = fakeConnectivity.mock,
            locationManager = fakeLocationMgr.mock,
        )
    }

    private fun buildViewModelRetryable(): Pair<HomeViewModel, GetHomeDataUseCase> {
        var callCount = 0
        val useCase = mockk<GetHomeDataUseCase>()

        every { useCase(any()) } answers {
            callCount++
            if (callCount == 1) {
                flowOf(Result.failure(Exception(ERR_NETWORK_ERROR)))
            } else {
                flowOf(
                    Result.success(
                        HomeData(
                            collections = emptyList(),
                            categories = emptyList(),
                            restaurants = listOf(fakeRestaurant(HOME_REST_R1, HOME_REST_MEGHANA)),
                            filterStatus = FilterStatus.NO_FILTER,
                            requestedArea = "",
                            availableAreas = emptyList(),
                        )
                    )
                )
            }
        }

        val vm = HomeViewModel(
            getHomeDataUseCase = useCase,
            cartRepository = fakeCartRepo,
            userRepository = fakeUserRepo,
            connectivityObserver = fakeConnectivity.mock,
            locationManager = fakeLocationMgr.mock,
        )
        return vm to useCase
    }

    private fun setContent(vm: HomeViewModel = buildViewModel()) {
        composeTestRule.setContent {
            MaterialTheme {
                HomeScreen(
                    navController = rememberNavController(),
                    viewModel = vm,
                )
            }
        }
    }

    // GROUP 1 — TopBar
    @Test
    fun homeScreen_deliveringTo_label_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(HOME_DELIVERING_TO)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_defaultLocation_isDisplayed_in_topBar() {
        setContent()
        composeTestRule
            .onNodeWithText(HOME_POPULAR_LOCATION)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_searchHint_isDisplayed_in_topBar() {
        setContent()
        composeTestRule
            .onNodeWithText(HOME_SEARCH_HINT)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_cartIcon_isDisplayed_in_topBar() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(HOME_GO_TO_CART_DESC)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_locationIcon_isDisplayed_in_topBar() {
        setContent()
        composeTestRule
            .onNodeWithContentDescription(HOME_LOCATION_ICON_DESC)
            .assertIsDisplayed()
    }

    // GROUP 2 — Cart Badge
    @Test
    fun homeScreen_cartBadge_not_shown_when_cart_empty() {
        setContent()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(CART_BADGE_ZERO).assertDoesNotExist()
    }

    @Test
    fun homeScreen_cartBadge_shows_3_when_3_items_in_cart() {
        fakeCartRepo.setItemCount(CART_COUNT_THREE)
        setContent()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(CART_COUNT_THREE.toString()).assertIsDisplayed()
    }

    @Test
    fun homeScreen_cartBadge_shows_1_when_one_item_in_cart() {
        fakeCartRepo.setItemCount(CART_COUNT_ONE)
        setContent()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(CART_COUNT_ONE.toString()).assertIsDisplayed()
    }

    // GROUP 3 — Restaurants Section
    @Test
    fun homeScreen_restaurantsNearYou_title_isDisplayed() {
        fakeRestaurantRepo.nearbyRestaurantsResult =
            Result.success(listOf(fakeRestaurant(HOME_REST_R1, HOME_REST_MEGHANA)))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_SECTION_RESTAURANTS)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_restaurantName_isDisplayed() {
        fakeRestaurantRepo.nearbyRestaurantsResult =
            Result.success(listOf(fakeRestaurant(HOME_REST_R1, HOME_REST_MEGHANA)))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(HOME_REST_MEGHANA).assertIsDisplayed()
    }

    @Test
    fun homeScreen_multiple_restaurants_all_names_shown() {
        fakeRestaurantRepo.nearbyRestaurantsResult = Result.success(
            listOf(
                fakeRestaurant(HOME_REST_R1, HOME_REST_MEGHANA),
                fakeRestaurant(HOME_REST_R2, HOME_REST_PIZZA_HUT),
            )
        )
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(HOME_REST_MEGHANA).assertIsDisplayed()
        composeTestRule.onNodeWithText(HOME_REST_PIZZA_HUT).assertIsDisplayed()
    }

    @Test
    fun homeScreen_notServiceable_message_shown_for_non_service_area() {
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_ERROR_NOT_IN_SUBSTR, substring = true, ignoreCase = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(HOME_CHANGE_DELIVERY_LOCATION)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_empty_restaurants_shows_no_restaurants_found() {
        val useCase = mockk<GetHomeDataUseCase>()
        every { useCase(any()) } returns flowOf(
            Result.success(
                HomeData(
                    collections = emptyList(),
                    categories = emptyList(),
                    restaurants = emptyList(),           // ← empty
                    filterStatus = FilterStatus.NO_FILTER, // ← must be NO_FILTER
                    requestedArea = "",
                    availableAreas = emptyList(),
                )
            )
        )

        val vm = HomeViewModel(
            getHomeDataUseCase = useCase,
            cartRepository = fakeCartRepo,
            userRepository = fakeUserRepo,
            connectivityObserver = fakeConnectivity.mock,
            locationManager = fakeLocationMgr.mock,
        )

        setContent(vm)
        composeTestRule.waitForIdle()

        assert(!vm.uiState.value.isLoading) { ASSERT_STILL_LOADING }
        assert(vm.uiState.value.filterStatus == FilterStatus.NO_FILTER) {
            "$ASSERT_EXPECTED_NO_FILTER ${vm.uiState.value.filterStatus}"
        }

        composeTestRule
            .onNodeWithText(HOME_NO_RESTAURANTS_FOUND, substring = true)
            .assertIsDisplayed()
    }

    // GROUP 4 — Categories Section
    @Test
    fun homeScreen_whatsOnYourMind_title_shown_when_categories_loaded() {
        fakeRestaurantRepo.categoriesResult =
            Result.success(listOf(fakeCategory(CART_COUNT_ONE, HOME_CATEGORY_BIRYANI)))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_SECTION_CATEGORIES)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_category_name_isDisplayed() {
        fakeRestaurantRepo.categoriesResult =
            Result.success(listOf(fakeCategory(CART_COUNT_ONE, HOME_CATEGORY_BIRYANI)))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(HOME_CATEGORY_BIRYANI).assertIsDisplayed()
    }

    @Test
    fun homeScreen_categories_section_not_shown_when_empty() {
        fakeRestaurantRepo.categoriesResult = Result.success(emptyList())
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_SECTION_CATEGORIES)
            .assertDoesNotExist()
    }

    // GROUP 5 — Offers Section
    @Test
    fun homeScreen_excitingOffers_title_shown_when_collections_loaded() {
        fakeRestaurantRepo.restaurantCollectionResult =
            Result.success(listOf(fakeCollection(HOME_CATEGORY_ID_1, HOME_COLL_TRENDING)))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_SECTION_OFFERS)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_offers_section_not_shown_when_empty() {
        fakeRestaurantRepo.restaurantCollectionResult = Result.success(emptyList())
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_SECTION_OFFERS)
            .assertDoesNotExist()
    }

    @Test
    fun homeScreen_collection_title_isDisplayed() {
        fakeRestaurantRepo.restaurantCollectionResult =
            Result.success(listOf(fakeCollection(HOME_COLL_ID_1, HOME_COLL_TRENDING_NOW)))
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(HOME_COLL_TRENDING_NOW).assertIsDisplayed()
    }

    // GROUP 6 — Error State
    @Test
    fun homeScreen_error_message_shown_on_failure() {
        val vm = buildViewModelWithError(ERR_NO_INTERNET_CONNECTION)
        setContent(vm)
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText(WRONG, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_error_body_message_shown_on_failure() {
        val vm = buildViewModelWithError(ERR_NO_INTERNET_CONNECTION)
        setContent(vm)
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText(ERR_NO_INTERNET_CONNECTION, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_retry_button_shown_on_error() {
        val vm = buildViewModelWithError(ERR_NETWORK_ERROR)
        setContent(vm)
        composeTestRule.waitForIdle()
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

        composeTestRule.onNodeWithText(HOME_REST_MEGHANA).assertIsDisplayed()
    }

    // GROUP 7 — Offline Banner
    @Test
    fun homeScreen_offlineBanner_shown_when_network_unavailable() {
        fakeConnectivity.setOffline()
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_OFFLINE_SUBSTR, substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_offlineBanner_hidden_when_network_available() {
        fakeConnectivity.setOnline()
        setContent()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_OFFLINE_SUBSTR, substring = true, ignoreCase = true)
            .assertDoesNotExist()
    }

    // GROUP 8 — Location Picker
    @Test
    fun homeScreen_locationPicker_shown_on_locationBar_tap() {
        setContent()
        composeTestRule
            .onNodeWithText(HOME_REST_ADDRESS)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_DELIVERY_LOCATION_TITLE)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_locationPicker_subtitle_shown_on_open() {
        setContent()
        composeTestRule
            .onNodeWithText(HOME_REST_ADDRESS)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_DELIVERY_LOCATION_SUBTITLE)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_locationPicker_dismissed_on_close_tap() {
        setContent()
        composeTestRule
            .onNodeWithText(HOME_REST_ADDRESS)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription(HOME_CLOSE_ICON_DESC)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_DELIVERY_LOCATION_TITLE)
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
            .onAllNodesWithText(HOME_POPULAR_LOCATION)[0]
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_locationPicker_useCurrentLocation_shown() {
        setContent()
        composeTestRule
            .onNodeWithText(HOME_POPULAR_LOCATION)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(HOME_USE_CURRENT_LOCATION, ignoreCase = true)
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
            .onNodeWithText(HOME_NO_SAVED_ADDRESSES)
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_locationPicker_savedAddress_Home_shown() {
        fakeUserRepo.currentUser = fakeUserWithAddress()
        setContent()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithContentDescription(AppConstants.LOCATION_ICON_DESC)
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(HOME_ADDRESS_LABEL_HOME, ignoreCase = true)
            .assertIsDisplayed()
    }

    // GROUP 9 — Bottom Navigation
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
            ASSERT_EXPECTED_DELIVERY_TAB
        }
    }

    @Test
    fun homeScreen_bottomNav_dining_click_updates_selectedTab() {
        val vm = buildViewModel()
        setContent(vm)
        composeTestRule.onNodeWithText(AppConstants.DINING).performClick()
        composeTestRule.waitForIdle()

        assert(vm.uiState.value.selectedTab == HomeViewModel.DeliveryTab.DINING) {
            ASSERT_EXPECTED_DINING_TAB
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
            ASSERT_EXPECTED_DELIVERY_BACK
        }
    }
}

// ── Local test data helpers ────────────────────────────────────
private fun fakeRestaurant(
    id: String = AndroidTestConstants.FAKE_REST_ID_R1,
    name: String = AndroidTestConstants.FAKE_REST_MEGHANA,
) = Restaurant(
    id = id,
    name = name,
    imageUrl = "",
    thumbUrl = "",
    rating = AndroidTestConstants.FAKE_REST_RATING_4_6,
    ratingText = AndroidTestConstants.FAKE_REST_RATING_TEXT_EX,
    ratingColor = AndroidTestConstants.FAKE_REST_RATING_COLOR,
    totalVotes = AndroidTestConstants.FAKE_REST_TOTAL_VOTES_12500,
    avgDeliveryTime = AndroidTestConstants.FAKE_REST_DELIVERY_TIME_30,
    deliveryFee = AndroidTestConstants.FAKE_REST_DELIVERY_FEE_FREE,
    minOrder = AndroidTestConstants.FAKE_REST_MIN_ORDER_100,
    cuisines = listOf(
        AndroidTestConstants.FAKE_REST_CUISINE_BIRYANI,
        AndroidTestConstants.FAKE_REST_CUISINE_SOUTH_IND
    ),
    address = AndroidTestConstants.FAKE_REST_ADDRESS_KORA,
    locality = AndroidTestConstants.FAKE_REST_LOCALITY_KORA,
    isOpen = true,
    hasDelivery = true,
    offers = emptyList(),
    avgCostForTwo = AndroidTestConstants.FAKE_REST_COST_FOR_TWO_400,
    distanceKm = AndroidTestConstants.FAKE_REST_DISTANCE_KM_2_5,
    phoneNumber = "",
    openingHours = AndroidTestConstants.FAKE_REST_OPENING_HOURS,
    highlights = emptyList(),
    knownFor = "",
)

private fun fakeCategory(id: Int, name: String) = FoodCategory(
    id = id,
    name = name,
    imageUrl = "",
)

private fun fakeCollection(id: Int, title: String) = RestaurantCollection(
    id = id,
    title = title,
    description = "",
    imageUrl = "",
    restaurantCount = AndroidTestConstants.HOME_COLL_RESTAURANT_COUNT,
    discount = AndroidTestConstants.HOME_COLL_DISCOUNT,
)

private fun fakeUserWithAddress() = User(
    id = AndroidTestConstants.HOME_USER_ID,
    name = AndroidTestConstants.HOME_USER_NAME,
    email = AndroidTestConstants.HOME_USER_EMAIL,
    phone = AndroidTestConstants.HOME_USER_PHONE,
    profileImage = "",
    addresses = listOf(
        Address(
            id = AndroidTestConstants.HOME_ADDRESS_ID,
            label = HOME_ADDRESS_LABEL_HOME,
            fullAddress = AndroidTestConstants.HOME_ADDRESS_FULL,
            landmark = AndroidTestConstants.HOME_ADDRESS_LANDMARK,
            latitude = AndroidTestConstants.HOME_ADDRESS_LAT,
            longitude = AndroidTestConstants.HOME_ADDRESS_LNG,
        )
    ),
    selectedLocation = AndroidTestConstants.HOME_USER_SELECTED_LOCATION,
)