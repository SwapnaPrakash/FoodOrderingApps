package com.swapna.foodapp.presentation.product

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.swapna.foodapp.domain.model.Customisation
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.cart.AddToCartUseCase
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.TestConstants.CART_QTY_1
import com.swapna.foodapp.utils.TestConstants.CATEGORY_BIRYANI
import com.swapna.foodapp.utils.TestConstants.CATEGORY_BREADS
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_COUNT_SIZE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_GROUP_SIZE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_GROUP_SPICE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_HOT
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_LARGE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_MEDIUM
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_MILD
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_REGULAR
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_XL
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_NAME_SIZE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_NAME_SPICE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_EXTRA_LARGE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_HOT
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_LARGE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_MEDIUM
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_MILD
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_REGULAR
import com.swapna.foodapp.utils.TestConstants.ERR_CART_FULL
import com.swapna.foodapp.utils.TestConstants.ERR_FAILED_LOAD_ITEM_MSG
import com.swapna.foodapp.utils.TestConstants.ERR_ITEM_NOT_FOUND_MSG
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_100
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_LARGE
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_ZERO
import com.swapna.foodapp.utils.TestConstants.MENU_DESC_BIRYANI
import com.swapna.foodapp.utils.TestConstants.MENU_DESC_NAAN
import com.swapna.foodapp.utils.TestConstants.MENU_ID_1
import com.swapna.foodapp.utils.TestConstants.MENU_ID_2
import com.swapna.foodapp.utils.TestConstants.MENU_ID_UNKNOWN
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_PLAIN_NAAN
import com.swapna.foodapp.utils.TestConstants.MSG_BIRYANI_ADDED_CART
import com.swapna.foodapp.utils.TestConstants.PRICE_249
import com.swapna.foodapp.utils.TestConstants.PRICE_50
import com.swapna.foodapp.utils.TestConstants.QTY_15_REPEAT
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_1
import com.swapna.foodapp.utils.TestConstants.TOTAL_PRICE_100
import com.swapna.foodapp.utils.TestConstants.TOTAL_PRICE_1047
import com.swapna.foodapp.utils.TestConstants.TOTAL_PRICE_299
import com.swapna.foodapp.utils.TestConstants.TOTAL_PRICE_349_P
import com.swapna.foodapp.utils.TestConstants.TOTAL_PRICE_498
import com.swapna.foodapp.utils.TestConstants.TOTAL_PRICE_598
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

    val restaurantRepository = mockk<RestaurantRepository>()
    val addToCartUseCase = mockk<AddToCartUseCase>()

    fun createViewModel(
        restaurantId: String = RESTAURANT_ID_1,
        menuItemId: String = MENU_ID_1,
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
            ioDispatcher = dispatcher,
        )
    }

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(dispatcher)

        every { restaurantRepository.getMenuItems(any()) } returns
                flowOf(Result.success(fakeMenuWithCustomisations()))

        coEvery { addToCartUseCase(any(), any(), any()) } just runs
    }

    afterEach { Dispatchers.resetMain() }

    // GROUP 1 — Load Menu Item
    given("ProductDetailScreen is opened with item m1") {

        `when`("menu loads successfully") {
            lateinit var vm: ProductDetailViewModel

            beforeEach { vm = createViewModel() }

            then("uiState.item should be Chicken Biryani") {
                vm.uiState.value.item?.name shouldBe MENU_ITEM_CHICK_BIR
            }
            then("isLoading should be false") {
                vm.uiState.value.isLoading shouldBe false
            }
            then("error should be null") {
                vm.uiState.value.error shouldBe null
            }
            then("initial quantity should be 1") {
                vm.uiState.value.quantity shouldBe CART_QTY_1
            }
            then("item should have 2 customisation groups") {
                val item = vm.uiState.value.item
                item?.customisations?.size shouldBe CUSTOMISE_COUNT_SIZE
                item?.customisations?.any { it.name == CUSTOMISE_NAME_SIZE } shouldBe true
                item?.customisations?.any { it.name == CUSTOMISE_NAME_SPICE } shouldBe true
            }
        }

        `when`("menu API throws error") {
            then("error state should be shown") {
                every { restaurantRepository.getMenuItems(any()) } returns
                        flowOf(Result.failure(Exception()))

                val vm = createViewModel()

                vm.uiState.value.isLoading shouldBe false
                vm.uiState.value.item shouldBe null
                vm.uiState.value.error shouldBe ERR_FAILED_LOAD_ITEM_MSG
            }
        }

        `when`("itemId does not exist in menu") {
            then("error state shows Item not found") {
                val vm = createViewModel(menuItemId = MENU_ID_UNKNOWN)

                vm.uiState.value.item shouldBe null
                vm.uiState.value.error shouldBe ERR_ITEM_NOT_FOUND_MSG
            }
        }
    }

    // GROUP 2 — Default Customisation Selections
    given("item with customisations loads") {

        `when`("screen opens with default selections") {
            lateinit var vm: ProductDetailViewModel

            beforeEach { vm = createViewModel() }

            then("first option of Size group is pre-selected") {
                vm.uiState.value
                    .selectedOptions[CUSTOMISE_GROUP_SIZE] shouldBe CUSTOMISE_OPT_REGULAR
            }
            then("first option of Spice group is pre-selected") {
                vm.uiState.value
                    .selectedOptions[CUSTOMISE_GROUP_SPICE] shouldBe CUSTOMISE_OPT_MILD
            }
        }

        `when`("item has NO customisations") {
            then("selectedOptions should be empty") {
                val vm = createViewModel(menuItemId = MENU_ID_2)
                vm.uiState.value.selectedOptions shouldBe emptyMap()
            }
        }
    }

    // GROUP 3 — Initial Price Calculation
    given("item loads with price ₹249") {

        `when`("default selections are applied") {
            then("totalPrice should equal base price (no extras)") {
                createViewModel().uiState.value.totalPrice shouldBe PRICE_249
            }
        }
    }

    // GROUP 4 — Customisation Selection
    given("user is customising their order") {

        `when`("user selects Large size (+₹50)") {
            lateinit var vm: ProductDetailViewModel

            beforeEach {
                vm = createViewModel()
                vm.onOptionSelected(
                    groupId = CUSTOMISE_GROUP_SIZE,
                    optionId = CUSTOMISE_OPT_LARGE,
                )
            }

            then("selectedOptions updates to large for size_group") {
                vm.uiState.value.selectedOptions[CUSTOMISE_GROUP_SIZE] shouldBe CUSTOMISE_OPT_LARGE
            }
            then("totalPrice should be 299.0") {
                vm.uiState.value.totalPrice shouldBe TOTAL_PRICE_299
            }
        }

        `when`("user selects Extra Large size (+₹100)") {
            then("totalPrice should be 349.0") {
                val vm = createViewModel()
                vm.onOptionSelected(
                    groupId = CUSTOMISE_GROUP_SIZE,
                    optionId = CUSTOMISE_OPT_EXTRA_LARGE
                )

                vm.uiState.value.totalPrice shouldBe TOTAL_PRICE_349_P
            }
        }

        `when`("user switches Large back to Regular") {
            then("totalPrice should return to base 249.0") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = CUSTOMISE_GROUP_SIZE, optionId = CUSTOMISE_OPT_LARGE)
                vm.onOptionSelected(
                    groupId = CUSTOMISE_GROUP_SIZE,
                    optionId = CUSTOMISE_OPT_REGULAR
                )

                vm.uiState.value.totalPrice shouldBe PRICE_249
            }
        }

        `when`("user selects option in Spice group") {
            then("size selection is NOT changed") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = CUSTOMISE_GROUP_SIZE, optionId = CUSTOMISE_OPT_LARGE)
                vm.onOptionSelected(
                    groupId = CUSTOMISE_GROUP_SPICE,
                    optionId = CUSTOMISE_OPT_HOT
                )

                vm.uiState.value.selectedOptions[CUSTOMISE_GROUP_SIZE] shouldBe CUSTOMISE_OPT_LARGE
                vm.uiState.value.selectedOptions[CUSTOMISE_GROUP_SPICE] shouldBe CUSTOMISE_OPT_HOT
            }
        }

        `when`("user selects both Large size + different spice") {
            then("totalPrice includes only size extra (spice is free)") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = CUSTOMISE_GROUP_SIZE, optionId = CUSTOMISE_OPT_LARGE)
                vm.onOptionSelected(groupId = CUSTOMISE_GROUP_SPICE, optionId = CUSTOMISE_OPT_HOT)

                vm.uiState.value.totalPrice shouldBe TOTAL_PRICE_299
            }
        }
    }

    // GROUP 5 — Quantity Management
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
                vm.uiState.value.quantity shouldBe CART_QTY_1
            }
        }

        `when`("user taps - when qty is already 1") {
            then("quantity should stay at 1") {
                val vm = createViewModel()
                vm.onDecrementQuantity()
                vm.uiState.value.quantity shouldBe CART_QTY_1
            }
        }

        `when`("user increments beyond MAX_ITEM_QUANTITY") {
            then("quantity should not exceed MAX") {
                val vm = createViewModel()
                repeat(QTY_15_REPEAT) { vm.onIncrementQuantity() }

                vm.uiState.value.quantity shouldBe AppBusinessRules.MAX_ITEM_QUANTITY
            }
        }
    }

    // GROUP 6 — Price Updates with Quantity
    given("user changes quantity") {

        `when`("qty incremented to 2 with no extra customisations") {
            then("totalPrice should be 249.0 × 2 = 498.0") {
                val vm = createViewModel()
                vm.onIncrementQuantity()
                vm.uiState.value.totalPrice shouldBe TOTAL_PRICE_498
            }
        }

        `when`("user selects Large (+₹50) and qty 2") {
            then("totalPrice should be (249+50)×2 = 598.0") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = CUSTOMISE_GROUP_SIZE, optionId = CUSTOMISE_OPT_LARGE)
                vm.onIncrementQuantity()
                vm.uiState.value.totalPrice shouldBe TOTAL_PRICE_598
            }
        }

        `when`("user selects Large (+₹50) then decrements to 1") {
            then("totalPrice should be (249+50)×1 = 299.0") {
                val vm = createViewModel()
                vm.onOptionSelected(groupId = CUSTOMISE_GROUP_SIZE, optionId = CUSTOMISE_OPT_LARGE)
                vm.onIncrementQuantity()
                vm.onDecrementQuantity()
                vm.uiState.value.totalPrice shouldBe TOTAL_PRICE_299
            }
        }

        `when`("qty is 3 with Extra Large (+₹100)") {
            then("totalPrice should be (249+100)×3 = 1047.0") {
                val vm = createViewModel()
                vm.onOptionSelected(
                    groupId = CUSTOMISE_GROUP_SIZE,
                    optionId = CUSTOMISE_OPT_EXTRA_LARGE
                )
                vm.onIncrementQuantity()
                vm.onIncrementQuantity()
                vm.uiState.value.totalPrice shouldBe TOTAL_PRICE_1047
            }
        }
    }

    // GROUP 7 — Add to Cart
    given("user taps Add to Cart button") {

        `when`("item loaded and default selections") {
            then("AddToCartUseCase is called once") {
                val vm = createViewModel()
                vm.onAddToCart()
                coVerify(exactly = 1) { addToCartUseCase(any(), any(), any()) }
            }
        }

        `when`("item loaded with default selections") {
            then("AddToCartUseCase called with correct MenuItem") {
                val itemSlot = slot<MenuItem>()
                coEvery { addToCartUseCase(capture(itemSlot), any(), any()) } just runs

                val vm = createViewModel()
                vm.onAddToCart()

                itemSlot.captured.id shouldBe MENU_ID_1
                itemSlot.captured.name shouldBe MENU_ITEM_CHICK_BIR
            }
        }

        `when`("user selected qty 2") {
            then("AddToCartUseCase called with qty 2") {
                val qtySlot = slot<Int>()
                coEvery { addToCartUseCase(any(), capture(qtySlot), any()) } just runs

                val vm = createViewModel()
                vm.onIncrementQuantity()
                vm.onAddToCart()

                qtySlot.captured shouldBe 2
            }
        }

        `when`("user selected Large size option") {
            then("AddToCartUseCase called with Large customisation") {
                val customisationsSlot = slot<List<CustomisationOption>>()
                coEvery { addToCartUseCase(any(), any(), capture(customisationsSlot)) } just runs

                val vm = createViewModel()
                vm.onOptionSelected(groupId = CUSTOMISE_GROUP_SIZE, optionId = CUSTOMISE_OPT_LARGE)
                vm.onAddToCart()

                customisationsSlot.captured
                    .any { it.id == CUSTOMISE_OPT_LARGE } shouldBe true
            }
        }

        `when`("add to cart succeeds") {
            then("ShowSnackbar event emitted with item name") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onAddToCart()
                    awaitItem() shouldBe ProductDetailViewModel.ProductDetailEvent
                        .ShowSnackbar(MSG_BIRYANI_ADDED_CART)
                    cancelAndIgnoreRemainingEvents()
                }
            }
            then("NavigateBack event emitted after snackbar") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onAddToCart()
                    awaitItem()   // consume ShowSnackbar
                    awaitItem() shouldBe ProductDetailViewModel.ProductDetailEvent.NavigateBack
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("AddToCartUseCase throws exception") {
            then("ShowError event emitted with error message") {
                coEvery { addToCartUseCase(any(), any(), any()) } throws
                        Exception(ERR_CART_FULL)

                val vm = createViewModel()
                vm.events.test {
                    vm.onAddToCart()

                    awaitItem() shouldBe ProductDetailViewModel.ProductDetailEvent
                        .ShowError(ERR_CART_FULL)

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
                        event is ProductDetailViewModel.ProductDetailEvent.ShowError
                    ) { "Expected ShowError but got $event" }

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // GROUP 8 — Navigation
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

    // GROUP 9 — Simple Item (no customisations)
    given("item has NO customisations (Plain Naan)") {

        `when`("screen opens with simple item m2") {
            then("item loads correctly") {
                createViewModel(menuItemId = MENU_ID_2).uiState.value
                    .item?.name shouldBe MENU_ITEM_PLAIN_NAAN
            }
        }

        `when`("simple item loaded") {
            then("totalPrice equals base price × qty") {
                createViewModel(menuItemId = MENU_ID_2).uiState.value
                    .totalPrice shouldBe PRICE_50
            }
        }

        `when`("user increments qty on simple item") {
            then("totalPrice updates correctly") {
                val vm = createViewModel(menuItemId = MENU_ID_2)
                vm.onIncrementQuantity()
                vm.uiState.value.totalPrice shouldBe TOTAL_PRICE_100
            }
        }

        `when`("user adds simple item to cart") {
            then("AddToCartUseCase called with empty customisations") {
                val customisationsSlot = slot<List<CustomisationOption>>()
                coEvery { addToCartUseCase(any(), any(), capture(customisationsSlot)) } just runs

                val vm = createViewModel(menuItemId = MENU_ID_2)
                vm.onAddToCart()

                coVerify(exactly = 1) { addToCartUseCase(any(), any(), any()) }
                customisationsSlot.captured.isEmpty() shouldBe true
            }
        }
    }
})

// ── Local test data helpers ───────────────────────────────────
private fun fakeMenuWithCustomisations(): Map<String, List<MenuItem>> = mapOf(
    CATEGORY_BIRYANI to listOf(
        MenuItem(
            id = MENU_ID_1,
            restaurantId = RESTAURANT_ID_1,
            name = MENU_ITEM_CHICK_BIR,
            description = MENU_DESC_BIRYANI,
            price = PRICE_249,
            imageUrl = "",
            category = CATEGORY_BIRYANI,
            isVeg = false,
            isRecommended = true,
            isBestseller = true,
            isAvailable = true,
            customisations = listOf(
                Customisation(
                    id = CUSTOMISE_GROUP_SIZE,
                    name = CUSTOMISE_NAME_SIZE,
                    options = listOf(
                        CustomisationOption(
                            CUSTOMISE_OPT_REGULAR,
                            CUSTOMISE_LABEL_REGULAR,
                            EXTRA_PRICE_ZERO
                        ),
                        CustomisationOption(
                            CUSTOMISE_OPT_LARGE,
                            CUSTOMISE_LABEL_LARGE,
                            EXTRA_PRICE_LARGE
                        ),
                        CustomisationOption(
                            CUSTOMISE_OPT_EXTRA_LARGE,
                            CUSTOMISE_LABEL_XL,
                            EXTRA_PRICE_100
                        ),
                    ),
                ),
                Customisation(
                    id = CUSTOMISE_GROUP_SPICE,
                    name = CUSTOMISE_NAME_SPICE,
                    options = listOf(
                        CustomisationOption(
                            CUSTOMISE_OPT_MILD,
                            CUSTOMISE_LABEL_MILD,
                            EXTRA_PRICE_ZERO
                        ),
                        CustomisationOption(
                            CUSTOMISE_OPT_MEDIUM,
                            CUSTOMISE_LABEL_MEDIUM,
                            EXTRA_PRICE_ZERO
                        ),
                        CustomisationOption(
                            CUSTOMISE_OPT_HOT,
                            CUSTOMISE_LABEL_HOT,
                            EXTRA_PRICE_ZERO
                        ),
                    ),
                ),
            ),
        )
    ),
    CATEGORY_BREADS to listOf(
        MenuItem(
            id = MENU_ID_2,
            restaurantId = RESTAURANT_ID_1,
            name = MENU_ITEM_PLAIN_NAAN,
            description = MENU_DESC_NAAN,
            price = PRICE_50,
            imageUrl = "",
            category = CATEGORY_BREADS,
            isVeg = true,
            isRecommended = false,
            isBestseller = false,
            isAvailable = true,
            customisations = emptyList(),
        )
    ),
)