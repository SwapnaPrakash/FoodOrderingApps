package com.swapna.foodapp.presentation.product

/*
import com.swapna.foodapp.presentation.navigation.AppRoutes
// ── Kotest ────────────────────────────────────────────────────
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.doubles.plusOrMinus

// ── Coroutines ────────────────────────────────────────────────
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// ── App ───────────────────────────────────────────────────────
import androidx.lifecycle.SavedStateHandle
import com.swapna.foodapp.fakes.FakeAddToCartUseCase
import com.swapna.foodapp.fakes.FakeRestaurantRepository
import com.swapna.foodapp.utils.AppBusinessRules

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModelSpec : BehaviorSpec({

    val dispatcher = UnconfinedTestDispatcher()

    lateinit var fakeRestaurantRepo: FakeRestaurantRepository
    lateinit var fakeAddToCart:      FakeAddToCartUseCase
    lateinit var viewModel:          ProductDetailViewModel

    // ── createViewModel helper ────────────────────────────────
    // WHY helper function?
    //   Most tests need a fresh viewModel
    //   With DIFFERENT itemId or restaurantId
    //   Helper avoids repeating SavedStateHandle setup
    fun createViewModel(
        restaurantId: String = "r1",
        menuItemId:   String = "m1",  // default = item with customisations
    ): ProductDetailViewModel {
        // SavedStateHandle simulates navigation arguments
        // In real app: route "product/r1/m1" → SavedStateHandle
        // In test: we pass the map directly
        val handle = SavedStateHandle(
            mapOf(
                AppRoutes.ARG_RESTAURANT_ID to restaurantId,
                AppRoutes.ARG_MENU_ITEM_ID  to menuItemId,
            )
        )
        return ProductDetailViewModel(
            savedStateHandle     = handle,
            restaurantRepository = fakeRestaurantRepo,
            addToCartUseCase     = fakeAddToCart,
        )
    }

    beforeEach {
        Dispatchers.setMain(dispatcher)
        // Fresh repo per test — no state bleed
        fakeRestaurantRepo = FakeRestaurantRepository()
        fakeAddToCart      = FakeAddToCartUseCase()
        // Create with default item "m1" (has customisations)
        viewModel          = createViewModel()
    }

    afterEach {
        Dispatchers.resetMain()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Load Menu Item
    // Tests what happens when screen first opens
    // ══════════════════════════════════════════════════════════

    given("ProductDetailScreen is opened with item m1") {

        `when`("menu loads successfully") {
            then("uiState.item should be Chicken Biryani") {
                // WHY check item name?
                // Screen must show correct item
                // Wrong item = critical UX bug
                // (user adds wrong item to cart)

                viewModel.uiState.value
                    .item?.name shouldBe "Chicken Biryani"
            }
        }

        `when`("menu loads successfully") {
            then("isLoading should be false") {
                // WHY check isLoading?
                // If true → spinner shows forever
                // User can never interact with screen

                viewModel.uiState.value
                    .isLoading shouldBe false
            }
        }

        `when`("menu loads successfully") {
            then("error should be null") {
                // Clean success state
                viewModel.uiState.value
                    .error shouldBe null
            }
        }

        `when`("menu loads successfully") {
            then("initial quantity should be 1") {
                // WHY 1 not 0?
                // User can't add 0 items to cart
                // Start at 1 = ready to tap Add immediately

                viewModel.uiState.value
                    .quantity shouldBe 1
            }
        }

        `when`("menu loads successfully") {
            then("item should have 2 customisation groups") {
                // Verify customisations loaded with item
                // m1 has: Size group + Spice Level group = 2

                val item = viewModel.uiState.value.item

                item?.customisations?.size shouldBe 2
                item?.customisations
                    ?.any { it.name == "Size" } shouldBe true
                item?.customisations
                    ?.any { it.name == "Spice Level" } shouldBe true
            }
        }

        `when`("menu API throws error") {
            then("error state should be shown") {
                // Set repo to throw before creating VM
                fakeRestaurantRepo.shouldThrowMenu = true
                fakeRestaurantRepo.errorMessage    =
                    "Network error"

                val failVM = createViewModel()

                // isLoading must stop
                failVM.uiState.value.isLoading shouldBe false
                // item must be null (couldn't load)
                failVM.uiState.value.item      shouldBe null
                // error message must be shown
                failVM.uiState.value.error     shouldBe
                        "Failed to load item"
            }
        }

        `when`("itemId does not exist in menu") {
            then("error state shows Item not found") {
                // What if navigation sent wrong itemId?
                // VM must handle gracefully, not crash

                // Menu only has m1 and m2
                // We request m999 → not found
                val notFoundVM = createViewModel(
                    menuItemId = "m999"  // doesn't exist
                )

                notFoundVM.uiState.value.item shouldBe null
                notFoundVM.uiState.value.error shouldBe
                        "Item not found"
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Default Customisation Selections
    // Tests that first option is pre-selected per group
    // ══════════════════════════════════════════════════════════

    given("item with customisations loads") {

        `when`("screen opens") {
            then("first option of Size group is pre-selected") {
                // WHY first option pre-selected?
                // Zomato UX: something must be selected
                // User doesn't have to interact to add to cart
                // First option = cheapest/most common

                // Size group first option = "regular"
                // selectedOptions["size_group"] = "regular"
                val selectedOptions =
                    viewModel.uiState.value.selectedOptions

                selectedOptions["size_group"] shouldBe "regular"
            }
        }

        `when`("screen opens") {
            then("first option of Spice group is pre-selected") {
                // Spice group first option = "mild"
                val selectedOptions =
                    viewModel.uiState.value.selectedOptions

                selectedOptions["spice_group"] shouldBe "mild"
            }
        }

        `when`("item has NO customisations") {
            then("selectedOptions should be empty") {
                // Simple item (e.g. Plain Naan) has no customisations
                // selectedOptions map should be empty
                // No need to pre-select anything

                // Create VM with m2 (Plain Naan — no customisations)
                val simpleVM = createViewModel(menuItemId = "m2")

                simpleVM.uiState.value
                    .selectedOptions shouldBe emptyMap()
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Initial Price Calculation
    // Tests starting price = base + default extras
    // ══════════════════════════════════════════════════════════

    given("item loads with price ₹249") {

        `when`("default selections are applied") {
            then("totalPrice should equal base price (no extras)") {
                // Default: Regular (₹0 extra) + Mild (₹0 extra)
                // Total = (249.0 + 0 + 0) × 1 = 249.0

                // WHY test this?
                // Initial price shown on button
                // If wrong → user sees wrong price immediately

                viewModel.uiState.value
                    .totalPrice shouldBe 249.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Customisation Selection
    // Tests price updates when user selects options
    // ══════════════════════════════════════════════════════════

    given("user is customising their order") {

        `when`("user selects Large size (+₹50)") {
            then("selectedOptions updates to large for size_group") {
                // Verify selection state updated
                viewModel.onOptionSelected(
                    groupId  = "size_group",
                    optionId = "large",
                )

                viewModel.uiState.value
                    .selectedOptions["size_group"] shouldBe "large"
            }
        }

        `when`("user selects Large size (+₹50)") {
            then("totalPrice should be 299.0 — base + extra") {
                // large = +₹50 extra
                // totalPrice = (249.0 + 50.0) × 1 = 299.0

                // WHY test price update?
                // Add to Cart button shows live price
                // User must see correct total before adding

                viewModel.onOptionSelected(
                    groupId  = "size_group",
                    optionId = "large",
                )

                viewModel.uiState.value
                    .totalPrice shouldBe 299.0
            }
        }

        `when`("user selects Extra Large size (+₹100)") {
            then("totalPrice should be 349.0") {
                // extra_large = +₹100
                // totalPrice = (249.0 + 100.0) × 1 = 349.0

                viewModel.onOptionSelected(
                    groupId  = "size_group",
                    optionId = "extra_large",
                )

                viewModel.uiState.value
                    .totalPrice shouldBe 349.0
            }
        }

        `when`("user switches Large back to Regular") {
            then("totalPrice should return to base 249.0") {
                // Select large first
                viewModel.onOptionSelected(
                    groupId  = "size_group",
                    optionId = "large",
                )
                // Price = 299.0

                // Switch back to regular
                viewModel.onOptionSelected(
                    groupId  = "size_group",
                    optionId = "regular",
                )

                // Price back to 249.0
                viewModel.uiState.value
                    .totalPrice shouldBe 249.0
            }
        }

        `when`("user selects option in Spice group") {
            then("size selection is NOT changed") {
                // WHY? Each group is INDEPENDENT
                // Changing spice → size stays same
                // If groups affected each other → bug

                // First select large size
                viewModel.onOptionSelected(
                    groupId  = "size_group",
                    optionId = "large",
                )

                // Then change spice
                viewModel.onOptionSelected(
                    groupId  = "spice_group",
                    optionId = "hot",
                )

                // Size should still be large
                viewModel.uiState.value
                    .selectedOptions["size_group"] shouldBe "large"

                // Spice should be hot
                viewModel.uiState.value
                    .selectedOptions["spice_group"] shouldBe "hot"
            }
        }

        `when`("user selects both Large size + different spice") {
            then("totalPrice includes only size extra (spice is free)") {
                // large = +₹50, hot = +₹0
                // Total = (249.0 + 50.0 + 0.0) × 1 = 299.0

                viewModel.onOptionSelected(
                    groupId  = "size_group",
                    optionId = "large",
                )
                viewModel.onOptionSelected(
                    groupId  = "spice_group",
                    optionId = "hot",
                )

                viewModel.uiState.value
                    .totalPrice shouldBe 299.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Quantity Management
    // Tests + and - buttons on quantity selector
    // ══════════════════════════════════════════════════════════

    given("user adjusts quantity on ProductDetailScreen") {

        `when`("user taps + button once from qty 1") {
            then("quantity should be 2") {
                // Initial qty = 1
                // Tap + → qty = 2

                viewModel.onIncrementQuantity()

                viewModel.uiState.value
                    .quantity shouldBe 2
            }
        }

        `when`("user taps + button twice") {
            then("quantity should be 3") {
                viewModel.onIncrementQuantity()
                viewModel.onIncrementQuantity()

                viewModel.uiState.value
                    .quantity shouldBe 3
            }
        }

        `when`("user taps - button from qty 2") {
            then("quantity should be 1") {
                // Increment first to get to 2
                viewModel.onIncrementQuantity()
                // qty = 2

                viewModel.onDecrementQuantity()
                // qty = 1

                viewModel.uiState.value
                    .quantity shouldBe 1
            }
        }

        `when`("user taps - button when qty is already 1") {
            then("quantity should stay at 1 — minimum") {
                // WHY not go to 0?
                // Cart item with qty 0 makes no sense
                // UI shows remove button for that
                // Decrement at 1 = do nothing (coerceAtLeast(1))

                viewModel.onDecrementQuantity()
                // qty should NOT go to 0

                viewModel.uiState.value
                    .quantity shouldBe 1
            }
        }

        `when`("user increments to MAX_ITEM_QUANTITY") {
            then("quantity should not exceed MAX") {
                // MAX_ITEM_QUANTITY = 10
                // Tapping + at 10 → stays at 10

                // Increment 15 times
                // (5 extra taps beyond MAX)
                repeat(15) {
                    viewModel.onIncrementQuantity()
                }

                viewModel.uiState.value.quantity shouldBe
                        AppBusinessRules.MAX_ITEM_QUANTITY
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Price Updates with Quantity
    // Tests that total = (base + extras) × qty
    // ══════════════════════════════════════════════════════════

    given("user changes quantity") {

        `when`("user increments qty to 2 with no customisations") {
            then("totalPrice should be 249.0 × 2 = 498.0") {
                // Base price = 249.0
                // No extra customisations selected (defaults = 0)
                // qty = 2
                // Total = (249.0 + 0) × 2 = 498.0

                viewModel.onIncrementQuantity()

                viewModel.uiState.value
                    .totalPrice shouldBe 498.0
            }
        }

        `when`("user selects Large (+₹50) and qty 2") {
            then("totalPrice should be (249+50)×2 = 598.0") {
                // Large selected = +₹50 extra
                // qty = 2
                // Total = (249.0 + 50.0) × 2 = 598.0

                viewModel.onOptionSelected(
                    groupId  = "size_group",
                    optionId = "large",
                )
                viewModel.onIncrementQuantity()

                viewModel.uiState.value
                    .totalPrice shouldBe 598.0
            }
        }

        `when`("user selects Large (+₹50) then decrements to 1") {
            then("totalPrice should be (249+50)×1 = 299.0") {
                // Select large
                viewModel.onOptionSelected(
                    groupId  = "size_group",
                    optionId = "large",
                )
                // Increment to 2
                viewModel.onIncrementQuantity()
                // Price = 598.0

                // Decrement back to 1
                viewModel.onDecrementQuantity()
                // Price = (249 + 50) × 1 = 299.0

                viewModel.uiState.value
                    .totalPrice shouldBe 299.0
            }
        }

        `when`("qty is 3 with Extra Large (+₹100)") {
            then("totalPrice should be (249+100)×3 = 1047.0") {
                viewModel.onOptionSelected(
                    groupId  = "size_group",
                    optionId = "extra_large",
                )
                viewModel.onIncrementQuantity() // qty = 2
                viewModel.onIncrementQuantity() // qty = 3

                viewModel.uiState.value
                    .totalPrice shouldBe 1047.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Add to Cart
    // Tests what happens when user taps "Add to Cart"
    // ══════════════════════════════════════════════════════════

    given("user taps Add to Cart button") {

        `when`("item loaded and default selections") {
            then("AddToCartUseCase is called once") {
                // WHY test callCount?
                // Double-tap protection
                // Use case must be called exactly once

                viewModel.onAddToCart()

                fakeAddToCart.callCount shouldBe 1
            }
        }

        `when`("item loaded with default selections") {
            then("AddToCartUseCase called with correct MenuItem") {
                viewModel.onAddToCart()

                // Verify correct item passed to use case
                fakeAddToCart.lastItem?.id shouldBe "m1"
                fakeAddToCart.lastItem?.name shouldBe
                        "Chicken Biryani"
            }
        }

        `when`("user selected qty 2") {
            then("AddToCartUseCase called with qty 2") {
                viewModel.onIncrementQuantity() // qty = 2

                viewModel.onAddToCart()

                fakeAddToCart.lastQty shouldBe 2
            }
        }

        `when`("user selected Large size option") {
            then("AddToCartUseCase called with Large customisation") {
                // WHY test customisations passed?
                // CartItem.totalPrice depends on customisations
                // Wrong customisations → wrong price in cart

                viewModel.onOptionSelected(
                    groupId  = "size_group",
                    optionId = "large",
                )

                viewModel.onAddToCart()

                // Verify large option was passed
                val customisations =
                    fakeAddToCart.lastCustomisations

                customisations.any {
                    it.id == "large"
                } shouldBe true
            }
        }

        `when`("add to cart succeeds") {
            then("ShowSnackbar event emitted with item name") {
                // User gets visual feedback
                // "Chicken Biryani added to cart 🛒"

                viewModel.onAddToCart()

                val event = viewModel.events.first()

                assert(
                    event is ProductDetailViewModel
                    .ProductDetailEvent.ShowSnackbar
                ) { "Expected ShowSnackbar but got $event" }

                (event as ProductDetailViewModel
                .ProductDetailEvent.ShowSnackbar)
                    .message shouldBe
                        "Chicken Biryani added to cart 🛒"
            }
        }

        `when`("add to cart succeeds") {
            then("NavigateBack event emitted after snackbar") {
                // After adding → go back to restaurant screen
                // User sees cart count updated in TopBar

                viewModel.onAddToCart()

                // Collect 2 events:
                // First = ShowSnackbar
                // Second = NavigateBack
                val events = mutableListOf<ProductDetailViewModel.ProductDetailEvent>()

                // Collect both emitted events
                viewModel.events.collect { event ->
                    events.add(event)
                    // Stop after 2 events
                    if (events.size == 2) return@collect
                }

                // Second event must be NavigateBack
                assert(
                    events[1] is ProductDetailViewModel
                    .ProductDetailEvent.NavigateBack
                ) { "Expected NavigateBack but got ${events[1]}" }
            }
        }

        `when`("AddToCartUseCase throws exception") {
            then("ShowError event emitted with error message") {
                // If use case fails → show error to user
                // Don't navigate back

                fakeAddToCart.shouldThrow  = true
                fakeAddToCart.errorMessage = "Cart is full"

                viewModel.onAddToCart()

                val event = viewModel.events.first()

                assert(
                    event is ProductDetailViewModel
                    .ProductDetailEvent.ShowError
                ) { "Expected ShowError but got $event" }

                (event as ProductDetailViewModel
                .ProductDetailEvent.ShowError)
                    .message shouldBe "Cart is full"
            }
        }

        `when`("item not loaded (null)") {
            then("ShowError emitted — cannot add null item") {
                // Edge case: what if item failed to load
                // but user somehow taps Add to Cart?
                // Must handle gracefully

                fakeRestaurantRepo.shouldThrowMenu = true
                val failVM = createViewModel()

                // Item is null (failed to load)
                failVM.uiState.value.item shouldBe null

                failVM.onAddToCart()

                val event = failVM.events.first()

                assert(
                    event is ProductDetailViewModel
                    .ProductDetailEvent.ShowError
                ) { "Expected ShowError but got $event" }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Navigation
    // ══════════════════════════════════════════════════════════

    given("user is on ProductDetailScreen") {

        `when`("user taps back button") {
            then("NavigateBack event is emitted") {
                viewModel.onBackPressed()

                val event = viewModel.events.first()

                assert(
                    event is ProductDetailViewModel
                    .ProductDetailEvent.NavigateBack
                ) { "Expected NavigateBack but got $event" }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Simple Item (no customisations)
    // ══════════════════════════════════════════════════════════

    given("item has NO customisations (Plain Naan)") {

        `when`("screen opens with simple item m2") {
            then("item loads correctly") {
                val simpleVM = createViewModel(menuItemId = "m2")

                simpleVM.uiState.value
                    .item?.name shouldBe "Plain Naan"
            }
        }

        `when`("simple item loaded") {
            then("totalPrice equals base price × qty") {
                // No customisations → no extras
                // Total = 50.0 × 1 = 50.0

                val simpleVM = createViewModel(menuItemId = "m2")

                simpleVM.uiState.value
                    .totalPrice shouldBe 50.0
            }
        }

        `when`("user increments qty on simple item") {
            then("totalPrice updates correctly") {
                val simpleVM = createViewModel(menuItemId = "m2")

                simpleVM.onIncrementQuantity() // qty = 2

                // 50.0 × 2 = 100.0
                simpleVM.uiState.value
                    .totalPrice shouldBe 100.0
            }
        }

        `when`("user adds simple item to cart") {
            then("AddToCartUseCase called with empty customisations") {
                val simpleVM = createViewModel(menuItemId = "m2")

                simpleVM.onAddToCart()

                fakeAddToCart.callCount shouldBe 1
                // No customisations passed
                fakeAddToCart.lastCustomisations
                    .isEmpty() shouldBe true
            }
        }
    }
})*/
