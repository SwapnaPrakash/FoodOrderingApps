package com.swapna.foodapp.presentation.product

// ── Kotest ────────────────────────────────────────────────────

// ── Coroutines ────────────────────────────────────────────────

// ── App ───────────────────────────────────────────────────────


import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.swapna.foodapp.domain.model.Customisation
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.cart.AddToCartUseCase
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.utils.AppBusinessRules
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModelSpec : BehaviorSpec({

    val dispatcher = UnconfinedTestDispatcher()

    // ── MockK dependencies ────────────────────────────────────
    val restaurantRepository = mockk<RestaurantRepository>()
    val addToCartUseCase = mockk<AddToCartUseCase>()

    // ── createViewModel helper ────────────────────────────────
    fun createViewModel(
        restaurantId: String = "r1",
        menuItemId: String = "m1",
    ): ProductDetailViewModel {
        val handle = SavedStateHandle(
            mapOf(
                AppRoutes.ARG_RESTAURANT_ID to restaurantId,
                AppRoutes.ARG_MENU_ITEM_ID to menuItemId,
            )
        )
        return ProductDetailViewModel(
            savedStateHandle = handle,
            restaurantRepository = restaurantRepository,
            addToCartUseCase = addToCartUseCase,
        )
    }

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(dispatcher)

        // Default: success with customisable item m1 + simple item m2
        every { restaurantRepository.getMenuItems(any()) } returns
                flowOf(Result.success(fakeMenuWithCustomisations()))

        // Default: add to cart succeeds silently
        coEvery { addToCartUseCase(any(), any(), any()) } just runs
    }

    afterEach {
        Dispatchers.resetMain()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Load Menu Item
    // ══════════════════════════════════════════════════════════

    given("ProductDetailScreen is opened with item m1") {

        `when`("menu loads successfully") {
            then("uiState.item should be Chicken Biryani") {
                createViewModel().uiState.value
                    .item?.name shouldBe "Chicken Biryani"
            }
        }

        `when`("menu loads successfully") {
            then("isLoading should be false") {
                createViewModel().uiState.value
                    .isLoading shouldBe false
            }
        }

        `when`("menu loads successfully") {
            then("error should be null") {
                createViewModel().uiState.value
                    .error shouldBe null
            }
        }

        `when`("menu loads successfully") {
            then("initial quantity should be 1") {
                createViewModel().uiState.value
                    .quantity shouldBe 1
            }
        }

        `when`("menu loads successfully") {
            then("item should have 2 customisation groups") {
                val item = createViewModel().uiState.value.item

                item?.customisations?.size shouldBe 2
                item?.customisations?.any { it.name == "Size" } shouldBe true
                item?.customisations?.any { it.name == "Spice Level" } shouldBe true
            }
        }

        `when`("menu API throws error") {
            then("error state should be shown") {
                // Override default stub → failure
                every { restaurantRepository.getMenuItems(any()) } returns
                        flowOf(Result.failure(Exception()))

                val vm = createViewModel()

                vm.uiState.value.isLoading shouldBe false
                vm.uiState.value.item shouldBe null
                vm.uiState.value.error shouldBe "Failed to load item"
            }
        }

        `when`("itemId does not exist in menu") {
            then("error state shows Item not found") {
                // m999 not in fake menu → not found
                val vm = createViewModel(menuItemId = "m999")

                vm.uiState.value.item shouldBe null
                vm.uiState.value.error shouldBe "Item not found"
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Default Customisation Selections
    // ══════════════════════════════════════════════════════════

    given("item with customisations loads") {

        `when`("screen opens") {
            then("first option of Size group is pre-selected") {
                createViewModel().uiState.value
                    .selectedOptions["size_group"] shouldBe "regular"
            }
        }

        `when`("screen opens") {
            then("first option of Spice group is pre-selected") {
                createViewModel().uiState.value
                    .selectedOptions["spice_group"] shouldBe "mild"
            }
        }

        `when`("item has NO customisations") {
            then("selectedOptions should be empty") {
                val vm = createViewModel(menuItemId = "m2")

                vm.uiState.value.selectedOptions shouldBe emptyMap()
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Initial Price Calculation
    // ══════════════════════════════════════════════════════════

    given("item loads with price ₹249") {

        `when`("default selections are applied") {
            then("totalPrice should equal base price (no extras)") {
                createViewModel().uiState.value
                    .totalPrice shouldBe 249.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Customisation Selection
    // ══════════════════════════════════════════════════════════

    given("user is customising their order") {

        `when`("user selects Large size (+₹50)") {
            then("selectedOptions updates to large for size_group") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = "size_group", optionId = "large")

                vm.uiState.value.selectedOptions["size_group"] shouldBe "large"
            }
        }

        `when`("user selects Large size (+₹50)") {
            then("totalPrice should be 299.0") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = "size_group", optionId = "large")

                vm.uiState.value.totalPrice shouldBe 299.0
            }
        }

        `when`("user selects Extra Large size (+₹100)") {
            then("totalPrice should be 349.0") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = "size_group", optionId = "extra_large")

                vm.uiState.value.totalPrice shouldBe 349.0
            }
        }

        `when`("user switches Large back to Regular") {
            then("totalPrice should return to base 249.0") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = "size_group", optionId = "large")
                vm.onOptionSelected(groupId = "size_group", optionId = "regular")

                vm.uiState.value.totalPrice shouldBe 249.0
            }
        }

        `when`("user selects option in Spice group") {
            then("size selection is NOT changed") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = "size_group", optionId = "large")
                vm.onOptionSelected(groupId = "spice_group", optionId = "hot")

                vm.uiState.value.selectedOptions["size_group"] shouldBe "large"
                vm.uiState.value.selectedOptions["spice_group"] shouldBe "hot"
            }
        }

        `when`("user selects both Large size + different spice") {
            then("totalPrice includes only size extra (spice is free)") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = "size_group", optionId = "large")
                vm.onOptionSelected(groupId = "spice_group", optionId = "hot")

                vm.uiState.value.totalPrice shouldBe 299.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Quantity Management
    // ══════════════════════════════════════════════════════════

    given("user adjusts quantity on ProductDetailScreen") {

        `when`("user taps + once from qty 1") {
            then("quantity should be 2") {
                val vm = createViewModel()
                vm.onIncrementQuantity()

                vm.uiState.value.quantity shouldBe 2
            }
        }

        `when`("user taps + twice") {
            then("quantity should be 3") {
                val vm = createViewModel()
                vm.onIncrementQuantity()
                vm.onIncrementQuantity()

                vm.uiState.value.quantity shouldBe 3
            }
        }

        `when`("user taps - from qty 2") {
            then("quantity should be 1") {
                val vm = createViewModel()
                vm.onIncrementQuantity()
                vm.onDecrementQuantity()

                vm.uiState.value.quantity shouldBe 1
            }
        }

        `when`("user taps - when qty is already 1") {
            then("quantity should stay at 1") {
                val vm = createViewModel()
                vm.onDecrementQuantity()

                vm.uiState.value.quantity shouldBe 1
            }
        }

        `when`("user increments beyond MAX_ITEM_QUANTITY") {
            then("quantity should not exceed MAX") {
                val vm = createViewModel()
                repeat(15) { vm.onIncrementQuantity() }

                vm.uiState.value.quantity shouldBe
                        AppBusinessRules.MAX_ITEM_QUANTITY
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Price Updates with Quantity
    // ══════════════════════════════════════════════════════════

    given("user changes quantity") {

        `when`("qty incremented to 2 with no extra customisations") {
            then("totalPrice should be 249.0 × 2 = 498.0") {
                val vm = createViewModel()
                vm.onIncrementQuantity()

                vm.uiState.value.totalPrice shouldBe 498.0
            }
        }

        `when`("user selects Large (+₹50) and qty 2") {
            then("totalPrice should be (249+50)×2 = 598.0") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = "size_group", optionId = "large")
                vm.onIncrementQuantity()

                vm.uiState.value.totalPrice shouldBe 598.0
            }
        }

        `when`("user selects Large (+₹50) then decrements to 1") {
            then("totalPrice should be (249+50)×1 = 299.0") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = "size_group", optionId = "large")
                vm.onIncrementQuantity()
                vm.onDecrementQuantity()

                vm.uiState.value.totalPrice shouldBe 299.0
            }
        }

        `when`("qty is 3 with Extra Large (+₹100)") {
            then("totalPrice should be (249+100)×3 = 1047.0") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = "size_group", optionId = "extra_large")
                vm.onIncrementQuantity()
                vm.onIncrementQuantity()

                vm.uiState.value.totalPrice shouldBe 1047.0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Add to Cart
    // ══════════════════════════════════════════════════════════

    given("user taps Add to Cart button") {

        `when`("item loaded and default selections") {
            then("AddToCartUseCase is called once") {
                val vm = createViewModel()
                vm.onAddToCart()

                // coVerify replaces fakeAddToCart.callCount
                coVerify(exactly = 1) {
                    addToCartUseCase(any(), any(), any())
                }
            }
        }

        `when`("item loaded with default selections") {
            then("AddToCartUseCase called with correct MenuItem") {
                val itemSlot = slot<MenuItem>()
                coEvery {
                    addToCartUseCase(capture(itemSlot), any(), any())
                } just runs

                val vm = createViewModel()
                vm.onAddToCart()

                itemSlot.captured.id shouldBe "m1"
                itemSlot.captured.name shouldBe "Chicken Biryani"
            }
        }

        `when`("user selected qty 2") {
            then("AddToCartUseCase called with qty 2") {
                val qtySlot = slot<Int>()
                coEvery {
                    addToCartUseCase(any(), capture(qtySlot), any())
                } just runs

                val vm = createViewModel()
                vm.onIncrementQuantity()
                vm.onAddToCart()

                qtySlot.captured shouldBe 2
            }
        }

        `when`("user selected Large size option") {
            then("AddToCartUseCase called with Large customisation") {
                val customisationsSlot = slot<List<CustomisationOption>>()
                coEvery {
                    addToCartUseCase(any(), any(), capture(customisationsSlot))
                } just runs

                val vm = createViewModel()
                vm.onOptionSelected(groupId = "size_group", optionId = "large")
                vm.onAddToCart()

                customisationsSlot.captured
                    .any { it.id == "large" } shouldBe true
            }
        }

        `when`("add to cart succeeds") {
            then("ShowSnackbar event emitted with item name") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onAddToCart()

                    awaitItem() shouldBe
                            ProductDetailViewModel.ProductDetailEvent
                                .ShowSnackbar("Chicken Biryani added to cart 🛒")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("add to cart succeeds") {
            then("NavigateBack event emitted after snackbar") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onAddToCart()

                    awaitItem() // consume ShowSnackbar

                    awaitItem() shouldBe
                            ProductDetailViewModel.ProductDetailEvent.NavigateBack

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("AddToCartUseCase throws exception") {
            then("ShowError event emitted with error message") {
                coEvery {
                    addToCartUseCase(any(), any(), any())
                } throws Exception("Cart is full")

                val vm = createViewModel()
                vm.events.test {
                    vm.onAddToCart()

                    awaitItem() shouldBe
                            ProductDetailViewModel.ProductDetailEvent
                                .ShowError("Cart is full")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("item not loaded (null)") {
            then("ShowError emitted — cannot add null item") {
                every { restaurantRepository.getMenuItems(any()) } returns
                        flowOf(Result.failure(Exception("error")))

                val vm = createViewModel()
                vm.uiState.value.item shouldBe null

                vm.events.test {
                    vm.onAddToCart()

                    val event = awaitItem()
                    assert(
                        event is ProductDetailViewModel
                        .ProductDetailEvent.ShowError
                    ) { "Expected ShowError but got $event" }

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Navigation
    // ══════════════════════════════════════════════════════════

    given("user is on ProductDetailScreen") {

        `when`("user taps back button") {
            then("NavigateBack event is emitted") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onBackPressed()

                    awaitItem() shouldBe
                            ProductDetailViewModel.ProductDetailEvent.NavigateBack

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Simple Item (no customisations)
    // ══════════════════════════════════════════════════════════

    given("item has NO customisations (Plain Naan)") {

        `when`("screen opens with simple item m2") {
            then("item loads correctly") {
                createViewModel(menuItemId = "m2").uiState.value
                    .item?.name shouldBe "Plain Naan"
            }
        }

        `when`("simple item loaded") {
            then("totalPrice equals base price × qty") {
                createViewModel(menuItemId = "m2").uiState.value
                    .totalPrice shouldBe 50.0
            }
        }

        `when`("user increments qty on simple item") {
            then("totalPrice updates correctly") {
                val vm = createViewModel(menuItemId = "m2")
                vm.onIncrementQuantity()

                vm.uiState.value.totalPrice shouldBe 100.0
            }
        }

        `when`("user adds simple item to cart") {
            then("AddToCartUseCase called with empty customisations") {
                val customisationsSlot = slot<List<CustomisationOption>>()
                coEvery {
                    addToCartUseCase(any(), any(), capture(customisationsSlot))
                } just runs

                val vm = createViewModel(menuItemId = "m2")
                vm.onAddToCart()

                coVerify(exactly = 1) { addToCartUseCase(any(), any(), any()) }
                customisationsSlot.captured.isEmpty() shouldBe true
            }
        }
    }
})

// ── Local test data helpers ───────────────────────────────────────────────
// WHY local not from FakeRepository companion?
// Spec is self-contained — no dependency on fake class
// Easy to read: data is right here in this file

private fun fakeMenuWithCustomisations(): Map<String, List<MenuItem>> = mapOf(
    "Biryani" to listOf(
        MenuItem(
            id = "m1",
            restaurantId = "r1",
            name = "Chicken Biryani",
            description = "Delicious Chicken Biryani",
            price = 249.0,
            imageUrl = "",
            category = "Biryani",
            isVeg = false,
            isRecommended = true,
            isBestseller = true,
            isAvailable = true,
            customisations = listOf(
                Customisation(
                    id = "size_group",
                    name = "Size",
                    options = listOf(
                        CustomisationOption("regular", "Regular", 0.0),
                        CustomisationOption("large", "Large", 50.0),
                        CustomisationOption("extra_large", "Extra Large", 100.0),
                    ),
                ),
                Customisation(
                    id = "spice_group",
                    name = "Spice Level",
                    options = listOf(
                        CustomisationOption("mild", "Mild", 0.0),
                        CustomisationOption("medium", "Medium", 0.0),
                        CustomisationOption("hot", "Hot", 0.0),
                    ),
                ),
            ),
        )
    ),
    "Breads" to listOf(
        MenuItem(
            id = "m2",
            restaurantId = "r1",
            name = "Plain Naan",
            description = "Delicious Plain Naan",
            price = 50.0,
            imageUrl = "",
            category = "Breads",
            isVeg = true,
            isRecommended = false,
            isBestseller = false,
            isAvailable = true,
            customisations = emptyList(),  // no customisations
        )
    ),
)
