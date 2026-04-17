package com.swapna.foodapp.presentation.restaurant

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.fakes.FakeAddToCartUseCase
import com.swapna.foodapp.fakes.FakeCartRepository
import com.swapna.foodapp.fakes.FakeRestaurantRepository
import com.swapna.foodapp.fakes.FakeRestaurantRepository.Companion.fakeMenuByCategory
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.fakeMenuItem
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain


@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantViewModelSpec : BehaviorSpec({

    // ── Fakes at spec level ───────────────────────────────────
    // WHY lateinit at spec level?
    // Same as HomeViewModelSpec — fakes declared once
    // Reset fresh in beforeEach per test
    // Each test gets clean fakes = no shared state
    lateinit var fakeRestaurantRepo: FakeRestaurantRepository
    lateinit var fakeCartRepo: FakeCartRepository
    lateinit var fakeAddToCart: FakeAddToCartUseCase

    // ── Fake data at spec level ───────────────────────────────
    // WHY at spec level?
    // HomeViewModelSpec defines fakeCollections, fakeCategories
    // fakeRestaurants at spec level — reused across tests
    // Same pattern here — define menu items once

    val chickenBiryani = fakeMenuItem(
        id = "m1",
        name = "Chicken Biryani",
        price = 249.0,
        isRecommended = true,
        isBestseller = true,
    )
    val muttonBiryani = fakeMenuItem(
        id = "m2",
        name = "Mutton Biryani",
        price = 349.0,
    )
    val chicken65 = fakeMenuItem(
        id = "m3",
        name = "Chicken 65",
        price = 199.0,
        isRecommended = true,
        category = "Starters",
    )
    val paneerTikka = fakeMenuItem(
        id = "m4",
        name = "Paneer Tikka",
        price = 179.0,
        isVeg = true,
        category = "Starters",
    )

    // ── ViewModel factory ─────────────────────────────────────
    // WHY factory function not lateinit?
    // HomeViewModelSpec creates VM inside each then block
    // Factory makes this easy and consistent
    // Each test gets fresh VM with current fakes state
    fun createViewModel(
        restaurantId: String = "r1",
    ): RestaurantViewModel {
        val handle = SavedStateHandle(
            mapOf(AppRoutes.ARG_RESTAURANT_ID to restaurantId)
        )
        return RestaurantViewModel(
            savedStateHandle = handle,
            restaurantRepository = fakeRestaurantRepo,
            cartRepository = fakeCartRepo,
            addToCartUseCase = fakeAddToCart,
        )
    }

    // ── Setup ─────────────────────────────────────────────────
    // WHY fresh fakes in beforeEach not clearAllMocks?
    // HomeViewModelSpec uses clearAllMocks for mockk objects
    // RestaurantViewModel uses Fake classes not mockk
    // Creating new Fake instance = same effect as clearAllMocks
    // Resets all tracking flags + empty cart + default menu
    beforeEach {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRestaurantRepo = FakeRestaurantRepository()
        fakeCartRepo = FakeCartRepository()
        fakeAddToCart = FakeAddToCartUseCase()
        // Default menu: Biryani + Starters categories
        // Set in beforeEach same as HomeViewModelSpec sets
        // every { getHomeDataUseCase() } default in beforeEach
        fakeRestaurantRepo.menuResult = fakeMenuByCategory()
    }

    afterEach {
        Dispatchers.resetMain()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Initial Data Loading
    // WHY test loading?
    // HomeViewModelSpec GROUP 1 tests API returns full home data
    // RestaurantViewModel similarly loads restaurant + menu on init
    // Verify data populated correctly from fake repository
    // ══════════════════════════════════════════════════════════

    given("user opens RestaurantScreen") {

        // ✅ PATTERN: mock setup INSIDE then
        // Same as HomeViewModelSpec:
        // every { getHomeDataUseCase() } returns flowOf(...)
        // placed INSIDE each then block

        `when`("restaurant detail loads successfully") {
            then("restaurant name should be Meghana Foods") {
                // VM created inside then — same as HomeViewModelSpec
                val vm = createViewModel()

                vm.uiState.value.restaurant?.name shouldBe
                        "Meghana Foods"
            }
        }

        `when`("restaurant detail loads — isLoading check") {
            then("isLoading should be false after loading") {
                val vm = createViewModel()

                vm.uiState.value.isLoading shouldBe false
            }
        }

        `when`("restaurant detail loads — error check") {
            then("error should be null on success") {
                val vm = createViewModel()

                vm.uiState.value.error shouldBe null
            }
        }

        // ✅ PATTERN: Merge related assertions into one then
        // Avoids duplicate when() names which cause Kotest issues
        // HomeViewModelSpec merges: collections + categories in
        // "all lists should be empty but no error" test
        `when`("menu loads successfully") {
            then("menuByCategory has 2 categories Biryani and Starters") {
                val vm = createViewModel()
                val menu = vm.uiState.value.menuByCategory

                menu.size shouldBe 2
                menu.containsKey("Biryani") shouldBe true
                menu.containsKey("Starters") shouldBe true
            }
        }

        `when`("menu loads — recommended items check") {
            then("recommended list not empty and all are isRecommended") {
                val vm = createViewModel()

                vm.uiState.value.recommended
                    .isNotEmpty() shouldBe true
                vm.uiState.value.recommended
                    .all { it.isRecommended } shouldBe true
            }
        }

        // ✅ PATTERN: Error case setup INSIDE then
        // HomeViewModelSpec:
        // every { getHomeDataUseCase() } returns
        //     flowOf(Result.failure(...))
        // placed inside then block for error tests
        `when`("restaurant API throws network error") {
            then("error shown restaurant null isLoading false") {
                // Setup error INSIDE then — not in beforeEach
                fakeRestaurantRepo.shouldThrowRestaurant = true
                fakeRestaurantRepo.errorMessage = "No internet"

                val vm = createViewModel()

                vm.uiState.value.error shouldBe
                        "Could not load restaurant"
                vm.uiState.value.restaurant shouldBe null
                vm.uiState.value.isLoading shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Cart Count in TopBar
    // WHY test cart count?
    // HomeViewModelSpec: "cart has 3 items" → cartItemCount = 3
    // Same pattern — RestaurantScreen shows cart badge in TopBar
    // Verify cartItemCount reflects seeded cart correctly
    // ══════════════════════════════════════════════════════════

    given("cart has items — checking cartItemCount") {

        `when`("cart has item1 qty 2 and item2 qty 1") {
            then("cartItemCount should be 3 total") {
                // Seed BEFORE createViewModel()
                // WHY? observeCart() in init collects immediately
                // Data must exist when first emission happens
                fakeCartRepo.seedCart(
                    CartItem("c1", chickenBiryani, 2),
                    CartItem("c2", muttonBiryani, 1),
                )

                val vm = createViewModel()

                vm.uiState.value.cartItemCount shouldBe 3
            }
        }

        `when`("cart is empty") {
            then("cartItemCount should be 0") {
                // No seedCart → fakeCartRepo empty by default
                val vm = createViewModel()

                vm.uiState.value.cartItemCount shouldBe 0
            }
        }

        `when`("cart has 3 single qty items") {
            then("cartItemCount should be 3") {
                fakeCartRepo.seedCart(
                    CartItem("c1", chickenBiryani, 1),
                    CartItem("c2", muttonBiryani, 1),
                    CartItem("c3", chicken65, 1),
                )

                val vm = createViewModel()

                vm.uiState.value.cartItemCount shouldBe 3
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Cart Total in uiState
    // WHY compute dynamically?
    // HomeViewModelSpec never hardcodes expected values
    // Always uses fake data constants for assertions
    // Same here — use AppBusinessRules constants not raw numbers
    // ══════════════════════════════════════════════════════════

    given("cart has items for total calculation") {

        `when`("1 item below free delivery threshold") {
            then("cartTotal equals subtotal plus delivery plus taxes") {
                // 50.0 safely below any reasonable threshold
                val cheapItem = fakeMenuItem("m_cheap", price = 50.0)
                fakeCartRepo.seedCart(CartItem("c1", cheapItem, 1))

                val vm = createViewModel()

                val subtotal = 50.0
                val delivery = AppBusinessRules.DEFAULT_DELIVERY_FEE
                val taxes = subtotal * AppBusinessRules.GST_RATE
                val expected = subtotal + delivery + taxes

                vm.uiState.value.cartTotal shouldBe expected
            }
        }

        `when`("1 item above free delivery threshold") {
            then("cartTotal equals subtotal plus taxes only") {
                val price = AppBusinessRules.FREE_DELIVERY_ABOVE + 100.0
                val item = fakeMenuItem("m_exp", price = price)
                fakeCartRepo.seedCart(CartItem("c1", item, 1))

                val vm = createViewModel()

                val taxes = price * AppBusinessRules.GST_RATE
                val expected = price + 0.0 + taxes

                vm.uiState.value.cartTotal shouldBe expected
            }
        }

        `when`("cart is empty — total check") {
            then("cartTotal should be 0.0") {
                val vm = createViewModel()

                vm.uiState.value.cartTotal shouldBe 0.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — CartBreakdown StateFlow
    // Verify individual breakdown fields: subtotal, delivery,
    // taxes, total — all computed by FakeCartRepository
    // using same AppBusinessRules as real CartRepositoryImpl
    // ══════════════════════════════════════════════════════════

    given("cart has items — checking cartBreakdown fields") {

        `when`("1 item at price 50 — all fields checked") {
            then("subtotal deliveryFee taxes total all correct") {
                val price = 50.0
                fakeCartRepo.seedCart(
                    CartItem("c1", fakeMenuItem("m1", price = price), 1)
                )

                val vm = createViewModel()

                val expectedDelivery =
                    AppBusinessRules.DEFAULT_DELIVERY_FEE
                val expectedTaxes =
                    price * AppBusinessRules.GST_RATE
                val expectedTotal =
                    price + expectedDelivery + expectedTaxes

                // All in one then — avoids duplicate when names
                vm.cartBreakdown.value.subtotal shouldBe price
                vm.cartBreakdown.value.deliveryFee shouldBe
                        expectedDelivery
                vm.cartBreakdown.value.taxes shouldBe
                        expectedTaxes
                vm.cartBreakdown.value.total shouldBe
                        expectedTotal
            }
        }

        `when`("cart empty — breakdown zeros check") {
            then("all cartBreakdown fields should be 0.0") {
                val vm = createViewModel()

                vm.cartBreakdown.value.subtotal shouldBe 0.0
                vm.cartBreakdown.value.deliveryFee shouldBe 0.0
                vm.cartBreakdown.value.taxes shouldBe 0.0
                vm.cartBreakdown.value.total shouldBe 0.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Quick Add to Cart (Recommended section)
    // ✅ PATTERN: Turbine .test{} for ALL event assertions
    // HomeViewModelSpec:
    //   vm.events.test {
    //       vm.onRestaurantClicked("r_001")
    //       val event = awaitItem()
    //       event shouldBe NavigateToRestaurant("r_001")
    //       cancelAndIgnoreRemainingEvents()
    //   }
    // Exact same pattern used here for all event tests
    // ══════════════════════════════════════════════════════════

    given("user taps ADD on recommended item") {

        // ✅ UNIQUE when() name — no duplicates
        // HomeViewModelSpec uses unique names like:
        // "onRestaurantClicked is called with 'r_001'"
        // "onCartClicked is called"
        // "onSearchClicked is called"
        `when`("quickAddToCart called with valid menu item") {
            then("AddToCartUseCase called once with correct item") {
                val vm = createViewModel()

                vm.quickAddToCart(chickenBiryani)

                fakeAddToCart.callCount shouldBe 1
                fakeAddToCart.lastItem?.id shouldBe "m1"
                fakeAddToCart.lastItem?.name shouldBe "Chicken Biryani"
            }
        }

        `when`("quickAddToCart completes successfully") {
            then("ItemAdded event emitted with Chicken Biryani name") {
                val vm = createViewModel()

                // ✅ Turbine: subscribe FIRST → then trigger action
                // Solves SharedFlow replay=0 race condition
                // Same as HomeViewModelSpec events.test{} pattern
                vm.events.test {
                    vm.quickAddToCart(chickenBiryani)

                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent
                                .ItemAdded("Chicken Biryani")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("quickAddToCart with Mutton Biryani succeeds") {
            then("ItemAdded emitted with Mutton Biryani name") {
                val vm = createViewModel()

                vm.events.test {
                    vm.quickAddToCart(muttonBiryani)

                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent
                                .ItemAdded("Mutton Biryani")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("AddToCartUseCase throws exception") {
            then("ShowError event emitted with error message") {
                // Setup error INSIDE then — HomeViewModelSpec pattern
                fakeAddToCart.shouldThrow = true
                fakeAddToCart.errorMessage = "Could not add to cart"

                val vm = createViewModel()

                vm.events.test {
                    vm.quickAddToCart(chickenBiryani)

                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent
                                .ShowError("Could not add to cart")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — onIncrementItem (Menu row + button)
    // When item NOT in cart → AddToCartUseCase called (first add)
    // When item IN cart → updateQuantity called (increment)
    // ══════════════════════════════════════════════════════════

    given("user taps + on menu item row") {

        `when`("item is NOT in cart — use case called check") {
            then("AddToCartUseCase called once") {
                // No seedCart → item not in cart
                val vm = createViewModel()

                vm.onIncrementItem(chickenBiryani)

                fakeAddToCart.callCount shouldBe 1
            }
        }

        `when`("item is NOT in cart — event check") {
            then("ItemAdded event emitted on first add") {
                val vm = createViewModel()

                vm.events.test {
                    vm.onIncrementItem(chickenBiryani)

                    val event = awaitItem()

                    assert(
                        event is RestaurantViewModel
                        .RestaurantEvent.ItemAdded
                    ) { "Expected ItemAdded but got $event" }

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("item already in cart with qty 1") {
            then("updateQuantity called with qty 2") {
                fakeCartRepo.seedCart(
                    CartItem("m1", chickenBiryani, 1)
                )

                val vm = createViewModel()

                vm.onIncrementItem(chickenBiryani)

                fakeCartRepo.updateQtyCalled shouldBe true
                fakeCartRepo.lastUpdatedItemId shouldBe "m1"
                fakeCartRepo.lastUpdatedQty shouldBe 2
            }
        }

        `when`("item in cart with qty 3") {
            then("updateQuantity called with qty 4") {
                fakeCartRepo.seedCart(
                    CartItem("m1", chickenBiryani, 3)
                )

                val vm = createViewModel()

                vm.onIncrementItem(chickenBiryani)

                fakeCartRepo.lastUpdatedQty shouldBe 4
            }
        }

        `when`("item is at MAX_ITEM_QUANTITY") {
            then("quantity capped at MAX not exceeded") {
                fakeCartRepo.seedCart(
                    CartItem(
                        "m1",
                        chickenBiryani,
                        AppBusinessRules.MAX_ITEM_QUANTITY,
                    )
                )

                val vm = createViewModel()

                vm.onIncrementItem(chickenBiryani)

                fakeCartRepo.lastUpdatedQty shouldBe
                        AppBusinessRules.MAX_ITEM_QUANTITY
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — onDecrementItem (Menu row - button)
    // qty > 1 → updateQuantity
    // qty == 1 → removeItem
    // qty == 0 (not in cart) → guard → nothing called
    // ══════════════════════════════════════════════════════════

    given("user taps - on menu item row") {

        `when`("item in cart with qty 2") {
            then("updateQuantity called with qty 1") {
                fakeCartRepo.seedCart(
                    CartItem("m1", chickenBiryani, 2)
                )

                val vm = createViewModel()

                vm.onDecrementItem("m1")

                fakeCartRepo.updateQtyCalled shouldBe true
                fakeCartRepo.lastUpdatedItemId shouldBe "m1"
                fakeCartRepo.lastUpdatedQty shouldBe 1
            }
        }

        `when`("item in cart with qty 1") {
            then("removeItem called — item fully removed") {
                fakeCartRepo.seedCart(
                    CartItem("m1", chickenBiryani, 1)
                )

                val vm = createViewModel()

                vm.onDecrementItem("m1")

                fakeCartRepo.removeItemCalled shouldBe true
                fakeCartRepo.updateQtyCalled shouldBe false
            }
        }

        `when`("itemId not in cart at all") {
            then("nothing called no crash — guard works") {
                // Empty cart → _quantities["nonexistent"] = null → 0
                // Guard: 0 <= 0 → return@launch
                val vm = createViewModel()

                vm.onDecrementItem("nonexistent_id")

                fakeCartRepo.removeItemCalled shouldBe false
                fakeCartRepo.updateQtyCalled shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Per-item Quantities StateFlow
    // _quantities = Map<itemId, qty> drives ADD vs [- qty +] UI
    // Test that quantities reflect cart state correctly
    // ══════════════════════════════════════════════════════════

    given("cart has specific quantities per item") {

        `when`("cart has m1 qty 2 and m2 qty 1") {
            then("quantities map has correct values for each itemId") {
                fakeCartRepo.seedCart(
                    CartItem("m1", chickenBiryani, 2),
                    CartItem("m2", muttonBiryani, 1),
                )

                val vm = createViewModel()

                vm.quantities.value["m1"] shouldBe 2
                vm.quantities.value["m2"] shouldBe 1
            }
        }

        `when`("cart is empty — quantities check") {
            then("quantities map should be empty") {
                val vm = createViewModel()

                vm.quantities.value.isEmpty() shouldBe true
            }
        }

        `when`("item removed from cart via decrement") {
            then("quantities map no longer contains that itemId") {
                fakeCartRepo.seedCart(
                    CartItem("m1", chickenBiryani, 1)
                )

                val vm = createViewModel()

                // Before: item exists
                vm.quantities.value.containsKey("m1") shouldBe true

                // Decrement to 0 → removeItem → quantities updated
                vm.onDecrementItem("m1")

                // After: item gone
                vm.quantities.value.containsKey("m1") shouldBe false
            }
        }

        `when`("quantity increments from 1 to 2") {
            then("quantities map updates to 2 for that itemId") {
                fakeCartRepo.seedCart(
                    CartItem("m1", chickenBiryani, 1)
                )

                val vm = createViewModel()

                vm.quantities.value["m1"] shouldBe 1

                vm.onIncrementItem(chickenBiryani)

                vm.quantities.value["m1"] shouldBe 2
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Navigation Events
    // ✅ ALL use Turbine .test{} — HomeViewModelSpec pattern
    // HomeViewModelSpec:
    //   vm.events.test {
    //       vm.onRestaurantClicked("r_001")
    //       val event = awaitItem()
    //       event shouldBe NavigateToRestaurant("r_001")
    //       cancelAndIgnoreRemainingEvents()
    //   }
    // ══════════════════════════════════════════════════════════

    given("user is on RestaurantScreen") {

        `when`("user taps back button") {
            then("NavigateBack event emitted") {
                val vm = createViewModel()

                vm.events.test {
                    vm.onBackPressed()

                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent.NavigateBack

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("user taps cart bar") {
            then("NavigateToCart event emitted") {
                val vm = createViewModel()

                vm.events.test {
                    vm.onCartBarTapped()

                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent.NavigateToCart

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("user taps menu item with id m1") {
            then("NavigateToProduct emitted with itemId m1") {
                val vm = createViewModel()

                vm.events.test {
                    vm.onMenuItemTapped("m1")

                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent
                                .NavigateToProduct("m1")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("user taps menu item with id m3") {
            then("NavigateToProduct emitted with itemId m3") {
                val vm = createViewModel()

                vm.events.test {
                    vm.onMenuItemTapped("m3")

                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent
                                .NavigateToProduct("m3")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 10 — Tab Selection
    // WHY test tabs?
    // HomeViewModelSpec tests DeliveryTab selection
    // Same pattern for MenuTab (MENU / REVIEWS)
    // ══════════════════════════════════════════════════════════

    given("default selected tab is MENU") {

        `when`("ViewModel first created") {
            then("selectedTab should default to MENU") {
                val vm = createViewModel()

                vm.uiState.value.selectedTab shouldBe
                        RestaurantViewModel.MenuTab.MENU
            }
        }

        `when`("user taps REVIEWS tab") {
            then("selectedTab changes to REVIEWS") {
                val vm = createViewModel()

                vm.onTabSelected(RestaurantViewModel.MenuTab.REVIEWS)

                vm.uiState.value.selectedTab shouldBe
                        RestaurantViewModel.MenuTab.REVIEWS
            }
        }

        `when`("user switches back to MENU after REVIEWS") {
            then("selectedTab goes back to MENU") {
                val vm = createViewModel()

                vm.onTabSelected(RestaurantViewModel.MenuTab.REVIEWS)
                vm.onTabSelected(RestaurantViewModel.MenuTab.MENU)

                vm.uiState.value.selectedTab shouldBe
                        RestaurantViewModel.MenuTab.MENU
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 11 — Scroll to Category
    // scrollToCategory is separate SharedFlow
    // Uses same Turbine pattern as events
    // ══════════════════════════════════════════════════════════

    given("restaurant has multiple menu categories") {

        `when`("user taps Starters in category footer") {
            then("scrollToCategory emits Starters") {
                val vm = createViewModel()

                // ✅ Turbine on scrollToCategory SharedFlow
                // Same pattern as events.test{}
                vm.scrollToCategory.test {
                    vm.onCategoryFooterTapped("Starters")

                    awaitItem() shouldBe "Starters"

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("user taps Biryani in category footer") {
            then("scrollToCategory emits Biryani") {
                val vm = createViewModel()

                vm.scrollToCategory.test {
                    vm.onCategoryFooterTapped("Biryani")

                    awaitItem() shouldBe "Biryani"

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("user taps Main Course in category footer") {
            then("scrollToCategory emits Main Course") {
                val vm = createViewModel()

                vm.scrollToCategory.test {
                    vm.onCategoryFooterTapped("Main Course")

                    awaitItem() shouldBe "Main Course"

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 12 — Retry on Error
    // WHY test retry?
    // HomeViewModelSpec: "first load fails, then retry succeeds"
    // Same pattern — verify retry reloads data
    // ══════════════════════════════════════════════════════════

    given("restaurant screen shows error on first load") {

        `when`("user taps retry after connection restored") {
            then("data reloads and restaurant name shown") {
                // First load fails
                fakeRestaurantRepo.shouldThrowRestaurant = true
                val vm = createViewModel()

                vm.uiState.value.error shouldBe
                        "Could not load restaurant"
                vm.uiState.value.restaurant shouldBe null

                // Fix connection → retry
                fakeRestaurantRepo.shouldThrowRestaurant = false
                vm.retry()

                // Data loads correctly
                vm.uiState.value.restaurant?.name shouldBe
                        "Meghana Foods"
                vm.uiState.value.error shouldBe null
            }
        }

        `when`("menu fails but restaurant loads successfully") {
            then("error shown and menu is empty") {
                fakeRestaurantRepo.shouldThrowMenu = true
                val vm = createViewModel()

                // Restaurant loads fine
                vm.uiState.value.restaurant?.name shouldBe
                        "Meghana Foods"
                // Menu failed
                vm.uiState.value.menuByCategory.isEmpty() shouldBe true
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 13 — getCategoryNames
    // Utility function used by RestaurantScreen for sticky headers
    // ══════════════════════════════════════════════════════════

    given("menu is fully loaded with Biryani and Starters") {

        `when`("getCategoryNames called") {
            then("returns list containing Biryani and Starters") {
                val vm = createViewModel()
                val names = vm.getCategoryNames()

                names.size shouldBe 2
                names.contains("Biryani") shouldBe true
                names.contains("Starters") shouldBe true
            }
        }

        `when`("getCategoryNames called — size check") {
            then("list size equals menuByCategory size") {
                val vm = createViewModel()

                // getCategoryNames should return same count as
                // keys in menuByCategory map
                vm.getCategoryNames().size shouldBe
                        vm.uiState.value.menuByCategory.size
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 14 — Different Restaurant IDs
    // WHY test different IDs?
    // HomeViewModelSpec tests different API outcomes
    // Verify ViewModel correctly uses SavedStateHandle restaurantId
    // ══════════════════════════════════════════════════════════

    given("RestaurantScreen opened with different restaurant IDs") {

        `when`("opened with restaurantId r1") {
            then("SavedStateHandle correctly passes r1") {
                // createViewModel("r1") sets ARG_RESTAURANT_ID to r1
                val vm = createViewModel(restaurantId = "r1")

                // restaurantId exposed from ViewModel
                vm.restaurantId shouldBe "r1"
            }
        }

        `when`("opened with restaurantId r2") {
            then("SavedStateHandle correctly passes r2") {
                val vm = createViewModel(restaurantId = "r2")

                vm.restaurantId shouldBe "r2"
            }
        }
    }
})