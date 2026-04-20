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

    val api           = mockk<FoodApi>()
    val restaurantDao = mockk<RestaurantDao>()
    val menuItemDao   = mockk<MenuItemDao>()
    // WHY real mappers not mocks?
    // Same reason as CartRepositoryImplSpec
    // Mappers are pure Kotlin — no Android deps
    // Real mappers exercise actual mapping logic
    val restaurantMapper = RestaurantMapper()
    val menuMapper       = MenuMapper()
    val entityMapper     = EntityMapper()

    fun createRepo() = RestaurantRepositoryImpl(
        api           = api,
        restaurantDao = restaurantDao,
        menuItemDao   = menuItemDao,
        restaurantMapper = restaurantMapper,
        menuMapper       = menuMapper,
        entityMapper     = entityMapper,
        ioDispatcher     = UnconfinedTestDispatcher(),
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { restaurantDao.getAllRestaurants() }       returns flowOf(emptyList())
        coEvery { restaurantDao.insertAll(any()) }        just Runs
        coEvery { restaurantDao.insert(any()) }           just Runs
        coEvery { restaurantDao.getById(any()) }          returns null
        coEvery { restaurantDao.count() }                 returns 0
        every { menuItemDao.getMenuByRestaurant(any()) }  returns flowOf(emptyList())
        coEvery { menuItemDao.insertAll(any()) }          just Runs
        coEvery { menuItemDao.clearForRestaurant(any()) } just Runs
    }

    afterEach { Dispatchers.resetMain() }

    // ══════════════════════════════════════════════════════════
    // getNearbyRestaurants
    // ══════════════════════════════════════════════════════════

    test("getNearbyRestaurants: emits cached data when Room has restaurants") {
        val entity = fakeRestaurantEntity("r1", "Meghana Foods")
        every { restaurantDao.getAllRestaurants() } returns flowOf(listOf(entity))
        coEvery { api.getNearbyRestaurants() } throws IOException("No internet")

        val result = createRepo().getNearbyRestaurants().first()

        result.isSuccess shouldBe true
        result.getOrNull()?.first()?.name shouldBe "Meghana Foods"
    }

    test("getNearbyRestaurants: emits failure when Room empty and API fails") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.getNearbyRestaurants() } throws IOException("No internet")

        val result = createRepo().getNearbyRestaurants().first()

        result.isFailure shouldBe true
    }

    // ✅ NEW — non-IOException (general Exception branch)
    // WHY? impl has two catch blocks: IOException and Exception
    // Without this test, the general Exception branch is never covered
    test("getNearbyRestaurants: emits failure on general exception when cache empty") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.getNearbyRestaurants() } throws Exception("Server error")

        val result = createRepo().getNearbyRestaurants().first()

        result.isFailure shouldBe true
    }

    // ✅ NEW — general exception with cache present — should NOT emit failure
    // WHY? impl: if (cachedEntities.isEmpty()) → only emit failure if no cache
    // With cache present + general exception → silently serve cache
    test("getNearbyRestaurants: does not emit failure on exception when cache exists") {
        val entity = fakeRestaurantEntity("r1", "Meghana Foods")
        every { restaurantDao.getAllRestaurants() } returns flowOf(listOf(entity))
        coEvery { api.getNearbyRestaurants() } throws Exception("Server error")

        val results = mutableListOf<Result<List<*>>>()
        createRepo().getNearbyRestaurants().collect { results.add(it) }

        // Only one emission — the cache — no failure
        results.size shouldBe 1
        results.first().isSuccess shouldBe true
    }

    test("getNearbyRestaurants: saves API response to Room") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf("r1" to "Meghana Foods", "r2" to "Empire")
        )

        createRepo().getNearbyRestaurants().first()

        coVerify { restaurantDao.insertAll(match { it.size == 2 }) }
    }

    test("getNearbyRestaurants: emits cache first then fresh data") {
        val staleEntity = fakeRestaurantEntity("r1", "Old Name")
        every { restaurantDao.getAllRestaurants() } returns flowOf(listOf(staleEntity))
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf("r1" to "New Name")
        )

        val emissions = mutableListOf<String>()
        createRepo().getNearbyRestaurants().collect { result ->
            result.getOrNull()?.firstOrNull()?.name?.let { emissions.add(it) }
        }

        emissions.first() shouldBe "Old Name"
        emissions.last()  shouldBe "New Name"
    }

    // ✅ NEW — fresh data restaurant count
    test("getNearbyRestaurants: fresh data has correct restaurant count") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf("r1" to "Meghana Foods", "r2" to "Pizza Hut", "r3" to "Burger King")
        )

        val result = createRepo().getNearbyRestaurants().first()

        result.getOrNull()!! shouldHaveSize 3
    }

    // ══════════════════════════════════════════════════════════
    // getCollections
    // ══════════════════════════════════════════════════════════

    test("getCollections: returns mapped collections from API") {
        coEvery { api.getCollections() } returns CollectionsResponse(
            collections = listOf(
                CollectionWrapper(CollectionDto(1, "Trending", "Hot", "", 20, "60% OFF")),
                CollectionWrapper(CollectionDto(2, "New",      "Fresh", "", 10, "Free Delivery")),
            )
        )

        val result = createRepo().getCollections().first()

        result.isSuccess shouldBe true
        result.getOrNull()!! shouldHaveSize 2
        result.getOrNull()!![0].title    shouldBe "Trending"
        result.getOrNull()!![0].discount shouldBe "60% OFF"
    }

    test("getCollections: returns empty list when API throws — non-critical") {
        coEvery { api.getCollections() } throws IOException("No internet")

        val result = createRepo().getCollections().first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.shouldBeEmpty()
    }

    // ✅ NEW — verify collection fields mapped correctly
    test("getCollections: maps collection id and restaurantCount correctly") {
        coEvery { api.getCollections() } returns CollectionsResponse(
            collections = listOf(
                CollectionWrapper(CollectionDto(42, "Weekend Special", "Desc", "", 15, "30% off"))
            )
        )

        val result = createRepo().getCollections().first()

        result.getOrNull()!!.first().id              shouldBe 42
        result.getOrNull()!!.first().restaurantCount shouldBe 15
    }

    // ══════════════════════════════════════════════════════════
    // getCategories
    // ══════════════════════════════════════════════════════════

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

    // ✅ NEW — category id mapped correctly
    test("getCategories: maps category id correctly") {
        coEvery { api.getCategories() } returns CategoriesResponse(
            categories = listOf(CategoryWrapper(CategoryDto(99, "Sushi")))
        )

        val result = createRepo().getCategories().first()

        result.getOrNull()!!.first().id shouldBe 99
    }

    // ══════════════════════════════════════════════════════════
    // getRestaurantDetail
    // ══════════════════════════════════════════════════════════

    test("getRestaurantDetail: serves cached restaurant first") {
        val entity = fakeRestaurantEntity("r1", "Meghana Foods")
        coEvery { restaurantDao.getById("r1") } returns entity
        coEvery { api.getNearbyRestaurants() } throws IOException("Timeout")

        val result = createRepo().getRestaurantDetail("r1").first()

        result.isSuccess shouldBe true
        result.getOrNull()?.name shouldBe "Meghana Foods"
    }

    test("getRestaurantDetail: fails when no cache and no network") {
        coEvery { restaurantDao.getById(any()) } returns null
        coEvery { api.getNearbyRestaurants() } throws IOException("No internet")

        val result = createRepo().getRestaurantDetail("r1").first()

        result.isFailure shouldBe true
    }

    test("getRestaurantDetail: saves fresh data to Room") {
        coEvery { restaurantDao.getById(any()) } returns null
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf("r1" to "Fresh Restaurant")
        )

        createRepo().getRestaurantDetail("r1").first()

        coVerify {
            restaurantDao.insertAll(
                match { entities -> entities.any { it.name == "Fresh Restaurant" } }
            )
        }
    }

    // ✅ NEW — restaurant found in API response
    // WHY? impl searches allRestaurants.find { it.id == id }
    // Must verify the correct restaurant is returned from API list
    test("getRestaurantDetail: returns correct restaurant when found in API response") {
        coEvery { restaurantDao.getById(any()) } returns null
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf("r1" to "Meghana Foods", "r2" to "Pizza Hut")
        )

        val result = createRepo().getRestaurantDetail("r1").first()

        result.isSuccess shouldBe true
        result.getOrNull()?.name shouldBe "Meghana Foods"
    }

    // ✅ NEW — restaurant NOT found in API response
    // WHY? if (found != null) else emit failure
    // Without this test — the else branch is never covered
    test("getRestaurantDetail: returns failure when id not found in API response") {
        coEvery { restaurantDao.getById(any()) } returns null
        coEvery { api.getNearbyRestaurants() } returns fakeGeocodeResponse(
            listOf("r2" to "Pizza Hut")  // r1 not in list
        )

        val result = createRepo().getRestaurantDetail("r1").first()

        result.isFailure shouldBe true
    }

    // ✅ NEW — cached restaurant id matches
    test("getRestaurantDetail: cached restaurant has correct id") {
        val entity = fakeRestaurantEntity("r1", "Meghana Foods")
        coEvery { restaurantDao.getById("r1") } returns entity

        val result = createRepo().getRestaurantDetail("r1").first()

        result.getOrNull()?.id shouldBe "r1"
    }

    // ══════════════════════════════════════════════════════════
    // getMenuItems
    // ✅ NEW — completely missing in original
    // WHY critical? RestaurantScreen shows menu — must work correctly
    // ══════════════════════════════════════════════════════════

    test("getMenuItems: serves cached menu when Room has items") {
        val entity = fakeMenuItemEntity("m1", "r1", "Chicken Biryani", "Biryani")
        every { menuItemDao.getMenuByRestaurant("r1") } returns flowOf(listOf(entity))
        coEvery { api.getDailyMenu() } throws Exception("No internet")

        val result = createRepo().getMenuItems("r1").first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.containsKey("Biryani") shouldBe true
    }

    test("getMenuItems: emits failure when no cache and API fails") {
        every { menuItemDao.getMenuByRestaurant(any()) } returns flowOf(emptyList())
        coEvery { api.getDailyMenu() } throws Exception("No internet")

        val result = createRepo().getMenuItems("r1").first()

        result.isFailure shouldBe true
    }

    test("getMenuItems: fetches from API when cache empty") {
        every { menuItemDao.getMenuByRestaurant(any()) } returns flowOf(emptyList())
        coEvery { api.getDailyMenu() } returns fakeDailyMenuResponse()

        val result = createRepo().getMenuItems("r1").first()

        result.isSuccess shouldBe true
    }

    test("getMenuItems: saves API response to Room") {
        every { menuItemDao.getMenuByRestaurant(any()) } returns flowOf(emptyList())
        coEvery { api.getDailyMenu() } returns fakeDailyMenuResponse()

        createRepo().getMenuItems("r1").first()

        // WHY verify clearForRestaurant?
        // impl clears old items before inserting fresh ones
        // Prevents stale menu items remaining after refresh
        coVerify { menuItemDao.clearForRestaurant("r1") }
        coVerify { menuItemDao.insertAll(any()) }
    }

    test("getMenuItems: groups items by category") {
        every { menuItemDao.getMenuByRestaurant(any()) } returns flowOf(emptyList())
        coEvery { api.getDailyMenu() } returns fakeDailyMenuResponse()

        val result = createRepo().getMenuItems("r1").first()
        val menuMap = result.getOrNull()!!

        menuMap.containsKey("Biryani") shouldBe true
        menuMap["Biryani"]!! shouldHaveSize 1
    }

    test("getMenuItems: cached items grouped by category correctly") {
        val entities = listOf(
            fakeMenuItemEntity("m1", "r1", "Chicken Biryani", "Biryani"),
            fakeMenuItemEntity("m2", "r1", "Mutton Biryani",  "Biryani"),
            fakeMenuItemEntity("m3", "r1", "Chicken 65",      "Starters"),
        )
        every { menuItemDao.getMenuByRestaurant("r1") } returns flowOf(entities)
        coEvery { api.getDailyMenu() } throws Exception("No internet")

        val result = createRepo().getMenuItems("r1").first()
        val menuMap = result.getOrNull()!!

        menuMap.size shouldBe 2
        menuMap["Biryani"]!!  shouldHaveSize 2
        menuMap["Starters"]!! shouldHaveSize 1
    }

    // ══════════════════════════════════════════════════════════
    // getReviews
    // ✅ NEW — completely missing in original
    // ══════════════════════════════════════════════════════════

    test("getReviews: returns mapped reviews from API") {
        coEvery { api.getReviews() } returns fakeReviewsResponse()

        val result = createRepo().getReviews("r1").first()

        result.isSuccess shouldBe true
        result.getOrNull()!! shouldHaveSize 2
    }

    test("getReviews: returns empty list on API failure — non-critical") {
        coEvery { api.getReviews() } throws Exception("No internet")

        val result = createRepo().getReviews("r1").first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.shouldBeEmpty()
    }

    test("getReviews: maps review fields correctly") {
        coEvery { api.getReviews() } returns ReviewsResponse(
            totalCount = 1,
            reviews = listOf(
                ReviewWrapper(ReviewDto(
                    id       = 1L,
                    rating   = 5,
                    text     = "Amazing food!",
                    timeAgo  = "2 days ago",
                    user     = ReviewUserDto(name = "Swapna", profileImage = null),
                ))
            )
        )

        val result = createRepo().getReviews("r1").first()
        val review = result.getOrNull()!!.first()

        review.id        shouldBe 1L
        review.rating    shouldBe 5
        review.text      shouldBe "Amazing food!"
        review.timeAgo   shouldBe "2 days ago"
        review.userName  shouldBe "Swapna"
        review.userImage shouldBe ""  // null profileImage → ""
    }

    // ✅ NEW — null userImage maps to empty string
    // WHY? userImage = wrapper.review.user.profileImage ?: ""
    test("getReviews: null userImage maps to empty string") {
        coEvery { api.getReviews() } returns ReviewsResponse(
            totalCount = 1,
            reviews = listOf(
                ReviewWrapper(ReviewDto(
                    id = 1L, rating = 4, text = "Good", timeAgo = "1 day",
                    user = ReviewUserDto(name = "User", profileImage = null)
                ))
            )
        )

        val review = createRepo().getReviews("r1").first().getOrNull()!!.first()
        review.userImage shouldBe ""
    }

    // ✅ NEW — non-null userImage maps correctly
    test("getReviews: non-null userImage maps correctly") {
        coEvery { api.getReviews() } returns ReviewsResponse(
            totalCount = 1,
            reviews = listOf(
                ReviewWrapper(ReviewDto(
                    id = 1L, rating = 4, text = "Good", timeAgo = "1 day",
                    user = ReviewUserDto(name = "User", profileImage = "https://img.jpg")
                ))
            )
        )

        val review = createRepo().getReviews("r1").first().getOrNull()!!.first()
        review.userImage shouldBe "https://img.jpg"
    }

    // ══════════════════════════════════════════════════════════
    // searchRestaurants
    // ══════════════════════════════════════════════════════════

    test("searchRestaurants: returns results from API") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.searchRestaurants() } returns fakeSearchResponse(
            listOf("r1" to "Pizza Hut", "r2" to "Dominos")
        )

        val result = createRepo().searchRestaurants("pizza", SearchFilters()).first()

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
        createRepo().searchRestaurants("Meghana", SearchFilters())
            .collect { results.add(it) }

        results.isNotEmpty() shouldBe true
        results.first().isSuccess shouldBe true
    }

    // ✅ NEW — empty cache + API failure = failure emitted
    // WHY? impl: if (cached.isEmpty()) emit(Result.failure(e))
    // Without this test — that branch never exercised
    test("searchRestaurants: emits failure when cache empty and API fails") {
        every { restaurantDao.getAllRestaurants() } returns flowOf(emptyList())
        coEvery { api.searchRestaurants() } throws Exception("Server error")

        val result = createRepo().searchRestaurants("pizza", SearchFilters()).first()

        result.isFailure shouldBe true
    }

    // ✅ NEW — blank query skips cache filter
    // WHY? impl: if (cached.isNotEmpty() && query.isNotBlank())
    // Blank query must not apply cache filter — go straight to API
    test("searchRestaurants: blank query skips cache filter and hits API") {
        val entities = listOf(fakeRestaurantEntity("r1", "Meghana Foods"))
        every { restaurantDao.getAllRestaurants() } returns flowOf(entities)
        coEvery { api.searchRestaurants() } returns fakeSearchResponse(
            listOf("r1" to "Meghana Foods")
        )

        val results = mutableListOf<Result<List<*>>>()
        createRepo().searchRestaurants("", SearchFilters()).collect { results.add(it) }

        // Only API result — no cache emission for blank query
        results.size shouldBe 1
    }

    // ✅ NEW — cache filter by name
    test("searchRestaurants: cached results filtered by query name") {
        val entities = listOf(
            fakeRestaurantEntity("r1", "Meghana Foods"),
            fakeRestaurantEntity("r2", "Pizza Hut"),
        )
        every { restaurantDao.getAllRestaurants() } returns flowOf(entities)
        coEvery { api.searchRestaurants() } throws IOException("No internet")

        val result = createRepo()
            .searchRestaurants("meghana", SearchFilters()).first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.size shouldBe 1
        result.getOrNull()!!.first().name shouldBe "Meghana Foods"
    }

    // ══════════════════════════════════════════════════════════
    // getCuisines
    // ✅ NEW — completely missing in original
    // ══════════════════════════════════════════════════════════

    test("getCuisines: returns mapped cuisines from API") {
        coEvery { api.getCuisines() } returns fakeCuisinesResponse()

        val result = createRepo().getCuisines().first()

        result.isSuccess shouldBe true
        result.getOrNull()!! shouldHaveSize 3
        result.getOrNull()!![0].name shouldBe "Biryani"
    }

    test("getCuisines: returns empty list on API failure — non-critical") {
        coEvery { api.getCuisines() } throws Exception("No internet")

        val result = createRepo().getCuisines().first()

        result.isSuccess shouldBe true
        result.getOrNull()!!.shouldBeEmpty()
    }

    test("getCuisines: maps cuisine id correctly") {
        coEvery { api.getCuisines() } returns CuisinesResponse(
            cuisines = listOf(CuisineWrapper(CuisineDto(id = 42, name = "Thai")))
        )

        val result = createRepo().getCuisines().first()

        result.getOrNull()!!.first().id   shouldBe 42
        result.getOrNull()!!.first().name shouldBe "Thai"
    }
})

// ── Test helper functions ─────────────────────────────────────

fun fakeRestaurantEntity(id: String, name: String) = RestaurantEntity(
    id              = id,
    name            = name,
    imageUrl        = "",
    thumbUrl        = "",
    rating          = 4.2,
    ratingText      = "Very Good",
    ratingColor     = "5BA829",
    totalVotes      = 100,
    avgDeliveryTime = 30,
    deliveryFee     = 30.0,
    minOrder        = 0,
    cuisinesJson    = """["Biryani"]""",
    address         = "Bengaluru",
    locality        = "Koramangala",
    isOpen          = true,
    hasDelivery     = true,
    offersJson      = "[]",
    avgCostForTwo   = 600,
    distanceKm      = 0.0,
    phoneNumber     = "",
    openingHours    = "",
    highlightsJson  = "[]",
    knownFor        = "",
)

fun fakeMenuItemEntity(
    id:           String,
    restaurantId: String,
    name:         String,
    category:     String,
) = MenuItemEntity(
    id                 = id,
    restaurantId       = restaurantId,
    name               = name,
    description        = "",
    price              = 249.0,
    imageUrl           = "",
    category           = category,
    isVeg              = false,
    isRecommended      = false,
    isBestseller       = false,
    isAvailable        = true,
    customisationsJson = "[]",
)

fun fakeRestaurantDto(id: String, name: String) = RestaurantDto(
    id              = id,
    name            = name,
    featuredImage   = "",
    thumb           = "",
    location        = LocationDto(
        "Bengaluru", "Koramangala", "Bengaluru", 4, "12.93", "77.62", "560095"
    ),
    cuisines        = "Biryani, South Indian",
    avgCostForTwo   = 600,
    priceRange      = 2,
    currency        = "Rs.",
    rating          = RatingDto("4.2", "Very Good", "5BA829", "1000"),
    hasDelivery     = 1,
    isDeliveringNow = 1,
    deliveryTime    = 30,
    minOrder        = 100,
)

fun fakeGeocodeResponse(
    restaurants: List<Pair<String, String>>,
) = GeocodeResponse(
    location = LocationDto(
        "Bengaluru", "Koramangala", "Bengaluru", 4, "12.93", "77.62", "560095"
    ),
    nearbyRestaurants = restaurants.map { (id, name) ->
        RestaurantWrapper(fakeRestaurantDto(id, name))
    },
)

fun fakeSearchResponse(
    restaurants: List<Pair<String, String>>,
) = SearchResponse(
    totalFound  = restaurants.size,
    shown       = restaurants.size,
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

// ✅ NEW helpers for missing tests

fun fakeDailyMenuResponse() = DailyMenuResponse(
    dailyMenus = listOf(
        DailyMenuWrapper(
            menu = DailyMenuDto(
                id     = "menu_1",
                name   = "Biryani",
                dishes = listOf(
                    DishWrapper(
                        dish = DishDto(
                            id            = "m1",
                            name          = "Chicken Biryani",
                            price         = "249",
                            description   = "Delicious",
                            imageUrl      = null,
                            isVeg         = 0,
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
    totalCount = 2,
    reviews = listOf(
        ReviewWrapper(ReviewDto(
            id      = 1L,
            rating  = 5,
            text    = "Amazing food!",
            timeAgo = "2 days ago",
            user    = ReviewUserDto(name = "Swapna", profileImage = null),
        )),
        ReviewWrapper(ReviewDto(
            id      = 2L,
            rating  = 4,
            text    = "Good biryani",
            timeAgo = "1 week ago",
            user    = ReviewUserDto(name = "Priya", profileImage = "https://img.jpg"),
        )),
    )
)

fun fakeCuisinesResponse() = CuisinesResponse(
    cuisines = listOf(
        CuisineWrapper(CuisineDto(1, "Biryani")),
        CuisineWrapper(CuisineDto(2, "Pizza")),
        CuisineWrapper(CuisineDto(3, "Burger")),
    )
)