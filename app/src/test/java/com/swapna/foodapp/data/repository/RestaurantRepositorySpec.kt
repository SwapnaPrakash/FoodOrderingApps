package com.swapna.foodapp.data.repository

import com.swapna.foodapp.data.local.dao.MenuItemDao
import com.swapna.foodapp.data.local.dao.RestaurantDao
import com.swapna.foodapp.data.local.entity.MenuItemEntity
import com.swapna.foodapp.data.local.entity.RestaurantEntity
import com.swapna.foodapp.data.mapper.EntityMapper
import com.swapna.foodapp.data.mapper.MenuMapper
import com.swapna.foodapp.data.mapper.RestaurantMapper
import com.swapna.foodapp.data.remote.api.FoodApi
import com.swapna.foodapp.data.remote.dto.CategoriesResponse
import com.swapna.foodapp.data.remote.dto.CategoryDto
import com.swapna.foodapp.data.remote.dto.CategoryWrapper
import com.swapna.foodapp.data.remote.dto.CollectionDto
import com.swapna.foodapp.data.remote.dto.CollectionWrapper
import com.swapna.foodapp.data.remote.dto.CollectionsResponse
import com.swapna.foodapp.data.remote.dto.CuisineDto
import com.swapna.foodapp.data.remote.dto.CuisineWrapper
import com.swapna.foodapp.data.remote.dto.CuisinesResponse
import com.swapna.foodapp.data.remote.dto.DailyMenuDto
import com.swapna.foodapp.data.remote.dto.DailyMenuResponse
import com.swapna.foodapp.data.remote.dto.DailyMenuWrapper
import com.swapna.foodapp.data.remote.dto.DishDto
import com.swapna.foodapp.data.remote.dto.DishWrapper
import com.swapna.foodapp.data.remote.dto.GeocodeResponse
import com.swapna.foodapp.data.remote.dto.LocationDto
import com.swapna.foodapp.data.remote.dto.RatingDto
import com.swapna.foodapp.data.remote.dto.RestaurantDto
import com.swapna.foodapp.data.remote.dto.RestaurantWrapper
import com.swapna.foodapp.data.remote.dto.ReviewDto
import com.swapna.foodapp.data.remote.dto.ReviewUserDto
import com.swapna.foodapp.data.remote.dto.ReviewWrapper
import com.swapna.foodapp.data.remote.dto.ReviewsResponse
import com.swapna.foodapp.data.remote.dto.SearchResponse
import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.utils.TestConstants.CATEGORY_BIRYANI
import com.swapna.foodapp.utils.TestConstants.CATEGORY_BURGER
import com.swapna.foodapp.utils.TestConstants.CATEGORY_ID_1
import com.swapna.foodapp.utils.TestConstants.CATEGORY_ID_2
import com.swapna.foodapp.utils.TestConstants.CATEGORY_ID_3
import com.swapna.foodapp.utils.TestConstants.CATEGORY_ID_99
import com.swapna.foodapp.utils.TestConstants.CATEGORY_PIZZA
import com.swapna.foodapp.utils.TestConstants.CATEGORY_STARTERS
import com.swapna.foodapp.utils.TestConstants.CATEGORY_SUSHI
import com.swapna.foodapp.utils.TestConstants.CATEGORY_THAI
import com.swapna.foodapp.utils.TestConstants.COLLECTION_COUNT_10
import com.swapna.foodapp.utils.TestConstants.COLLECTION_COUNT_15
import com.swapna.foodapp.utils.TestConstants.COLLECTION_COUNT_20
import com.swapna.foodapp.utils.TestConstants.COLLECTION_DISCOUNT_30
import com.swapna.foodapp.utils.TestConstants.COLLECTION_DISCOUNT_60
import com.swapna.foodapp.utils.TestConstants.COLLECTION_FRESH
import com.swapna.foodapp.utils.TestConstants.COLLECTION_GENERIC_DESC
import com.swapna.foodapp.utils.TestConstants.COLLECTION_HOT
import com.swapna.foodapp.utils.TestConstants.COLLECTION_ID_1
import com.swapna.foodapp.utils.TestConstants.COLLECTION_ID_2
import com.swapna.foodapp.utils.TestConstants.COLLECTION_ID_42
import com.swapna.foodapp.utils.TestConstants.COLLECTION_NEW
import com.swapna.foodapp.utils.TestConstants.COLLECTION_TRENDING
import com.swapna.foodapp.utils.TestConstants.COLLECTION_WEEKEND
import com.swapna.foodapp.utils.TestConstants.CUISINE_ID_1
import com.swapna.foodapp.utils.TestConstants.CUISINE_ID_2
import com.swapna.foodapp.utils.TestConstants.CUISINE_ID_3
import com.swapna.foodapp.utils.TestConstants.CUISINE_ID_42
import com.swapna.foodapp.utils.TestConstants.ENTITY_ADDRESS
import com.swapna.foodapp.utils.TestConstants.ENTITY_CITY_ID
import com.swapna.foodapp.utils.TestConstants.ENTITY_COST_TWO
import com.swapna.foodapp.utils.TestConstants.ENTITY_CUISINES_JSON
import com.swapna.foodapp.utils.TestConstants.ENTITY_CURRENCY
import com.swapna.foodapp.utils.TestConstants.ENTITY_DELIVERY_FEE
import com.swapna.foodapp.utils.TestConstants.ENTITY_DELIVERY_TIME
import com.swapna.foodapp.utils.TestConstants.ENTITY_LAT
import com.swapna.foodapp.utils.TestConstants.ENTITY_LNG
import com.swapna.foodapp.utils.TestConstants.ENTITY_LOCALITY
import com.swapna.foodapp.utils.TestConstants.ENTITY_PRICE_RANGE
import com.swapna.foodapp.utils.TestConstants.ENTITY_RATING
import com.swapna.foodapp.utils.TestConstants.ENTITY_RATING_COLOR
import com.swapna.foodapp.utils.TestConstants.ENTITY_RATING_TEXT
import com.swapna.foodapp.utils.TestConstants.ENTITY_VOTES
import com.swapna.foodapp.utils.TestConstants.ENTITY_ZIPCODE
import com.swapna.foodapp.utils.TestConstants.ERR_NO_INTERNET
import com.swapna.foodapp.utils.TestConstants.ERR_SERVER
import com.swapna.foodapp.utils.TestConstants.HOME_SOUTH_INDIAN
import com.swapna.foodapp.utils.TestConstants.MENU_ENTITY_PRICE
import com.swapna.foodapp.utils.TestConstants.MENU_ID_1
import com.swapna.foodapp.utils.TestConstants.MENU_ID_2
import com.swapna.foodapp.utils.TestConstants.MENU_ID_3
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_65
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_DESC
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_ID
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_MUTTON_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_PRICE_STR
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_BURGER
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_DOMINOS
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_EMPIRE
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_FRESH
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_1
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_2
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_3
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_MEGHANA
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_NEW_NAME
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_OLD_NAME
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_PIZZA
import com.swapna.foodapp.utils.TestConstants.REVIEW_ID_1
import com.swapna.foodapp.utils.TestConstants.REVIEW_ID_2
import com.swapna.foodapp.utils.TestConstants.REVIEW_IMG_URL
import com.swapna.foodapp.utils.TestConstants.REVIEW_RATING_4
import com.swapna.foodapp.utils.TestConstants.REVIEW_RATING_5
import com.swapna.foodapp.utils.TestConstants.REVIEW_TEXT_AMAZING
import com.swapna.foodapp.utils.TestConstants.REVIEW_TEXT_GOOD
import com.swapna.foodapp.utils.TestConstants.REVIEW_TEXT_GOOD_BIR
import com.swapna.foodapp.utils.TestConstants.REVIEW_TIME_1_DAY
import com.swapna.foodapp.utils.TestConstants.REVIEW_TIME_1_WEEK
import com.swapna.foodapp.utils.TestConstants.REVIEW_TIME_2_DAYS
import com.swapna.foodapp.utils.TestConstants.REVIEW_TOTAL_1
import com.swapna.foodapp.utils.TestConstants.REVIEW_TOTAL_COUNT
import com.swapna.foodapp.utils.TestConstants.REVIEW_USER_GENERIC
import com.swapna.foodapp.utils.TestConstants.REVIEW_USER_PRIYA
import com.swapna.foodapp.utils.TestConstants.REVIEW_USER_SWAPNA
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantRepositorySpec : FunSpec({

    val api = mockk<FoodApi>()
    val restaurantDao = mockk<RestaurantDao>()
    val menuItemDao = mockk<MenuItemDao>()
    val restaurantMapper = RestaurantMapper()
    val menuMapper = MenuMapper()
    val entityMapper = EntityMapper()

    fun createRepo() = RestaurantRepositoryImpl(
        api = api,
        restaurantDao = restaurantDao,
        menuItemDao = menuItemDao,
        restaurantMapper = restaurantMapper,
        menuMapper = menuMapper,
        entityMapper = entityMapper,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { restaurantDao.insertAll(any()) } just Runs
        coEvery { restaurantDao.insert(any()) } just Runs
        coEvery { restaurantDao.getById(any()) } returns null
        coEvery { restaurantDao.count() } returns 0
        every { menuItemDao.getMenuByRestaurant(any()) } returns flowOf(emptyList())
        coEvery { menuItemDao.insertAll(any()) } just Runs
        coEvery { menuItemDao.clearForRestaurant(any()) } just Runs
    }

    afterEach { Dispatchers.resetMain() }

    // getNearbyRestaurants
    test("getNearbyRestaurants: emits cached data when Room has restaurants") {
        val entity = fakeRestaurantEntity(RESTAURANT_ID_1, RESTAURANT_MEGHANA)
        every { restaurantDao.getAllRestaurants() } returns flowOf(listOf(entity))
        coEvery { api.getNearbyRestaurants() } throws IOException(ERR_NO_INTERNET)

        val result = createRepo().getNearbyRestaurants().first()

        result.isSuccess shouldBe true
        result.getOrNull()?.first()?.name shouldBe RESTAURANT_MEGHANA
    }

    test("getNearbyRestaurants: emits failure when Room empty and API fails") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.getNearbyRestaurants() } throws IOException(ERR_NO_INTERNET)

        val result = createRepo().getNearbyRestaurants().first()

        result.isFailure shouldBe true
    }

    test("getNearbyRestaurants: emits failure on general exception when cache empty") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.getNearbyRestaurants() } throws Exception(ERR_SERVER)

        val result = createRepo().getNearbyRestaurants().first()

        result.isFailure shouldBe true
    }

    test("getNearbyRestaurants: does not emit failure on exception when cache exists") {
        val entity = fakeRestaurantEntity(RESTAURANT_ID_1, RESTAURANT_MEGHANA)
        every { restaurantDao.getAllRestaurants() } returns flowOf(listOf(entity))
        coEvery { api.getNearbyRestaurants() } throws Exception(ERR_SERVER)

        val results = mutableListOf<Result<List<*>>>()
        createRepo().getNearbyRestaurants().collect { results.add(it) }

        results.size shouldBe 1
        results.first().isSuccess shouldBe true
    }

    test("getNearbyRestaurants: saves API response to Room") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf(RESTAURANT_ID_1 to RESTAURANT_MEGHANA, RESTAURANT_ID_2 to RESTAURANT_EMPIRE)
        )

        createRepo().getNearbyRestaurants().first()

        coVerify { restaurantDao.insertAll(match { it.size == 2 }) }
    }

    test("getNearbyRestaurants: emits cache first then fresh data") {
        val staleEntity = fakeRestaurantEntity(RESTAURANT_ID_1, RESTAURANT_OLD_NAME)
        every { restaurantDao.getAllRestaurants() } returns flowOf(listOf(staleEntity))
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf(RESTAURANT_ID_1 to RESTAURANT_NEW_NAME)
        )

        val emissions = mutableListOf<String>()
        createRepo().getNearbyRestaurants().collect { result ->
            result.getOrNull()?.firstOrNull()?.name?.let { emissions.add(it) }
        }

        emissions.first() shouldBe RESTAURANT_OLD_NAME
        emissions.last() shouldBe RESTAURANT_NEW_NAME
    }

    test("getNearbyRestaurants: fresh data has correct restaurant count") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf(
                RESTAURANT_ID_1 to RESTAURANT_MEGHANA,
                RESTAURANT_ID_2 to RESTAURANT_PIZZA,
                RESTAURANT_ID_3 to RESTAURANT_BURGER,
            )
        )

        val result = createRepo().getNearbyRestaurants().first()

        result.getOrNull()!! shouldHaveSize 3
    }

    // getCollections
    test("getCollections: returns mapped collections from API") {
        coEvery { api.getCollections() } returns CollectionsResponse(
            collections = listOf(
                CollectionWrapper(
                    CollectionDto(
                        COLLECTION_ID_1,
                        COLLECTION_TRENDING,
                        COLLECTION_HOT,
                        "",
                        COLLECTION_COUNT_20,
                        COLLECTION_DISCOUNT_60
                    )
                ),
                CollectionWrapper(
                    CollectionDto(
                        COLLECTION_ID_2,
                        COLLECTION_NEW,
                        COLLECTION_FRESH,
                        "",
                        COLLECTION_COUNT_10,
                        COLLECTION_DISCOUNT_30
                    )
                ),
            )
        )

        val result = createRepo().getCollections().first()

        result.isSuccess shouldBe true
        result.getOrNull()!! shouldHaveSize 2
        result.getOrNull()!![0].title shouldBe COLLECTION_TRENDING
        result.getOrNull()!![0].discount shouldBe COLLECTION_DISCOUNT_60
    }

    test("getCollections: returns empty list when API throws — non-critical") {
        coEvery { api.getCollections() } throws IOException(ERR_NO_INTERNET)

        val result = createRepo().getCollections().first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.shouldBeEmpty()
    }

    test("getCollections: maps collection id and restaurantCount correctly") {
        coEvery { api.getCollections() } returns CollectionsResponse(
            collections = listOf(
                CollectionWrapper(
                    CollectionDto(
                        COLLECTION_ID_42,
                        COLLECTION_WEEKEND,
                        COLLECTION_GENERIC_DESC,
                        "",
                        COLLECTION_COUNT_15,
                        COLLECTION_DISCOUNT_30
                    )
                )
            )
        )

        val result = createRepo().getCollections().first()

        result.getOrNull()!!.first().id shouldBe COLLECTION_ID_42
        result.getOrNull()!!.first().restaurantCount shouldBe COLLECTION_COUNT_15
    }

    // getCategories
    test("getCategories: returns mapped categories from API") {
        coEvery { api.getCategories() } returns fakeCategoriesResponse()

        val result = createRepo().getCategories().first()

        result.isSuccess shouldBe true
        result.getOrNull()!! shouldHaveSize 3
        result.getOrNull()!![0].name shouldBe CATEGORY_BIRYANI
    }

    test("getCategories: returns empty list on API failure — non-critical") {
        coEvery { api.getCategories() } throws Exception(ERR_SERVER)

        val result = createRepo().getCategories().first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.shouldBeEmpty()
    }

    test("getCategories: maps category id correctly") {
        coEvery { api.getCategories() } returns CategoriesResponse(
            categories = listOf(CategoryWrapper(CategoryDto(CATEGORY_ID_99, CATEGORY_SUSHI)))
        )

        val result = createRepo().getCategories().first()

        result.getOrNull()!!.first().id shouldBe CATEGORY_ID_99
    }

    // getRestaurantDetail
    test("getRestaurantDetail: serves cached restaurant first") {
        val entity = fakeRestaurantEntity(RESTAURANT_ID_1, RESTAURANT_MEGHANA)
        coEvery { restaurantDao.getById(RESTAURANT_ID_1) } returns entity
        coEvery { api.getNearbyRestaurants() } throws IOException(ERR_NO_INTERNET)

        val result = createRepo().getRestaurantDetail(RESTAURANT_ID_1).first()

        result.isSuccess shouldBe true
        result.getOrNull()?.name shouldBe RESTAURANT_MEGHANA
    }

    test("getRestaurantDetail: fails when no cache and no network") {
        coEvery { restaurantDao.getById(any()) } returns null
        coEvery { api.getNearbyRestaurants() } throws IOException(ERR_NO_INTERNET)

        val result = createRepo().getRestaurantDetail(RESTAURANT_ID_1).first()

        result.isFailure shouldBe true
    }

    test("getRestaurantDetail: saves fresh data to Room") {
        coEvery { restaurantDao.getById(any()) } returns null
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf(RESTAURANT_ID_1 to RESTAURANT_FRESH)
        )

        createRepo().getRestaurantDetail(RESTAURANT_ID_1).first()

        coVerify {
            restaurantDao.insertAll(
                match { entities -> entities.any { it.name == RESTAURANT_FRESH } }
            )
        }
    }

    test("getRestaurantDetail: returns correct restaurant when found in API response") {
        coEvery { restaurantDao.getById(any()) } returns null
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf(RESTAURANT_ID_1 to RESTAURANT_MEGHANA, RESTAURANT_ID_2 to RESTAURANT_PIZZA)
        )

        val result = createRepo().getRestaurantDetail(RESTAURANT_ID_1).first()

        result.isSuccess shouldBe true
        result.getOrNull()?.name shouldBe RESTAURANT_MEGHANA
    }

    test("getRestaurantDetail: returns failure when id not found in API response") {
        coEvery { restaurantDao.getById(any()) } returns null
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf(RESTAURANT_ID_2 to RESTAURANT_PIZZA)              // r1 not in list
        )

        val result = createRepo().getRestaurantDetail(RESTAURANT_ID_1).first()

        result.isFailure shouldBe true
    }

    test("getRestaurantDetail: cached restaurant has correct id") {
        val entity = fakeRestaurantEntity(RESTAURANT_ID_1, RESTAURANT_MEGHANA)
        coEvery { restaurantDao.getById(RESTAURANT_ID_1) } returns entity

        val result = createRepo().getRestaurantDetail(RESTAURANT_ID_1).first()

        result.getOrNull()?.id shouldBe RESTAURANT_ID_1
    }


    // getMenuItems
    test("getMenuItems: serves cached menu when Room has items") {
        val entity = fakeMenuItemEntity(
            MENU_ID_1,
            RESTAURANT_ID_1,
            MENU_ITEM_CHICK_BIR,
            CATEGORY_BIRYANI
        )
        every { menuItemDao.getMenuByRestaurant(RESTAURANT_ID_1) } returns flowOf(listOf(entity))
        coEvery { api.getDailyMenu() } throws Exception(ERR_NO_INTERNET)

        val result = createRepo().getMenuItems(RESTAURANT_ID_1).first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.containsKey(CATEGORY_BIRYANI) shouldBe true
    }

    test("getMenuItems: emits failure when no cache and API fails") {
        every { menuItemDao.getMenuByRestaurant(any()) } returns flowOf(emptyList())
        coEvery { api.getDailyMenu() } throws Exception(ERR_NO_INTERNET)

        val result = createRepo().getMenuItems(RESTAURANT_ID_1).first()

        result.isFailure shouldBe true
    }

    test("getMenuItems: fetches from API when cache empty") {
        every { menuItemDao.getMenuByRestaurant(any()) } returns flowOf(emptyList())
        coEvery { api.getDailyMenu() } returns fakeDailyMenuResponse()

        val result = createRepo().getMenuItems(RESTAURANT_ID_1).first()

        result.isSuccess shouldBe true
    }

    test("getMenuItems: saves API response to Room") {
        every { menuItemDao.getMenuByRestaurant(any()) } returns flowOf(emptyList())
        coEvery { api.getDailyMenu() } returns fakeDailyMenuResponse()

        createRepo().getMenuItems(RESTAURANT_ID_1).first()

        coVerify { menuItemDao.clearForRestaurant(RESTAURANT_ID_1) }
        coVerify { menuItemDao.insertAll(any()) }
    }

    test("getMenuItems: groups items by category") {
        every { menuItemDao.getMenuByRestaurant(any()) } returns flowOf(emptyList())
        coEvery { api.getDailyMenu() } returns fakeDailyMenuResponse()

        val result = createRepo().getMenuItems(RESTAURANT_ID_1).first()
        val menuMap = result.getOrNull()!!

        menuMap.containsKey(CATEGORY_BIRYANI) shouldBe true
        menuMap[CATEGORY_BIRYANI]!! shouldHaveSize 1
    }

    test("getMenuItems: cached items grouped by category correctly") {
        val entities = listOf(
            fakeMenuItemEntity(
                MENU_ID_1,
                RESTAURANT_ID_1,
                MENU_ITEM_CHICK_BIR,
                CATEGORY_BIRYANI
            ),
            fakeMenuItemEntity(
                MENU_ID_2,
                RESTAURANT_ID_1,
                MENU_ITEM_MUTTON_BIR,
                CATEGORY_BIRYANI
            ),
            fakeMenuItemEntity(
                MENU_ID_3,
                RESTAURANT_ID_1,
                MENU_ITEM_CHICK_65,
                CATEGORY_STARTERS
            ),
        )
        every { menuItemDao.getMenuByRestaurant(RESTAURANT_ID_1) } returns flowOf(entities)
        coEvery { api.getDailyMenu() } throws Exception(ERR_NO_INTERNET)

        val result = createRepo().getMenuItems(RESTAURANT_ID_1).first()
        val menuMap = result.getOrNull()!!

        menuMap.size shouldBe 2
        menuMap[CATEGORY_BIRYANI]!! shouldHaveSize 2
        menuMap[CATEGORY_STARTERS]!! shouldHaveSize 1
    }

    // ══════════════════════════════════════════════════════════
    // getReviews
    // ══════════════════════════════════════════════════════════

    test("getReviews: returns mapped reviews from API") {
        coEvery { api.getReviews() } returns fakeReviewsResponse()

        val result = createRepo().getReviews(RESTAURANT_ID_1).first()

        result.isSuccess shouldBe true
        result.getOrNull()!! shouldHaveSize REVIEW_TOTAL_COUNT
    }

    test("getReviews: returns empty list on API failure — non-critical") {
        coEvery { api.getReviews() } throws Exception(ERR_NO_INTERNET)

        val result = createRepo().getReviews(RESTAURANT_ID_1).first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.shouldBeEmpty()
    }

    test("getReviews: maps review fields correctly") {
        coEvery { api.getReviews() } returns ReviewsResponse(
            totalCount = REVIEW_TOTAL_1,
            reviews = listOf(
                ReviewWrapper(
                    ReviewDto(
                        id = REVIEW_ID_1,
                        rating = REVIEW_RATING_5,
                        text = REVIEW_TEXT_AMAZING,
                        timeAgo = REVIEW_TIME_2_DAYS,
                        user = ReviewUserDto(name = REVIEW_USER_SWAPNA, profileImage = null),
                    )
                )
            )
        )

        val review = createRepo().getReviews(RESTAURANT_ID_1).first().getOrNull()!!.first()

        review.id shouldBe REVIEW_ID_1
        review.rating shouldBe REVIEW_RATING_5
        review.text shouldBe REVIEW_TEXT_AMAZING
        review.timeAgo shouldBe REVIEW_TIME_2_DAYS
        review.userName shouldBe REVIEW_USER_SWAPNA
        review.userImage shouldBe ""
    }

    test("getReviews: null userImage maps to empty string") {
        coEvery { api.getReviews() } returns ReviewsResponse(
            totalCount = REVIEW_TOTAL_1,
            reviews = listOf(
                ReviewWrapper(
                    ReviewDto(
                        id = REVIEW_ID_1, rating = REVIEW_RATING_4,
                        text = REVIEW_TEXT_GOOD, timeAgo = REVIEW_TIME_1_DAY,
                        user = ReviewUserDto(name = REVIEW_USER_GENERIC, profileImage = null)
                    )
                )
            )
        )

        val review = createRepo().getReviews(RESTAURANT_ID_1).first().getOrNull()!!.first()
        review.userImage shouldBe ""
    }

    test("getReviews: non-null userImage maps correctly") {
        coEvery { api.getReviews() } returns ReviewsResponse(
            totalCount = REVIEW_TOTAL_1,
            reviews = listOf(
                ReviewWrapper(
                    ReviewDto(
                        id = REVIEW_ID_1, rating = REVIEW_RATING_4,
                        text = REVIEW_TEXT_GOOD, timeAgo = REVIEW_TIME_1_DAY,
                        user = ReviewUserDto(
                            name = REVIEW_USER_GENERIC,
                            profileImage = REVIEW_IMG_URL
                        )
                    )
                )
            )
        )

        val review = createRepo().getReviews(RESTAURANT_ID_1).first().getOrNull()!!.first()
        review.userImage shouldBe REVIEW_IMG_URL
    }

    // ══════════════════════════════════════════════════════════
    // searchRestaurants
    // ══════════════════════════════════════════════════════════

    test("searchRestaurants: returns results from API") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.searchRestaurants() } returns fakeSearchResponse(
            listOf(RESTAURANT_ID_1 to RESTAURANT_PIZZA, RESTAURANT_ID_2 to RESTAURANT_DOMINOS)
        )

        val result = createRepo().searchRestaurants("pizza", SearchFilters()).first()

        result.isSuccess shouldBe true
        result.getOrNull()!! shouldHaveSize 2
    }

    test("searchRestaurants: emits cached restaurants when API fails") {
        val entities = listOf(
            fakeRestaurantEntity(RESTAURANT_ID_1, RESTAURANT_MEGHANA),
            fakeRestaurantEntity(RESTAURANT_ID_2, RESTAURANT_EMPIRE),
        )
        every { restaurantDao.getAllRestaurants() } returns flowOf(entities)
        coEvery { api.searchRestaurants() } throws IOException(ERR_NO_INTERNET)

        val results = mutableListOf<Result<List<*>>>()
        createRepo().searchRestaurants(RESTAURANT_MEGHANA, SearchFilters())
            .collect { results.add(it) }

        results.isNotEmpty() shouldBe true
        results.first().isSuccess shouldBe true
    }

    test("searchRestaurants: emits failure when cache empty and API fails") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.searchRestaurants() } throws Exception(ERR_SERVER)

        val result = createRepo().searchRestaurants("pizza", SearchFilters()).first()

        result.isFailure shouldBe true
    }

    test("searchRestaurants: blank query skips cache filter and hits API") {
        val entities = listOf(fakeRestaurantEntity(RESTAURANT_ID_1, RESTAURANT_MEGHANA))
        every { restaurantDao.getAllRestaurants() } returns flowOf(entities)
        coEvery { api.searchRestaurants() } returns fakeSearchResponse(
            listOf(RESTAURANT_ID_1 to RESTAURANT_MEGHANA)
        )

        val results = mutableListOf<Result<List<*>>>()
        createRepo().searchRestaurants("", SearchFilters()).collect { results.add(it) }

        results.size shouldBe 1
    }

    test("searchRestaurants: cached results filtered by query name") {
        val entities = listOf(
            fakeRestaurantEntity(RESTAURANT_ID_1, RESTAURANT_MEGHANA),
            fakeRestaurantEntity(RESTAURANT_ID_2, RESTAURANT_PIZZA),
        )
        every { restaurantDao.getAllRestaurants() } returns flowOf(entities)
        coEvery { api.searchRestaurants() } throws IOException(ERR_NO_INTERNET)

        val result = createRepo()
            .searchRestaurants("meghana", SearchFilters()).first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.size shouldBe 1
        result.getOrNull()!!.first().name shouldBe RESTAURANT_MEGHANA
    }

    // ══════════════════════════════════════════════════════════
    // getCuisines
    // ══════════════════════════════════════════════════════════

    test("getCuisines: returns mapped cuisines from API") {
        coEvery { api.getCuisines() } returns fakeCuisinesResponse()

        val result = createRepo().getCuisines().first()

        result.isSuccess shouldBe true
        result.getOrNull()!! shouldHaveSize 3
        result.getOrNull()!![0].name shouldBe CATEGORY_BIRYANI
    }

    test("getCuisines: returns empty list on API failure — non-critical") {
        coEvery { api.getCuisines() } throws Exception(ERR_NO_INTERNET)

        val result = createRepo().getCuisines().first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.shouldBeEmpty()
    }

    test("getCuisines: maps cuisine id correctly") {
        coEvery { api.getCuisines() } returns CuisinesResponse(
            cuisines = listOf(
                CuisineWrapper(
                    CuisineDto(
                        id = CUISINE_ID_42,
                        name = CATEGORY_THAI
                    )
                )
            )
        )

        val result = createRepo().getCuisines().first()

        result.getOrNull()!!.first().id shouldBe CUISINE_ID_42
        result.getOrNull()!!.first().name shouldBe CATEGORY_THAI
    }
})

// ── Test helpers ──────────────────────────────────────────────

fun fakeRestaurantEntity(id: String, name: String) = RestaurantEntity(
    id = id,
    name = name,
    imageUrl = "",
    thumbUrl = "",
    rating = ENTITY_RATING,
    ratingText = ENTITY_RATING_TEXT,
    ratingColor = ENTITY_RATING_COLOR,
    totalVotes = ENTITY_VOTES,
    avgDeliveryTime = ENTITY_DELIVERY_TIME,
    deliveryFee = ENTITY_DELIVERY_FEE,
    minOrder = 0,
    cuisinesJson = ENTITY_CUISINES_JSON,
    address = ENTITY_ADDRESS,
    locality = ENTITY_LOCALITY,
    isOpen = true,
    hasDelivery = true,
    offersJson = "[]",
    avgCostForTwo = ENTITY_COST_TWO,
    distanceKm = 0.0,
    phoneNumber = "",
    openingHours = "",
    highlightsJson = "[]",
    knownFor = "",
)

fun fakeMenuItemEntity(
    id: String,
    restaurantId: String,
    name: String,
    category: String,
) = MenuItemEntity(
    id = id,
    restaurantId = restaurantId,
    name = name,
    description = "",
    price = MENU_ENTITY_PRICE,
    imageUrl = "",
    category = category,
    isVeg = false,
    isRecommended = false,
    isBestseller = false,
    isAvailable = true,
    customisationsJson = "[]",
)

fun fakeRestaurantDto(id: String, name: String) = RestaurantDto(
    id = id,
    name = name,
    featuredImage = "",
    thumb = "",
    location = LocationDto(
        ENTITY_ADDRESS, ENTITY_LOCALITY, ENTITY_ADDRESS,
        ENTITY_CITY_ID, ENTITY_LAT, ENTITY_LNG, ENTITY_ZIPCODE
    ),
    cuisines = "$CATEGORY_BIRYANI, $HOME_SOUTH_INDIAN",
    avgCostForTwo = ENTITY_COST_TWO,
    priceRange = ENTITY_PRICE_RANGE,
    currency = ENTITY_CURRENCY,
    rating = RatingDto(
        ENTITY_RATING.toString(), ENTITY_RATING_TEXT, ENTITY_RATING_COLOR, ENTITY_VOTES.toString()
    ),
    hasDelivery = 1,
    isDeliveringNow = 1,
    deliveryTime = ENTITY_DELIVERY_TIME,
    minOrder = 100,
)

fun fakeGeocodeResponse(restaurants: List<Pair<String, String>>) = GeocodeResponse(
    location = LocationDto(
        ENTITY_ADDRESS, ENTITY_LOCALITY, ENTITY_ADDRESS,
        ENTITY_CITY_ID, ENTITY_LAT, ENTITY_LNG, ENTITY_ZIPCODE
    ),
    nearbyRestaurants = restaurants.map { (id, name) ->
        RestaurantWrapper(fakeRestaurantDto(id, name))
    },
)

fun fakeSearchResponse(restaurants: List<Pair<String, String>>) = SearchResponse(
    totalFound = restaurants.size,
    shown = restaurants.size,
    restaurants = restaurants.map { (id, name) ->
        RestaurantWrapper(fakeRestaurantDto(id, name))
    },
)

fun fakeCategoriesResponse() = CategoriesResponse(
    categories = listOf(
        CategoryWrapper(CategoryDto(CATEGORY_ID_1, CATEGORY_BIRYANI)),
        CategoryWrapper(CategoryDto(CATEGORY_ID_2, CATEGORY_PIZZA)),
        CategoryWrapper(CategoryDto(CATEGORY_ID_3, CATEGORY_BURGER)),
    )
)

fun fakeDailyMenuResponse() = DailyMenuResponse(
    dailyMenus = listOf(
        DailyMenuWrapper(
            menu = DailyMenuDto(
                id = MENU_ITEM_ID,
                name = CATEGORY_BIRYANI,
                dishes = listOf(
                    DishWrapper(
                        dish = DishDto(
                            id = MENU_ID_1,
                            name = MENU_ITEM_CHICK_BIR,
                            price = MENU_ITEM_PRICE_STR,
                            description = MENU_ITEM_DESC,
                            imageUrl = null,
                            isVeg = 0,
                            isRecommended = 1,
                            customisations = null,
                        )
                    )
                )
            )
        )
    )
)

fun fakeReviewsResponse() = ReviewsResponse(
    totalCount = REVIEW_TOTAL_COUNT,
    reviews = listOf(
        ReviewWrapper(
            ReviewDto(
                id = REVIEW_ID_1,
                rating = REVIEW_RATING_5,
                text = REVIEW_TEXT_AMAZING,
                timeAgo = REVIEW_TIME_2_DAYS,
                user = ReviewUserDto(name = REVIEW_USER_SWAPNA, profileImage = null),
            )
        ),
        ReviewWrapper(
            ReviewDto(
                id = REVIEW_ID_2,
                rating = REVIEW_RATING_4,
                text = REVIEW_TEXT_GOOD_BIR,
                timeAgo = REVIEW_TIME_1_WEEK,
                user = ReviewUserDto(name = REVIEW_USER_PRIYA, profileImage = REVIEW_IMG_URL),
            )
        ),
    )
)

fun fakeCuisinesResponse() = CuisinesResponse(
    cuisines = listOf(
        CuisineWrapper(CuisineDto(CUISINE_ID_1, CATEGORY_BIRYANI)),
        CuisineWrapper(CuisineDto(CUISINE_ID_2, CATEGORY_PIZZA)),
        CuisineWrapper(CuisineDto(CUISINE_ID_3, CATEGORY_BURGER)),
    )
)