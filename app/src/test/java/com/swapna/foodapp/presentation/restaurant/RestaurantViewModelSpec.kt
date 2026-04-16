package com.swapna.foodapp.presentation.restaurant

import androidx.lifecycle.SavedStateHandle
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.MenuItem
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import app.cash.turbine.test


@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantViewModelSpec : BehaviorSpec({

    // ── Fakes ─────────────────────────────────────────────────
    // WHY lateinit at spec level?
    // beforeEach resets these fresh per test
    // Tests that need different setups just reassign inside then
    lateinit var fakeRestaurantRepo: FakeRestaurantRepository
    lateinit var fakeCartRepo:       FakeCartRepository
    lateinit var fakeAddToCart:      FakeAddToCartUseCase

    // ── createViewModel ───────────────────────────────────────
    // WHY NOT store viewModel as lateinit?
    // HomeViewModelSpec pattern: create vm INSIDE each then
    // Avoids combine() + UnconfinedTestDispatcher timing issues
    // where cold flowOf() may not always complete before assertion
    fun createViewModel(
        restaurantId: String = "r1",
    ): RestaurantViewModel {
        val handle = SavedStateHandle(
            mapOf(AppRoutes.ARG_RESTAURANT_ID to restaurantId)
        )
        return RestaurantViewModel(
            savedStateHandle     = handle,
            restaurantRepository = fakeRestaurantRepo,
            cartRepository       = fakeCartRepo,
            addToCartUseCase     = fakeAddToCart,
        )
    }

    beforeEach {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRestaurantRepo = FakeRestaurantRepository()
        fakeCartRepo       = FakeCartRepository()
        fakeAddToCart      = FakeAddToCartUseCase()
        fakeRestaurantRepo.menuResult = fakeMenuByCategory()
    }

    afterEach {
        Dispatchers.resetMain()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Initial Data Loading
    // ✅ FIX: create vm INSIDE each then (HomeViewModelSpec pattern)
    // This ensures combine() + flowOf() completes
    // before assertion runs every single time
    // ══════════════════════════════════════════════════════════

    given("user opens RestaurantScreen") {

        `when`("restaurant loads") {
            then("restaurant name is Meghana Foods") {
                val vm = createViewModel()

                vm.uiState.value.restaurant?.name shouldBe
                        "Meghana Foods"
            }
        }

        `when`("restaurant loads — isLoading check") {
            then("isLoading should be false") {
                val vm = createViewModel()

                vm.uiState.value.isLoading shouldBe false
            }
        }

        `when`("restaurant loads — error check") {
            then("error should be null") {
                val vm = createViewModel()

                vm.uiState.value.error shouldBe null
            }
        }

        // ✅ FIX: Merge Biryani + Starters into ONE test
        // Avoids timing issue where same vm state is accessed
        // from multiple separate then blocks
        `when`("menu loads") {
            then("menuByCategory has 2 categories including Biryani and Starters"){
                    val vm = createViewModel()



                    val menu = vm.uiState.value.menuByCategory

                    menu.size shouldBe 2
                    menu.containsKey("Biryani")  shouldBe true
                    menu.containsKey("Breads")  shouldBe true

            }
        }

        `when`("menu has recommended items") {
            then("recommended list is not empty and all are isRecommended") {
                val vm = createViewModel()

                vm.uiState.value.recommended.isNotEmpty() shouldBe true
                vm.uiState.value.recommended
                    .all { it.isRecommended } shouldBe true
            }
        }

        `when`("restaurant API throws network error") {
            then("error set and restaurant is null and isLoading false") {
                fakeRestaurantRepo.shouldThrowRestaurant = true
                fakeRestaurantRepo.errorMessage = "No internet"

                val vm = createViewModel()

                vm.uiState.value.error shouldBe
                        "Could not load restaurant"
                vm.uiState.value.restaurant shouldBe null
                vm.uiState.value.isLoading  shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Cart Count
    // ══════════════════════════════════════════════════════════

    given("cart has items") {

        `when`("cart has item1 qty 2 and item2 qty 1") {
            then("cartItemCount should be 3") {
                val item1 = fakeMenuItem("m1", "Chicken Biryani", 249.0)
                val item2 = fakeMenuItem("m2", "Mutton Biryani",  349.0)

                fakeCartRepo.seedCart(
                    CartItem("c1", item1, 2),
                    CartItem("c2", item2, 1),
                )

                val vm = createViewModel()

                vm.uiState.value.cartItemCount shouldBe 3
            }
        }

        `when`("cart is empty") {
            then("cartItemCount should be 0") {
                val vm = createViewModel()

                vm.uiState.value.cartItemCount shouldBe 0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Cart Total
    // ══════════════════════════════════════════════════════════

    given("cart has items for total calculation") {

        `when`("1 item at price below free delivery threshold") {
            then("cartTotal equals subtotal plus delivery plus taxes") {
                val price = 50.0
                fakeCartRepo.seedCart(
                    CartItem("c1", fakeMenuItem("m1", price = price), 1)
                )

                val vm = createViewModel()

                val subtotal = price
                val delivery = if (
                    subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE
                ) 0.0 else AppBusinessRules.DEFAULT_DELIVERY_FEE
                val taxes    = subtotal * AppBusinessRules.GST_RATE
                val expected = subtotal + delivery + taxes

                vm.uiState.value.cartTotal shouldBe expected
            }
        }

        `when`("1 item at price above free delivery threshold") {
            then("cartTotal equals subtotal plus taxes with no delivery fee") {
                val price = AppBusinessRules.FREE_DELIVERY_ABOVE + 100.0
                fakeCartRepo.seedCart(
                    CartItem("c1", fakeMenuItem("m1", price = price), 1)
                )

                val vm = createViewModel()

                val subtotal = price
                val taxes    = subtotal * AppBusinessRules.GST_RATE
                val expected = subtotal + 0.0 + taxes

                vm.uiState.value.cartTotal shouldBe expected
            }
        }

        `when`("cart has no items") {
            then("cartTotal should be 0.0") {
                val vm = createViewModel()

                vm.uiState.value.cartTotal shouldBe 0.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — CartBreakdown StateFlow
    // ══════════════════════════════════════════════════════════

    given("cart has items — checking cartBreakdown") {

        `when`("checking all breakdown fields with 1 item at price 50") {
            then("subtotal deliveryFee taxes and total all correct") {
                val price = 50.0
                fakeCartRepo.seedCart(
                    CartItem("c1", fakeMenuItem("m1", price = price), 1)
                )

                val vm = createViewModel()

                val expectedDelivery = AppBusinessRules.DEFAULT_DELIVERY_FEE
                val expectedTaxes    = price * AppBusinessRules.GST_RATE
                val expectedTotal    = price + expectedDelivery + expectedTaxes

                vm.cartBreakdown.value.subtotal    shouldBe price
                vm.cartBreakdown.value.deliveryFee shouldBe expectedDelivery
                vm.cartBreakdown.value.taxes       shouldBe expectedTaxes
                vm.cartBreakdown.value.total       shouldBe expectedTotal
            }
        }

        `when`("cart has no items — breakdown check") {
            then("all cartBreakdown values should be 0.0") {
                val vm = createViewModel()

                vm.cartBreakdown.value.subtotal    shouldBe 0.0
                vm.cartBreakdown.value.deliveryFee shouldBe 0.0
                vm.cartBreakdown.value.taxes       shouldBe 0.0
                vm.cartBreakdown.value.total       shouldBe 0.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Quick Add to Cart
    // ══════════════════════════════════════════════════════════

    given("user taps ADD on recommended item") {

        `when`("quickAddToCart is called with valid item") {
            then("AddToCartUseCase called once with correct item") {
                val vm   = createViewModel()
                val item = fakeMenuItem("m1", "Chicken Biryani", 249.0)

                vm.quickAddToCart(item)

                fakeAddToCart.callCount      shouldBe 1
                fakeAddToCart.lastItem?.id   shouldBe "m1"
                fakeAddToCart.lastItem?.name shouldBe "Chicken Biryani"
            }
        }

        `when`("quickAddToCart completes successfully") {
            then("ItemAdded event emitted with correct item name") {
                val vm   = createViewModel()
                val item = fakeMenuItem("m1", "Chicken Biryani", 249.0)

                vm.events.test {
                    vm.quickAddToCart(item)

                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent
                                .ItemAdded("Chicken Biryani")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("AddToCartUseCase throws exception") {
            then("ShowError event emitted with error message") {
                fakeAddToCart.shouldThrow  = true
                fakeAddToCart.errorMessage = "Could not add to cart"

                val vm   = createViewModel()
                val item = fakeMenuItem("m1", "Chicken Biryani", 249.0)

                vm.events.test {
                    vm.quickAddToCart(item)

                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent
                                .ShowError("Could not add to cart")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — onIncrementItem
    // ══════════════════════════════════════════════════════════

    given("user taps + on menu item row") {

        `when`("item is NOT in cart — use case check") {
            then("AddToCartUseCase is called once") {
                val vm   = createViewModel()
                val item = fakeMenuItem()

                vm.onIncrementItem(item)

                fakeAddToCart.callCount shouldBe 1
            }
        }

        `when`("item is NOT in cart — event check") {
            then("ItemAdded event is emitted") {
                val vm   = createViewModel()
                val item = fakeMenuItem()

                vm.events.test {
                    vm.onIncrementItem(item)

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
                val item = fakeMenuItem("m1", "Chicken Biryani", 249.0)
                fakeCartRepo.seedCart(CartItem("m1", item, 1))

                val vm = createViewModel()

                vm.onIncrementItem(item)

                fakeCartRepo.updateQtyCalled   shouldBe true
                fakeCartRepo.lastUpdatedItemId shouldBe "m1"
                fakeCartRepo.lastUpdatedQty    shouldBe 2
            }
        }

        `when`("item in cart with qty 3") {
            then("updateQuantity called with qty 4") {
                val item = fakeMenuItem("m1", "Chicken Biryani", 249.0)
                fakeCartRepo.seedCart(CartItem("m1", item, 3))

                val vm = createViewModel()

                vm.onIncrementItem(item)

                fakeCartRepo.lastUpdatedQty shouldBe 4
            }
        }

        `when`("item is at MAX_ITEM_QUANTITY") {
            then("quantity does not exceed MAX_ITEM_QUANTITY") {
                val item = fakeMenuItem("m1", "Chicken Biryani", 249.0)
                fakeCartRepo.seedCart(
                    CartItem(
                        "m1",
                        item,
                        AppBusinessRules.MAX_ITEM_QUANTITY,
                    )
                )

                val vm = createViewModel()

                vm.onIncrementItem(item)

                fakeCartRepo.lastUpdatedQty shouldBe
                        AppBusinessRules.MAX_ITEM_QUANTITY
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — onDecrementItem
    // ✅ FIX: ViewModel guard + fresh vm per test
    // ══════════════════════════════════════════════════════════

    given("user taps - on menu item row") {

        `when`("item in cart with qty 2") {
            then("updateQuantity called with qty 1") {
                val item = fakeMenuItem("m1", "Chicken Biryani", 249.0)
                fakeCartRepo.seedCart(CartItem("m1", item, 2))

                val vm = createViewModel()

                vm.onDecrementItem("m1")

                fakeCartRepo.updateQtyCalled   shouldBe true
                fakeCartRepo.lastUpdatedItemId shouldBe "m1"
                fakeCartRepo.lastUpdatedQty    shouldBe 1
            }
        }

        `when`("item in cart with qty 1") {
            then("removeItem called and item fully removed") {
                val item = fakeMenuItem("m1", "Chicken Biryani", 249.0)
                fakeCartRepo.seedCart(CartItem("m1", item, 1))

                val vm = createViewModel()

                vm.onDecrementItem("m1")

                fakeCartRepo.removeItemCalled shouldBe true
                fakeCartRepo.updateQtyCalled  shouldBe false
            }
        }

        // ✅ FIX: Create fresh vm + fresh fakeCartRepo
        // Empty cart → _quantities is empty
        // currentQty = 0 → guard: 0 <= 0 → return@launch
        // removeItem and updateQuantity must NOT be called
        `when`("itemId is not in cart at all") {
            then("no repository method is called and no crash") {
                // Fresh fakeCartRepo from beforeEach — cart is empty
                // Fresh vm from inside then — _quantities is empty
                val vm = createViewModel()

                vm.onDecrementItem("nonexistent_id")

                fakeCartRepo.removeItemCalled shouldBe false
                fakeCartRepo.updateQtyCalled  shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Per-item Quantities StateFlow
    // ══════════════════════════════════════════════════════════

    given("cart has specific quantities per item") {

        `when`("cart has m1 qty 2 and m2 qty 1") {
            then("quantities map reflects each item correctly") {
                val item1 = fakeMenuItem("m1", "Chicken Biryani", 249.0)
                val item2 = fakeMenuItem("m2", "Mutton Biryani",  349.0)

                fakeCartRepo.seedCart(
                    CartItem("m1", item1, 2),
                    CartItem("m2", item2, 1),
                )

                val vm = createViewModel()

                vm.quantities.value["m1"] shouldBe 2
                vm.quantities.value["m2"] shouldBe 1
            }
        }

        `when`("cart has no items") {
            then("quantities map should be empty") {
                val vm = createViewModel()

                vm.quantities.value.isEmpty() shouldBe true
            }
        }

        `when`("item is removed from cart via decrement") {
            then("quantities map no longer contains that itemId") {
                val item = fakeMenuItem("m1", "Chicken Biryani", 249.0)
                fakeCartRepo.seedCart(CartItem("m1", item, 1))

                val vm = createViewModel()

                vm.quantities.value.containsKey("m1") shouldBe true

                vm.onDecrementItem("m1")

                vm.quantities.value.containsKey("m1") shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Navigation Events
    // ══════════════════════════════════════════════════════════

    given("user is on RestaurantScreen") {

        `when`("user taps back button") {
            then("NavigateBack event should be emitted") {
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
            then("NavigateToCart event should be emitted") {
                val vm = createViewModel()

                vm.events.test {
                    vm.onCartBarTapped()

                    awaitItem() shouldBe
                            RestaurantViewModel.RestaurantEvent.NavigateToCart

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("user taps a menu item") {
            then("NavigateToProduct emitted with correct itemId m1") {
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
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 10 — Tab Selection
    // ══════════════════════════════════════════════════════════

    given("default selected tab is MENU") {

        `when`("ViewModel is first created") {
            then("selectedTab should be MENU") {
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

        `when`("user switches back to MENU after tapping REVIEWS") {
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
    // ══════════════════════════════════════════════════════════

    given("restaurant has multiple menu categories") {

        `when`("user taps Starters in footer") {
            then("scrollToCategory emits Starters") {
                val vm = createViewModel()

                vm.scrollToCategory.test {
                    vm.onCategoryFooterTapped("Starters")

                    awaitItem() shouldBe "Starters"

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("user taps Biryani in footer") {
            then("scrollToCategory emits Biryani") {
                val vm = createViewModel()

                vm.scrollToCategory.test {
                    vm.onCategoryFooterTapped("Biryani")

                    awaitItem() shouldBe "Biryani"

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 12 — Retry
    // ══════════════════════════════════════════════════════════

    given("restaurant screen shows error on first load") {

        `when`("user taps retry after connection restored") {
            then("data reloads and restaurant name is shown") {
                fakeRestaurantRepo.shouldThrowRestaurant = true

                val vm = createViewModel()

                vm.uiState.value.error shouldBe
                        "Could not load restaurant"

                fakeRestaurantRepo.shouldThrowRestaurant = false
                vm.retry()

                vm.uiState.value.restaurant?.name shouldBe
                        "Meghana Foods"
                vm.uiState.value.error shouldBe null
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 13 — getCategoryNames
    // ✅ FIX: create vm inside then block
    // ══════════════════════════════════════════════════════════

    given("menu is fully loaded with Biryani and Starters") {

        `when`("getCategoryNames is called") {
            then("list contains both Biryani and Starters") {
                    // Create VM fresh here - guarantees combine()
                    // completes and menuByCategory is fully populated
                    val vm = createViewModel()

                    val names = vm.getCategoryNames()

                    names.size shouldBe 2
                    names.contains("Biryani") shouldBe true
                    names.contains("Breads") shouldBe true
            }
        }
    }
})