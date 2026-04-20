package com.swapna.foodapp.presentation.profile

import app.cash.turbine.test
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
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

    // ── MockK dependency ──────────────────────────────────────
    val userRepository = mockk<UserRepository>()

    // ── Controllable user flow ────────────────────────────────
    // WHY MutableStateFlow not flowOf()?
    //   Some tests update the user mid-test (e.g. after saveProfile)
    //   MutableStateFlow lets us emit new values from answers block
    val userFlow = MutableStateFlow<User?>(null)

    fun createViewModel() = ProfileViewModel(
        userRepository = userRepository,
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(dispatcher)
        userFlow.value = testUser()   // default: valid logged-in user

        // Default stubs
        every { userRepository.getCurrentUser() } returns userFlow
        every { userRepository.getRecentOrders() } returns
                flowOf(emptyList())
        coEvery { userRepository.updateUser(any()) } returns
                Result.success(Unit)
        coEvery { userRepository.logout() } just runs
        coEvery { userRepository.deleteAddress(any()) } just runs
    }

    afterEach { Dispatchers.resetMain() }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Initial Profile Load
    // ══════════════════════════════════════════════════════════

    given("ProfileScreen opens for the first time") {

        `when`("user is logged in and profile loads") {
            then("isLoading is false") {
                createViewModel().uiState.value.isLoading shouldBe false
            }
        }

        `when`("user profile loads") {
            then("user name is Swapna") {
                createViewModel().uiState.value.user?.name shouldBe "Swapna"
            }
        }

        `when`("user profile loads") {
            then("error is null") {
                createViewModel().uiState.value.error shouldBe null
            }
        }

        `when`("user profile loads") {
            then("isLoggedIn computed property is true") {
                createViewModel().uiState.value.isLoggedIn shouldBe true
            }
        }

        `when`("user has name and email set") {
            then("displayName shows user name") {
                createViewModel().uiState.value.displayName shouldBe "Swapna"
            }
        }

        `when`("user has name and email set") {
            then("displayEmail shows user email") {
                createViewModel().uiState.value.displayEmail shouldBe
                        "swapna@example.com"
            }
        }

        `when`("user has no name set") {
            then("displayName falls back to Add your name") {
                userFlow.value = testUser(name = "")
                createViewModel().uiState.value.displayName shouldBe "Add your name"
            }
        }

        `when`("user has no email set") {
            then("displayEmail falls back to Add email address") {
                userFlow.value = testUser(email = "")
                createViewModel().uiState.value.displayEmail shouldBe "Add email address"
            }
        }

        `when`("getCurrentUser emits null") {
            then("error is set and isLoggedIn is false") {
                userFlow.value = null

                val vm = createViewModel()

                vm.uiState.value.user      shouldBe null
                vm.uiState.value.error     shouldBe "Could not load profile"
                vm.uiState.value.isLoggedIn shouldBe false
            }
        }

        `when`("editName and editEmail pre-filled on load") {
            then("editName matches user name") {
                createViewModel().uiState.value.editName shouldBe "Swapna"
            }
        }

        `when`("editName and editEmail pre-filled on load") {
            then("editEmail matches user email") {
                createViewModel().uiState.value.editEmail shouldBe
                        "swapna@example.com"
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Orders Load
    // ══════════════════════════════════════════════════════════

    given("ProfileScreen loads recent orders") {

        `when`("API returns 2 orders") {
            then("orders list has 2 items") {
                every { userRepository.getRecentOrders() } returns
                        flowOf(listOf(testOrder("o1"), testOrder("o2")))

                createViewModel().uiState.value.orders.size shouldBe 2
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
                        testAddress("a1", "Home"),
                        testAddress("a2", "Work"),
                    )
                )
                val vm = createViewModel()

                vm.uiState.value.addresses.size shouldBe 2
                vm.uiState.value.hasAddresses   shouldBe true
            }
        }

        `when`("user has no addresses") {
            then("addresses is empty and hasAddresses is false") {
                userFlow.value = testUser(addresses = emptyList())
                val vm = createViewModel()

                vm.uiState.value.addresses    shouldBe emptyList()
                vm.uiState.value.hasAddresses shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Edit Mode
    // ══════════════════════════════════════════════════════════

    given("user taps Edit button") {

        `when`("onEditClicked called") {
            then("isEditMode becomes true") {
                val vm = createViewModel()
                vm.onEditClicked()

                vm.uiState.value.isEditMode shouldBe true
            }
        }

        `when`("onEditClicked called") {
            then("editName pre-filled with current user name") {
                val vm = createViewModel()
                vm.onEditClicked()

                vm.uiState.value.editName shouldBe "Swapna"
            }
        }

        `when`("onEditClicked called") {
            then("editEmail pre-filled with current user email") {
                val vm = createViewModel()
                vm.onEditClicked()

                vm.uiState.value.editEmail shouldBe "swapna@example.com"
            }
        }

        `when`("user types new name") {
            then("editName updates to new value") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onNameChanged("Swapna Reddy")

                vm.uiState.value.editName shouldBe "Swapna Reddy"
            }
        }

        `when`("user types new email") {
            then("editEmail updates to new value") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onEmailChanged("new@example.com")

                vm.uiState.value.editEmail shouldBe "new@example.com"
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

                // Must revert to original — not keep the edit
                vm.uiState.value.editName shouldBe "Swapna"
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Save Profile
    // ══════════════════════════════════════════════════════════

    given("user taps Save after editing profile") {

        `when`("name and email are valid") {
            then("updateUser called with updated User object") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onNameChanged("Swapna Reddy")
                vm.onEmailChanged("updated@example.com")
                vm.onSaveProfile()

                coVerify {
                    userRepository.updateUser(
                        match {
                            it.name  == "Swapna Reddy" &&
                                    it.email == "updated@example.com"
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
        }

        `when`("save succeeds") {
            then("ShowSnackbar event emitted with Profile updated") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onEditClicked()
                    vm.onSaveProfile()

                    awaitItem() shouldBe
                            ProfileViewModel.ProfileEvent
                                .ShowSnackbar("Profile updated ✅")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("name is blank") {
            then("ShowError emitted — Name cannot be empty") {
                val vm = createViewModel()
                vm.onEditClicked()
                vm.onNameChanged("")  // blank name
                vm.events.test {
                    vm.onSaveProfile()

                    awaitItem() shouldBe
                            ProfileViewModel.ProfileEvent
                                .ShowError("Name cannot be empty")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("name is blank") {
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

                    awaitItem() shouldBe
                            ProfileViewModel.ProfileEvent
                                .ShowError("No user found. Please login again.")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("updateUser throws exception") {
            then("ShowError emitted with exception message") {
                coEvery { userRepository.updateUser(any()) } returns
                        Result.failure(Exception("Network error"))

                val vm = createViewModel()
                vm.onEditClicked()
                vm.events.test {
                    vm.onSaveProfile()

                    awaitItem() shouldBe
                            ProfileViewModel.ProfileEvent
                                .ShowError("Network error")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Delete Address
    // ══════════════════════════════════════════════════════════

    given("user taps delete on a saved address") {

        `when`("deleteAddress succeeds") {
            then("deleteAddress called with correct addressId") {
                val vm = createViewModel()
                vm.onDeleteAddress("a1")

                coVerify { userRepository.deleteAddress("a1") }
            }
        }

        `when`("deleteAddress succeeds") {
            then("ShowSnackbar emitted with Address removed") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onDeleteAddress("a1")

                    awaitItem() shouldBe
                            ProfileViewModel.ProfileEvent
                                .ShowSnackbar("Address removed")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("deleteAddress throws exception") {
            then("ShowError emitted with error message") {
                coEvery { userRepository.deleteAddress(any()) } throws
                        Exception("Delete failed")

                val vm = createViewModel()
                vm.events.test {
                    vm.onDeleteAddress("a1")

                    awaitItem() shouldBe
                            ProfileViewModel.ProfileEvent
                                .ShowError("Delete failed")

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Logout
    // ══════════════════════════════════════════════════════════

    given("user taps Logout") {

        `when`("logout succeeds") {
            then("logout called on UserRepository") {
                val vm = createViewModel()
                vm.onLogout()

                coVerify { userRepository.logout() }
            }
        }

        `when`("logout succeeds") {
            then("NavigateToLogin event emitted") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onLogout()

                    awaitItem() shouldBe
                            ProfileViewModel.ProfileEvent.NavigateToLogin

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("logout throws exception") {
            then("ShowError emitted with error message") {
                coEvery { userRepository.logout() } throws
                        Exception("Logout failed. Please try again.")

                val vm = createViewModel()
                vm.events.test {
                    vm.onLogout()

                    awaitItem() shouldBe
                            ProfileViewModel.ProfileEvent
                                .ShowError("Logout failed. Please try again.")

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

                    awaitItem() shouldBe
                            ProfileViewModel.ProfileEvent.NavigateBack

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Reactive Profile Updates
    // Tests that Flow emissions drive UI updates automatically
    // ══════════════════════════════════════════════════════════

    given("user profile changes after screen opens") {

        `when`("getCurrentUser emits updated user name") {
            then("uiState reflects new name immediately") {
                val vm = createViewModel()
                vm.uiState.value.user?.name shouldBe "Swapna"

                // Emit updated user — simulates Room emitting after updateUser()
                userFlow.value = testUser(name = "Swapna Reddy")

                vm.uiState.value.user?.name shouldBe "Swapna Reddy"
            }
        }

        `when`("getCurrentUser emits null after logout") {
            then("isLoggedIn becomes false and error set") {
                val vm = createViewModel()
                vm.uiState.value.isLoggedIn shouldBe true

                userFlow.value = null

                vm.uiState.value.isLoggedIn shouldBe false
                vm.uiState.value.error      shouldNotBe null
            }
        }
    }
})

// ── Local test data helpers ───────────────────────────────────────────────

private fun testUser(
    id:        String        = "u1",
    name:      String        = "Swapna",
    email:     String        = "swapna@example.com",
    phone:     String        = "+919876543210",
    addresses: List<Address> = emptyList(),
) = User(
    id               = id,
    name             = name,
    email            = email,
    phone            = phone,
    profileImage     = "",
    addresses        = addresses,
    selectedLocation = "Koramangala",
)

private fun testAddress(
    id:    String = "a1",
    label: String = "Home",
) = Address(
    id          = id,
    label       = label,
    fullAddress = "123 Test Street, Koramangala, Bengaluru",
    landmark    = "",
    latitude    = 0.0,
    longitude   = 0.0,
)

private fun testOrder(
    id: String = "o1",
) = Order(
    id             = id,
    restaurantId   = "r1",
    restaurantName = "Meghana Foods",
    restaurantImage = "",
    status         = "Delivered",
    timeFriendly   = "Today, 12:00 PM",
    totalAmount    = 249.0,
    items          = emptyList(),
    canReorder     = true,
)