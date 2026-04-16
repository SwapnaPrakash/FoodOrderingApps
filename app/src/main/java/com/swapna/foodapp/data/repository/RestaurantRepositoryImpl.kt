package com.swapna.foodapp.data.repository

import com.swapna.foodapp.data.local.dao.MenuItemDao
import com.swapna.foodapp.data.local.dao.RestaurantDao
import com.swapna.foodapp.data.mapper.EntityMapper
import com.swapna.foodapp.data.mapper.MenuMapper
import com.swapna.foodapp.data.mapper.RestaurantMapper
import com.swapna.foodapp.data.remote.api.FoodApi
import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.Cuisine
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.Review
import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.utils.AppConstants.COULD_NOT_LOAD_MENU
import com.swapna.foodapp.utils.AppConstants.NO_INTERNET
import com.swapna.foodapp.utils.AppConstants.NO_INTERNET_LOAD_RESTAURANT
import com.swapna.foodapp.utils.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import javax.inject.Inject

class RestaurantRepositoryImpl @Inject constructor(
    private val api: FoodApi,
    private val restaurantDao: RestaurantDao,
    private val menuItemDao: MenuItemDao,
    private val restaurantMapper: RestaurantMapper,
    private val menuMapper: MenuMapper,
    private val entityMapper: EntityMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RestaurantRepository {

    // GET NEARBY RESTAURANTS — Offline-first
    override fun getNearbyRestaurants():
            Flow<Result<List<Restaurant>>> = flow {

        // Step 1: Serve cache immediately
        val cachedEntities = restaurantDao
            .getAllRestaurants()
            .first()

        if (cachedEntities.isNotEmpty()) {
            val cachedDomain = cachedEntities.map {
                entityMapper.restaurantToDomain(it)
            }
            emit(Result.success(cachedDomain))
        }

        // Step 2: Fetch fresh from geocode.json
        try {
            val response    = api.getNearbyRestaurants()
            val freshDomain = response.nearbyRestaurants.map {
                restaurantMapper.toDomain(it.restaurant)
            }

            // Save ALL restaurants to Room with their ids
            // This is what getRestaurantDetail reads later
            val freshEntities = freshDomain.map {
                entityMapper.restaurantToEntity(it)
            }
            restaurantDao.insertAll(freshEntities)

            emit(Result.success(freshDomain))

        } catch (e: IOException) {
            if (cachedEntities.isEmpty()) {
                emit(Result.failure(IOException(NO_INTERNET)))
            }
        } catch (e: Exception) {
            if (cachedEntities.isEmpty()) {
                emit(Result.failure(e))
            }
        }

    }.flowOn(ioDispatcher)

    // GET COLLECTIONS — (Exciting Offers)
    override fun getCollections(): Flow<Result<List<Collections>>> = flow {
        try {
            val response = api.getCollections()
            val collections = response.collections.map { wrapper ->
                Collections(
                    id = wrapper.collection.id,
                    title = wrapper.collection.title,
                    description = wrapper.collection.description,
                    imageUrl = wrapper.collection.imageUrl,
                    restaurantCount = wrapper.collection.restaurantCount,
                    discount = wrapper.collection.discount ?: "",
                )
            }
            emit(Result.success(collections))
        } catch (e: Exception) {
            // Collections are non-critical — emit empty list on failure
            // Home screen still works without offers
            emit(Result.success(emptyList()))
        }
    }.flowOn(ioDispatcher)

    // GET CATEGORIES
    override fun getCategories(): Flow<Result<List<FoodCategory>>> = flow {
        try {
            val response = api.getCategories()
            val categories = response.categories.map { wrapper ->
                FoodCategory(
                    id = wrapper.category.id,
                    name = wrapper.category.name,
                    imageUrl = wrapper.category.imageUrl ?: "",
                )
            }
            emit(Result.success(categories))
        } catch (e: Exception) {
            emit(Result.success(emptyList()))
        }
    }.flowOn(ioDispatcher)

    // GET RESTAURANT DETAIL
    override fun getRestaurantDetail(
        id: String,
    ): Flow<Result<Restaurant>> = flow {

        android.util.Log.d(
            "REPO",
            "getRestaurantDetail called id=$id"
        )

        // ✅ FIX: Read from Room by id
        // Room was populated by getNearbyRestaurants()
        // getById(id) returns only the restaurant matching this id
        val cached = restaurantDao.getById(id)

        if (cached != null) {
            // ✅ This returns DIFFERENT restaurant per id
            // id=101 → Meghana Foods
            // id=102 → Pizza Hut
            // id=103 → Burger King
            android.util.Log.d(
                "REPO",
                "Found in Room: ${cached.name}"
            )
            emit(
                Result.success(
                    entityMapper.restaurantToDomain(cached)
                )
            )
        } else {
            // Room cache empty (first launch, no internet before)
            // Try fetching all restaurants fresh and find this one
            android.util.Log.d(
                "REPO",
                "Not in Room, fetching from API id=$id"
            )
            try {
                val response = api.getNearbyRestaurants()
                val allRestaurants = response.nearbyRestaurants.map {
                    restaurantMapper.toDomain(it.restaurant)
                }

                // Save all to Room
                val entities = allRestaurants.map {
                    entityMapper.restaurantToEntity(it)
                }
                restaurantDao.insertAll(entities)

                // Find the one we need by id
                val found = allRestaurants.find { it.id == id }

                if (found != null) {
                    android.util.Log.d(
                        "REPO",
                        "Not in Room, fetching from API id=$id"
                    )
                    emit(Result.success(found))
                } else {
                    android.util.Log.d(
                        "REPO",
                        "Found in API response: ${found?.name}"
                    )
                    emit(
                        Result.failure(
                            Exception(NO_INTERNET_LOAD_RESTAURANT)
                        )
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e(
                    "REPO",
                    "Failed to load restaurant id=$id: ${e.message}"
                )
                emit(
                    Result.failure(
                        Exception(NO_INTERNET_LOAD_RESTAURANT)
                    )
                )
            }
        }

    }.flowOn(ioDispatcher)
    // GET MENU ITEMS — with Room caching per restaurant
    override fun getMenuItems(
        restaurantId: String,
    ): Flow<Result<Map<String, List<MenuItem>>>> = flow {

        // Serve cached menu
        val cachedItems = menuItemDao
            .getMenuByRestaurant(restaurantId)
            .first()

        if (cachedItems.isNotEmpty()) {
            val cachedMap = cachedItems
                .map { entityMapper.menuItemToDomain(it) }
                .groupBy { it.category }
            emit(Result.success(cachedMap))
        }

        // Fetch fresh menu
        try {
            val response = api.getDailyMenu()
            val menuMap = menuMapper.toDomain(response, restaurantId)

            // Save to Room — clear old first to avoid stale items
            menuItemDao.clearForRestaurant(restaurantId)
            val entities = menuMap.values
                .flatten()
                .map { entityMapper.menuItemToEntity(it) }
            menuItemDao.insertAll(entities)

            emit(Result.success(menuMap))

        } catch (e: Exception) {
            if (cachedItems.isEmpty()) {
                emit(Result.failure(Exception(COULD_NOT_LOAD_MENU)))
            }
        }

    }.flowOn(ioDispatcher)

    // GET REVIEWS
    override fun getReviews(restaurantId: String): Flow<Result<List<Review>>> = flow {
        try {
            val response = api.getReviews()
            val reviews = response.reviews.map { wrapper ->
                Review(
                    id = wrapper.review.id,
                    rating = wrapper.review.rating,
                    text = wrapper.review.text,
                    timeAgo = wrapper.review.timeAgo,
                    userName = wrapper.review.user.name,
                    userImage = wrapper.review.user.profileImage ?: "",
                )
            }
            emit(Result.success(reviews))
        } catch (e: Exception) {
            emit(Result.success(emptyList()))
        }
    }.flowOn(ioDispatcher)

    // SEARCH RESTAURANTS
    override fun searchRestaurants(
        query: String,
        filters: SearchFilters,
    ): Flow<Result<List<Restaurant>>> = flow {
        val cached = restaurantDao.getAllRestaurants().first()
        if (cached.isNotEmpty() && query.isNotBlank()) {
            val cachedDomain = cached
                .map { entityMapper.restaurantToDomain(it) }
                .filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.cuisines.any { c -> c.contains(query, ignoreCase = true) }
                }
            if (cachedDomain.isNotEmpty()) {
                emit(Result.success(cachedDomain))
            }
        }

        try {
            val response = api.searchRestaurants()
            val restaurants = response.restaurants
                .map { restaurantMapper.toDomain(it.restaurant) }
            emit(Result.success(restaurants))
        } catch (e: Exception) {
            if (cached.isEmpty()) {
                emit(Result.failure(e))
            }
        }
    }.flowOn(ioDispatcher)

    // GET CUISINES
    override fun getCuisines(): Flow<Result<List<Cuisine>>> = flow {
        try {
            val response = api.getCuisines()
            val cuisines = response.cuisines.map { wrapper ->
                Cuisine(
                    id = wrapper.cuisine.id,
                    name = wrapper.cuisine.name,
                )
            }
            emit(Result.success(cuisines))
        } catch (e: Exception) {
            emit(Result.success(emptyList()))
        }
    }.flowOn(ioDispatcher)
}