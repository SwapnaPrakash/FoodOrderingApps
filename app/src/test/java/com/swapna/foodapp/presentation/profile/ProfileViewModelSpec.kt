package com.swapna.foodapp.presentation.profile

import app.cash.turbine.test
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.TestConstants.ADDRESS_FULL_1
import com.swapna.foodapp.utils.TestConstants.ADDRESS_ID_1
import com.swapna.foodapp.utils.TestConstants.ADDRESS_ID_2
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LABEL_WORK
import com.swapna.foodapp.utils.TestConstants.ERR_COULD_NOT_LOAD_PROFILE
import com.swapna.foodapp.utils.TestConstants.ERR_DELETE_FAILED
import com.swapna.foodapp.utils.TestConstants.ERR_LOGOUT_FAILED_MSG
import com.swapna.foodapp.utils.TestConstants.ERR_NAME_EMPTY_MSG
import com.swapna.foodapp.utils.TestConstants.ERR_NETWORK
import com.swapna.foodapp.utils.TestConstants.ERR_NO_USER_FOUND_MSG
import com.swapna.foodapp.utils.TestConstants.MSG_ADDRESS_REMOVED
import com.swapna.foodapp.utils.TestConstants.MSG_PROFILE_UPDATED_TICK
import com.swapna.foodapp.utils.TestConstants.ORDER_COUNT_2
import com.swapna.foodapp.utils.TestConstants.ORDER_ID_1
import com.swapna.foodapp.utils.TestConstants.ORDER_ID_2
import com.swapna.foodapp.utils.TestConstants.ORDER_STATUS_DELIVERED_CAP
import com.swapna.foodapp.utils.TestConstants.ORDER_TIME_FRIENDLY
import com.swapna.foodapp.utils.TestConstants.PRICE_249
import com.swapna.foodapp.utils.TestConstants.PROFILE_FALLBACK_EMAIL
import com.swapna.foodapp.utils.TestConstants.PROFILE_FALLBACK_NAME
import com.swapna.foodapp.utils.TestConstants.PROFILE_SELECTED_LOC
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_1
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_MEGHANA
import com.swapna.foodapp.utils.TestConstants.USER_EMAIL_SWAPNA
import com.swapna.foodapp.utils.TestConstants.USER_EMAIL_UPDATED
import com.swapna.foodapp.utils.TestConstants.USER_ID_1
import com.swapna.foodapp.utils.TestConstants.USER_NAME_SWAPNA
import com.swapna.foodapp.utils.TestConstants.USER_NAME_UPDATED
import com.swapna.foodapp.utils.TestConstants.USER_PHONE_VALID
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelSpec : BehaviorSpec({

    val dispatcher = UnconfinedTestDispatcher()
    val userRepository = mockk<UserRepository>()
    val userFlow = MutableStateFlow<User?>(null)

    fun createViewModel() = ProfileViewModel(
        userRepository = userRepository,
        ioDispatcher = dispatcher,
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(dispatcher)
        userFlow.value = testUser()

        every { userRepository.getCurrentUser() } returns userFlow
        every { userRepository.getRecentOrders() } returns flowOf(emptyList())
        coEvery { userRepository.updateUser(any()) } returns Result.success(Unit)
        coEvery { userRepository.logout() } just runs
        coEvery { userRepository.deleteAddress(any()) } just runs
    }

    afterEach { Dispatchers.resetMain() }

    // GROUP 1 — Initial Profile Load
    given("ProfileScreen opens for the first time") {

        `when`("user profile loads successfully") {
            lateinit var vm: ProfileViewModel
            beforeEach { vm = createViewModel() }

            then("isLoading is false") { vm.uiState.value.isLoading shouldBe false }

            then("user name is Swapna") {
                vm.uiState.value.user?.name shouldBe USER_NAME_SWAPNA
            }

            then("error is null") { vm.uiState.value.error shouldBe null }

            then("isLoggedIn is true") { vm.uiState.value.isLoggedIn shouldBe true }

            then("displayName shows user name") {
                vm.uiState.value.displayName shouldBe USER_NAME_SWAPNA
            }

            then("displayEmail shows user email") {
                vm.uiState.value.displayEmail shouldBe USER_EMAIL_SWAPNA
            }

            then("editName matches user name") {
                vm.uiState.value.editName shouldBe USER_NAME_SWAPNA
            }

            then("editEmail matches user email") {
                vm.uiState.value.editEmail shouldBe USER_EMAIL_SWAPNA
            }
        }

        `when`("user has no name set") {
            then("displayName falls back to Add your name") {
                userFlow.value = testUser(name = "")
                createViewModel().uiState.value.displayName shouldBe PROFILE_FALLBACK_NAME
            }
        }

        `when`("user has no email set") {
            then("displayEmail falls back to Add email address") {
                userFlow.value = testUser(email = "")
                createViewModel().uiState.value.displayEmail shouldBe PROFILE_FALLBACK_EMAIL
            }
        }

        `when`("getCurrentUser emits null") {
            then("error is set and isLoggedIn is false") {
                userFlow.value = null

                val vm = createViewModel()

                vm.uiState.value.user shouldBe null
                vm.uiState.value.error shouldBe ERR_COULD_NOT_LOAD_PROFILE
                vm.uiState.value.isLoggedIn shouldBe false
            }
        }

    }

    // GROUP 2 — Orders Load
    given("ProfileScreen loads recent orders") {

        `when`("API returns 2 orders") {
            then("orders list has 2 items") {
                every { userRepository.getRecentOrders() } returns
                        flowOf(listOf(testOrder(ORDER_ID_1), testOrder(ORDER_ID_2)))

                createViewModel().uiState.value.orders.size shouldBe ORDER_COUNT_2
            }
        }

        `when`("API returns empty orders") {
            then("orders list is empty") {
                createViewModel().uiState.value.orders shouldBe emptyList()
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Addresses
    // ══════════════════════════════════════════════════════════

    given("user has saved addresses") {

        `when`("user has Home and Work addresses") {
            then("addresses computed property returns both") {
                userFlow.value = testUser(
                    addresses = listOf(
                        testAddress(ADDRESS_ID_1, ADDRESS_LABEL_HOME),
                        testAddress(ADDRESS_ID_2, ADDRESS_LABEL_WORK),
                    )
                )
                val vm = createViewModel()

                vm.uiState.value.addresses.size shouldBe 2
                vm.uiState.value.hasAddresses shouldBe true
            }
        }

        `when`("user has no addresses") {
            then("addresses is empty and hasAddresses is false") {
                userFlow.value = testUser(addresses = emptyList())
                val vm = createViewModel()

                vm.uiState.value.addresses shouldBe emptyList()
                vm.uiState.value.hasAddresses shouldBe false
            }
        }
    }

    // GROUP 4 — Edit Mode
    given("user taps Edit button") {

        `when`("onEditClicked called") {

            lateinit var vm: ProfileViewModel

            beforeEach {
                vm = createViewModel()
                vm.onEditClicked()
            }

            then("isEditMode becomes true") {
                vm.uiState.value.isEditMode shouldBe true
            }

            then("editName pre-filled with current user name") {
                vm.uiState.value.editName shouldBe USER_NAME_SWAPNA
            }

            then("editEmail pre-filled with current user email") {
                vm.uiState.value.editEmail shouldBe USER_EMAIL_SWAPNA
            }
        }

        `when`("user types new name") {
            then("editName updates to new value") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onNameChanged(USER_NAME_UPDATED)
                vm.uiState.value.editName shouldBe USER_NAME_UPDATED
            }
        }

        `when`("user types new email") {
            then("editEmail updates to new value") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onEmailChanged(USER_EMAIL_UPDATED)
                vm.uiState.value.editEmail shouldBe USER_EMAIL_UPDATED
            }
        }

        `when`("user cancels edit") {
            then("isEditMode becomes false") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onNameChanged("Some Edit")
                vm.onCancelEdit()
                vm.uiState.value.isEditMode shouldBe false
            }
        }

        `when`("user cancels after typing new name") {
            then("editName resets to original user name") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onNameChanged("Changed Name")
                vm.onCancelEdit()
                vm.uiState.value.editName shouldBe USER_NAME_SWAPNA
            }
        }
    }

    // GROUP 5 — Save Profile
    given("user taps Save after editing profile") {

        `when`("name and email are valid") {
            then("updateUser called with updated User object") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onNameChanged(USER_NAME_UPDATED)
                vm.onEmailChanged(USER_EMAIL_UPDATED)
                vm.onSaveProfile()

                coVerify {
                    userRepository.updateUser(
                        match {
                            it.name == USER_NAME_UPDATED &&
                                    it.email == USER_EMAIL_UPDATED
                        }
                    )
                }
            }
        }

        `when`("save succeeds") {

            then("isEditMode becomes false") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onSaveProfile()
                vm.uiState.value.isEditMode shouldBe false
            }

            then("ShowSnackbar emitted with Profile updated") {

                val vm = createViewModel()
                vm.events.test {
                    vm.onEditClicked()
                    vm.onSaveProfile()
                    awaitItem() shouldBe ProfileViewModel.ProfileEvent.ShowSnackbar(
                        MSG_PROFILE_UPDATED_TICK
                    )

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("name is blank") {
            then("ShowError emitted — Name cannot be empty") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onNameChanged("")
                vm.events.test {
                    vm.onSaveProfile()
                    awaitItem() shouldBe ProfileViewModel.ProfileEvent.ShowError(ERR_NAME_EMPTY_MSG)
                    cancelAndIgnoreRemainingEvents()
                }
            }
            then("updateUser is NOT called") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onNameChanged("")
                vm.onSaveProfile()
                coVerify(exactly = 0) { userRepository.updateUser(any()) }
            }
        }

        `when`("user is null during save") {
            then("ShowError emitted — No user found") {
                userFlow.value = null

                val vm = createViewModel()
                vm.events.test {
                    vm.onSaveProfile()

                    awaitItem() shouldBe ProfileViewModel.ProfileEvent
                        .ShowError(ERR_NO_USER_FOUND_MSG)

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("updateUser throws exception") {
            then("ShowError emitted with exception message") {
                coEvery { userRepository.updateUser(any()) } returns
                        Result.failure(Exception(ERR_NETWORK))

                val vm = createViewModel()
                vm.onEditClicked()
                vm.events.test {
                    vm.onSaveProfile()

                    awaitItem() shouldBe ProfileViewModel.ProfileEvent
                        .ShowError(ERR_NETWORK)

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // GROUP 6 — Delete Address
    given("user taps delete on a saved address") {

        `when`("deleteAddress succeeds") {
            then("deleteAddress called with correct addressId") {
                val vm = createViewModel()
                vm.onDeleteAddress(ADDRESS_ID_1)
                coVerify { userRepository.deleteAddress(ADDRESS_ID_1) }
            }
            then("ShowSnackbar emitted with Address removed") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onDeleteAddress(ADDRESS_ID_1)
                    awaitItem() shouldBe ProfileViewModel.ProfileEvent.ShowSnackbar(
                        MSG_ADDRESS_REMOVED
                    )
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("deleteAddress throws exception") {
            then("ShowError emitted with error message") {
                coEvery { userRepository.deleteAddress(any()) } throws
                        Exception(ERR_DELETE_FAILED)

                val vm = createViewModel()
                vm.events.test {
                    vm.onDeleteAddress(ADDRESS_ID_1)

                    awaitItem() shouldBe ProfileViewModel.ProfileEvent
                        .ShowError(ERR_DELETE_FAILED)

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // GROUP 7 — Logout
    given("user taps Logout") {

        `when`("logout succeeds") {
            then("logout called on UserRepository") {
                createViewModel().also { it.onLogout() }
                coVerify { userRepository.logout() }
            }
            then("NavigateToLogin event emitted") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onLogout()
                    awaitItem() shouldBe ProfileViewModel.ProfileEvent.NavigateToLogin
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("logout throws exception") {
            then("ShowError emitted with error message") {
                coEvery { userRepository.logout() } throws
                        Exception(ERR_LOGOUT_FAILED_MSG)

                val vm = createViewModel()
                vm.events.test {
                    vm.onLogout()

                    awaitItem() shouldBe ProfileViewModel.ProfileEvent
                        .ShowError(ERR_LOGOUT_FAILED_MSG)

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Navigation
    // ══════════════════════════════════════════════════════════

    given("user is on ProfileScreen") {

        `when`("user taps back button") {
            then("NavigateBack event emitted") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onBackPressed()
                    awaitItem() shouldBe ProfileViewModel.ProfileEvent.NavigateBack
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Reactive Profile Updates
    // ══════════════════════════════════════════════════════════

    given("user profile changes after screen opens") {

        `when`("getCurrentUser emits updated user name") {
            then("uiState reflects new name immediately") {
                val vm = createViewModel()
                vm.uiState.value.user?.name shouldBe USER_NAME_SWAPNA

                userFlow.value = testUser(name = USER_NAME_UPDATED)

                vm.uiState.value.user?.name shouldBe USER_NAME_UPDATED
            }
        }

        `when`("getCurrentUser emits null after logout") {
            then("isLoggedIn becomes false and error set") {
                val vm = createViewModel()
                vm.uiState.value.isLoggedIn shouldBe true

                userFlow.value = null

                vm.uiState.value.isLoggedIn shouldBe false
                vm.uiState.value.error shouldNotBe null
            }
        }
    }
})

private fun testUser(
    id: String = USER_ID_1,
    name: String = USER_NAME_SWAPNA,
    email: String = USER_EMAIL_SWAPNA,
    phone: String = USER_PHONE_VALID,
    addresses: List<Address> = emptyList(),
) = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    profileImage = "",
    addresses = addresses,
    selectedLocation = PROFILE_SELECTED_LOC,
)

private fun testAddress(
    id: String = ADDRESS_ID_1,
    label: String = ADDRESS_LABEL_HOME,
) = Address(
    id = id,
    label = label,
    fullAddress = ADDRESS_FULL_1,
    landmark = "",
    latitude = 0.0,
    longitude = 0.0,
)

private fun testOrder(
    id: String = ORDER_ID_1,
) = Order(
    id = id,
    restaurantId = RESTAURANT_ID_1,
    restaurantName = RESTAURANT_MEGHANA,
    restaurantImage = "",
    status = ORDER_STATUS_DELIVERED_CAP,
    timeFriendly = ORDER_TIME_FRIENDLY,
    totalAmount = PRICE_249,
    items = emptyList(),
    canReorder = true,
)