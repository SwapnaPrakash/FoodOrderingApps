package com.swapna.foodapp.data.remote

import com.swapna.foodapp.data.local.dao.MenuItemDao
import com.swapna.foodapp.data.local.dao.RestaurantDao
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
import com.swapna.foodapp.data.remote.dto.GeocodeResponse
import com.swapna.foodapp.data.remote.dto.LocationDto
import com.swapna.foodapp.data.remote.dto.RatingDto
import com.swapna.foodapp.data.remote.dto.RestaurantDto
import com.swapna.foodapp.data.remote.dto.RestaurantWrapper
import com.swapna.foodapp.data.remote.dto.SearchResponse
import com.swapna.foodapp.data.repository.RestaurantRepositoryImpl
import com.swapna.foodapp.domain.model.SearchFilters
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
        coEvery { menuItemDao.getMenuByRestaurant(any()) } returns flowOf(emptyList())
        coEvery { menuItemDao.insertAll(any()) } just Runs
        coEvery { menuItemDao.clearForRestaurant(any()) } just Runs
    }

    afterEach {
        Dispatchers.resetMain()
    }

    // getNearbyRestaurants
    test("getNearbyRestaurants: emits cached data when Room has restaurants") {
        // Pre-populate mock DAO with cached entity
        val entity = fakeRestaurantEntity("r1", "Meghana Foods")
        every { restaurantDao.getAllRestaurants() } returns flowOf(listOf(entity))

        // API fails — simulates offline
        coEvery { api.getNearbyRestaurants() } throws IOException("No internet")

        val result = createRepo().getNearbyRestaurants().first()

        result.isSuccess shouldBe true
        result.getOrNull()?.first()?.name shouldBe "Meghana Foods"
    }

    test("getNearbyRestaurants: emits failure when Room empty and API fails") {
        // Room is empty — no cache
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.getNearbyRestaurants() } throws IOException("No internet")

        val result = createRepo().getNearbyRestaurants().first()

        result.isFailure shouldBe true
    }

    test("getNearbyRestaurants: saves API response to Room") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf("r1" to "Meghana Foods", "r2" to "Empire")
        )

        // Collect all emissions
        createRepo().getNearbyRestaurants().first()

        // Verify Room insertAll was called with 2 restaurants
        coVerify { restaurantDao.insertAll(match { it.size == 2 }) }
    }

    test("getNearbyRestaurants: emits cache first then fresh data") {
        // Start with stale cache
        val staleEntity = fakeRestaurantEntity("r1", "Old Name")
        every { restaurantDao.getAllRestaurants() } returns flowOf(listOf(staleEntity))

        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf("r1" to "New Name")
        )

        val emissions = mutableListOf<String>()
        createRepo().getNearbyRestaurants().collect { result ->
            result.getOrNull()?.firstOrNull()?.name?.let { emissions.add(it) }
        }

        // First emission = cached name
        emissions.first() shouldBe "Old Name"
        // Last emission = fresh name
        emissions.last() shouldBe "New Name"
    }

    // getCollections
    test("getCollections: returns mapped collections from API") {
        coEvery { api.getCollections() } returns CollectionsResponse(
            collections = listOf(
                CollectionWrapper(CollectionDto(1, "Trending", "Hot", "", 20, "60% OFF")),
                CollectionWrapper(CollectionDto(2, "New", "Fresh", "", 10, "Free Delivery")),
            )
        )

        val result = createRepo().getCollections().first()

        result.isSuccess shouldBe true
        result.getOrNull()!! shouldHaveSize 2
        result.getOrNull()!![0].title shouldBe "Trending"
        result.getOrNull()!![0].discount shouldBe "60% OFF"
    }

    test("getCollections: returns empty list when API throws — non-critical") {
        coEvery { api.getCollections() } throws IOException("No internet")

        val result = createRepo().getCollections().first()

        // Non-critical → success with empty list, NOT failure
        result.isSuccess shouldBe true
        result.getOrNull()!!.shouldBeEmpty()
    }

    // getCategories
    test("getCategories: returns mapped categories from API") {
        coEvery { api.getCategories() } returns fakeCategoriesResponse()

        val result = createRepo().getCategories().first()

        result.isSuccess shouldBe true
        result.getOrNull()!! shouldHaveSize 3
        result.getOrNull()!![0].name shouldBe "Biryani"
    }

    test("getCategories: returns empty list on API failure — non-critical") {
        coEvery { api.getCategories() } throws Exception("Error")

        val result = createRepo().getCategories().first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.shouldBeEmpty()
    }

    // getRestaurantDetail
    test("getRestaurantDetail: serves cached restaurant first") {
        val entity = fakeRestaurantEntity("r1", "Meghana Foods")
        coEvery { restaurantDao.getById("r1") } returns entity

        // API offline
        coEvery { api.getRestaurantDetail() } throws IOException("Timeout")

        val result = createRepo().getRestaurantDetail("r1").first()

        result.isSuccess shouldBe true
        result.getOrNull()?.name shouldBe "Meghana Foods"
    }

    test("getRestaurantDetail: fails when no cache and no network") {
        coEvery { restaurantDao.getById(any()) } returns null
        coEvery { api.getRestaurantDetail() } throws IOException("No internet")

        val result = createRepo().getRestaurantDetail("r1").first()

        result.isFailure shouldBe true
    }

    test("getRestaurantDetail: saves fresh data to Room") {
        coEvery { restaurantDao.getById(any()) } returns null
        coEvery { api.getRestaurantDetail() } returns fakeRestaurantDto("r1", "Fresh Restaurant")

        createRepo().getRestaurantDetail("r1").first()

        // Verify Room insert was called
        coVerify { restaurantDao.insert(match { it.name == "Fresh Restaurant" }) }
    }

    // searchRestaurants
    test("searchRestaurants: returns results from API") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.searchRestaurants() } returns fakeSearchResponse(
            listOf("r1" to "Pizza Hut", "r2" to "Dominos")
        )

        val result = createRepo().searchRestaurants(
            "pizza", SearchFilters()
        ).first()

        result.isSuccess shouldBe true
        result.getOrNull()!! shouldHaveSize 2
    }

    test("searchRestaurants: emits cached restaurants when API fails") {
        val entities = listOf(
            fakeRestaurantEntity("r1", "Meghana Foods"),
            fakeRestaurantEntity("r2", "Empire"),
        )
        every { restaurantDao.getAllRestaurants() } returns flowOf(entities)
        coEvery { api.searchRestaurants() } throws IOException("No internet")

        val results = mutableListOf<Result<List<*>>>()
        createRepo().searchRestaurants(
            "Meghana", SearchFilters()
        ).collect { results.add(it) }

        // Should have emitted cached result
        results.isNotEmpty() shouldBe true
        results.first().isSuccess shouldBe true
    }
})

// ── Test helper functions

fun fakeRestaurantEntity(id: String, name: String) = RestaurantEntity(
    id = id,
    name = name,
    imageUrl = "",
    thumbUrl = "",
    rating = 4.2,
    ratingText = "Very Good",
    ratingColor = "5BA829",
    totalVotes = 100,
    avgDeliveryTime = 30,
    deliveryFee = 30.0,
    minOrder = 0,
    cuisinesJson = "[\"Biryani\"]",
    address = "Bengaluru",
    locality = "Koramangala",
    isOpen = true,
    hasDelivery = true,
)

fun fakeRestaurantDto(id: String, name: String) = RestaurantDto(
    id = id,
    name = name,
    featuredImage = "",
    thumb = "",
    location = LocationDto("Bengaluru", "Koramangala", "Bengaluru", 4, "12.93", "77.62", "560095"),
    cuisines = "Biryani, South Indian",
    avgCostForTwo = 600,
    priceRange = 2,
    currency = "Rs.",
    rating = RatingDto("4.2", "Very Good", "5BA829", "1000"),
    hasDelivery = 1,
    isDeliveringNow = 1,
    deliveryTime = 30,
    minOrder = 100,
)

fun fakeGeocodeResponse(restaurants: List<Pair<String, String>>) = GeocodeResponse(
    location = LocationDto("Bengaluru", "Koramangala", "Bengaluru", 4, "12.93", "77.62", "560095"),
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
        CategoryWrapper(CategoryDto(1, "Biryani")),
        CategoryWrapper(CategoryDto(2, "Pizza")),
        CategoryWrapper(CategoryDto(3, "Burger")),
    )
)