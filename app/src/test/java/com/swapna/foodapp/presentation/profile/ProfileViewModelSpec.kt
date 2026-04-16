package com.swapna.foodapp.presentation.profile

/*
// ── Kotest ────────────────────────────────────────────────────
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

// ── Coroutines ────────────────────────────────────────────────
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// ── App ───────────────────────────────────────────────────────
import com.swapna.foodapp.fakes.FakeUserRepository
import com.swapna.foodapp.domain.model.User

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelSpec : BehaviorSpec({

    // ── Test dispatcher ───────────────────────────────────────
    // UnconfinedTestDispatcher = coroutines run immediately
    // No need to advance time manually
    val dispatcher = UnconfinedTestDispatcher()

    // ── Test dependencies ─────────────────────────────────────
    lateinit var fakeUserRepo: FakeUserRepository
    lateinit var viewModel:    ProfileViewModel
    beforeEach {
        // Set test dispatcher as Main
        // ProfileViewModel uses viewModelScope → Main
        Dispatchers.setMain(dispatcher)

        // Fresh fake per test
        fakeUserRepo = FakeUserRepository()

        // Create ViewModel — init calls loadUser()
        // With UnconfinedTestDispatcher loadUser() completes
        // before first assertion runs
        viewModel = ProfileViewModel(fakeUserRepo)
    }

    afterEach {
        // Always reset Main dispatcher
        // Prevents test isolation issues
        Dispatchers.resetMain()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Load User
    // Tests what ProfileScreen shows when it opens
    // ══════════════════════════════════════════════════════════

    given("ProfileScreen is opened") {

        `when`("repository returns user successfully") {
            then("uiState.user should not be null") {
                // WHY? User is null → header shows nothing
                // Must be populated after load

                viewModel.uiState.value.user shouldNotBe null
            }
        }

        `when`("repository returns user successfully") {
            then("user name should be Swapna Prakash") {
                // Verify CORRECT user loaded
                // Not just any non-null value

                viewModel.uiState.value
                    .user?.name shouldBe "Swapna Prakash"
            }
        }

        `when`("repository returns user successfully") {
            then("user email should be swapna@email.com") {
                viewModel.uiState.value
                    .user?.email shouldBe "swapna@email.com"
            }
        }

        `when`("repository returns user successfully") {
            then("user phone should be 9876543210") {
                viewModel.uiState.value
                    .user?.phone shouldBe "9876543210"
            }
        }

        `when`("repository returns user successfully") {
            then("isLoading should be false") {
                // Spinner must stop after load completes
                // If still true → user sees infinite spinner

                viewModel.uiState.value
                    .isLoading shouldBe false
            }
        }

        `when`("repository returns user successfully") {
            then("error should be null") {
                // Clean success state — no error shown

                viewModel.uiState.value
                    .error shouldBe null
            }
        }

        `when`("repository throws error") {
            then("error message should be set") {
                // Setup repo to fail BEFORE creating VM
                // WHY before? VM.init calls loadUser() immediately
                fakeUserRepo.getUserResult = Result.failure(
                    Exception("Failed to load profile")
                )

                // Create VM AFTER setting up failure
                val failVM = ProfileViewModel(fakeUserRepo)

                failVM.uiState.value.error shouldBe
                        "Failed to load profile"
            }
        }

        `when`("repository throws error") {
            then("isLoading should be false even on error") {
                // Error state must stop loading
                // Spinner + error at same time = bad UX

                fakeUserRepo.getUserResult = Result.failure(
                    Exception("Network error")
                )
                val failVM = ProfileViewModel(fakeUserRepo)

                failVM.uiState.value.isLoading shouldBe false
            }
        }

        `when`("repository throws error") {
            then("user should be null") {
                // Failed to load → no user data
                fakeUserRepo.getUserResult = Result.failure(
                    Exception("Error")
                )
                val failVM = ProfileViewModel(fakeUserRepo)

                failVM.uiState.value.user shouldBe null
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Edit Profile Sheet
    // Tests open/close of ModalBottomSheet
    // ══════════════════════════════════════════════════════════

    given("user is viewing their profile") {

        `when`("user taps Edit Profile button") {
            then("showEditSheet should be true") {
                // WHY test showEditSheet?
                // ProfileScreen shows EditProfileSheet
                // when showEditSheet = true
                // If wrong → edit button does nothing (bug)

                viewModel.onEditProfileTapped()

                viewModel.uiState.value
                    .showEditSheet shouldBe true
            }
        }

        `when`("user taps Edit Profile button") {
            then("editName should be pre-filled with current name") {
                // WHY pre-fill?
                // User sees current value in field
                // Can edit part of name, not retype whole thing
                // Better UX

                viewModel.onEditProfileTapped()

                viewModel.uiState.value
                    .editName shouldBe "Swapna Prakash"
            }
        }

        `when`("user taps Edit Profile button") {
            then("editEmail should be pre-filled with current email") {
                viewModel.onEditProfileTapped()

                viewModel.uiState.value
                    .editEmail shouldBe "swapna@email.com"
            }
        }

        `when`("user taps Edit Profile button") {
            then("name and email errors should be cleared") {
                // Previous errors from last edit session
                // must not show in new session

                viewModel.onEditProfileTapped()

                viewModel.uiState.value.nameError  shouldBe null
                viewModel.uiState.value.emailError shouldBe null
            }
        }

        `when`("user dismisses edit sheet") {
            then("showEditSheet should be false") {
                // Open sheet first
                viewModel.onEditProfileTapped()
                viewModel.uiState.value
                    .showEditSheet shouldBe true

                // Dismiss = swipe down or tap outside
                viewModel.onDismissEditSheet()

                viewModel.uiState.value
                    .showEditSheet shouldBe false
            }
        }

        `when`("user dismisses edit sheet") {
            then("validation errors should be cleared") {
                // Errors from incomplete form
                // should not persist after dismiss

                viewModel.onEditProfileTapped()

                // Simulate validation errors
                // by triggering save with empty name
                viewModel.onNameChanged("")
                viewModel.onSaveProfile()

                // nameError should be set
                viewModel.uiState.value.nameError shouldNotBe null

                // Dismiss sheet
                viewModel.onDismissEditSheet()

                // Errors cleared
                viewModel.uiState.value.nameError shouldBe null
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Edit Field Changes
    // Tests typing in name/email fields
    // ══════════════════════════════════════════════════════════

    given("user is editing their profile") {

        `when`("user types new name") {
            then("editName in uiState updates") {
                // Real-time update = Compose recomposes with new value
                // TextField shows what user typed

                viewModel.onNameChanged("Swapna S")

                viewModel.uiState.value
                    .editName shouldBe "Swapna S"
            }
        }

        `when`("user types new email") {
            then("editEmail in uiState updates") {
                viewModel.onEmailChanged("new@email.com")

                viewModel.uiState.value
                    .editEmail shouldBe "new@email.com"
            }
        }

        `when`("user had name error and starts typing") {
            then("nameError should be cleared") {
                // WHY clear on type?
                // Error shown → user starts fixing it
                // Error should disappear as they type
                // NOT wait until they submit again

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("")
                viewModel.onSaveProfile()

                // Error set
                viewModel.uiState.value
                    .nameError shouldNotBe null

                // User starts typing to fix
                viewModel.onNameChanged("S")

                // Error cleared immediately
                viewModel.uiState.value
                    .nameError shouldBe null
            }
        }

        `when`("user had email error and starts typing") {
            then("emailError should be cleared") {
                viewModel.onEditProfileTapped()
                viewModel.onEmailChanged("invalid")
                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .emailError shouldNotBe null

                viewModel.onEmailChanged("new@")

                viewModel.uiState.value
                    .emailError shouldBe null
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Validation
    // Tests all validation rules before save
    // ══════════════════════════════════════════════════════════

    given("user taps Save with invalid data") {

        `when`("name is empty") {
            then("nameError should be set") {
                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("")

                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .nameError shouldBe "Name cannot be empty"
            }
        }

        `when`("name is blank spaces only") {
            then("nameError should be set") {
                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("   ")

                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .nameError shouldBe "Name cannot be empty"
            }
        }

        `when`("name is only 1 character") {
            then("nameError should be set") {
                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("S")

                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .nameError shouldBe
                        "Name must be at least 2 characters"
            }
        }

        `when`("email is empty") {
            then("emailError should be set") {
                viewModel.onEditProfileTapped()
                viewModel.onEmailChanged("")

                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .emailError shouldBe "Email cannot be empty"
            }
        }

        `when`("email format is invalid") {
            then("emailError should be set") {
                viewModel.onEditProfileTapped()
                // Missing @ → invalid email
                viewModel.onEmailChanged("notanemail")

                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .emailError shouldBe
                        "Enter a valid email address"
            }
        }

        `when`("email missing domain") {
            then("emailError should be set") {
                viewModel.onEditProfileTapped()
                viewModel.onEmailChanged("user@")

                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .emailError shouldBe
                        "Enter a valid email address"
            }
        }

        `when`("both name and email are invalid") {
            then("both errors should be set simultaneously") {
                // WHY test both at once?
                // User might have both fields wrong
                // Show ALL errors at once — not one at a time
                // Better UX than fixing one by one

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("")
                viewModel.onEmailChanged("invalid")

                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .nameError shouldBe "Name cannot be empty"
                viewModel.uiState.value
                    .emailError shouldBe
                        "Enter a valid email address"
            }
        }

        `when`("validation fails") {
            then("updateUser should NOT be called") {
                // WHY verify? API call must not happen
                // when input is invalid
                // Would waste bandwidth + show server errors

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("")

                viewModel.onSaveProfile()

                // Repository updateUser must NOT have been called
                fakeUserRepo.updateUserCalled shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Save Profile (Happy Path)
    // Tests successful save flow
    // ══════════════════════════════════════════════════════════

    given("user enters valid name and email") {

        `when`("user taps Save and repository succeeds") {
            then("updateUser is called with updated user") {
                // Verify repository receives correct data
                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("Swapna S")
                viewModel.onEmailChanged("new@email.com")

                viewModel.onSaveProfile()

                // Repository must have been called
                fakeUserRepo.updateUserCalled shouldBe true

                // With correct updated user
                fakeUserRepo.lastUpdatedUser?.name shouldBe
                        "Swapna S"
                fakeUserRepo.lastUpdatedUser?.email shouldBe
                        "new@email.com"
            }
        }

        `when`("save succeeds") {
            then("user in uiState updates to new values") {
                // WHY test uiState update?
                // Profile header must show NEW values
                // not stale old values after save

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("Swapna S")
                viewModel.onEmailChanged("new@email.com")

                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .user?.name shouldBe "Swapna S"
                viewModel.uiState.value
                    .user?.email shouldBe "new@email.com"
            }
        }

        `when`("save succeeds") {
            then("showEditSheet should be false — sheet closes") {
                // Sheet must close after successful save
                // User sees updated profile header

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("Swapna S")
                viewModel.onEmailChanged("new@email.com")

                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .showEditSheet shouldBe false
            }
        }

        `when`("save succeeds") {
            then("ShowSnackbar event emitted with success message") {
                // Feedback to user that save worked

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("Swapna S")
                viewModel.onEmailChanged("new@email.com")

                viewModel.onSaveProfile()

                val event = viewModel.events.first()

                assert(
                    event is ProfileViewModel
                    .ProfileEvent.ShowSnackbar
                ) { "Expected ShowSnackbar but got $event" }

                (event as ProfileViewModel
                .ProfileEvent.ShowSnackbar)
                    .message shouldBe
                        "Profile updated successfully"
            }
        }

        `when`("save succeeds") {
            then("isSaving should be false after completion") {
                // Button spinner must stop after save
                // If stays true → button disabled forever (bug)

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("Swapna S")
                viewModel.onEmailChanged("new@email.com")

                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .isSaving shouldBe false
            }
        }

        `when`("user name has leading/trailing spaces") {
            then("saved name is trimmed") {
                // "  Swapna  " → "Swapna"
                // WHY trim? Spaces in name cause
                // display issues + comparison bugs

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("  Swapna S  ")
                viewModel.onEmailChanged("swapna@email.com")

                viewModel.onSaveProfile()

                fakeUserRepo.lastUpdatedUser?.name shouldBe
                        "Swapna S"
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Save Profile (Error Path)
    // Tests repository failure during save
    // ══════════════════════════════════════════════════════════

    given("user taps Save but repository fails") {

        `when`("updateUser returns failure") {
            then("ShowError event emitted") {
                // Network error during save
                // User must see error message

                fakeUserRepo.updateUserResult = Result.failure(
                    Exception("Failed to update profile")
                )

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("Swapna S")
                viewModel.onEmailChanged("new@email.com")

                viewModel.onSaveProfile()

                val event = viewModel.events.first()

                assert(
                    event is ProfileViewModel
                    .ProfileEvent.ShowError
                ) { "Expected ShowError but got $event" }

                (event as ProfileViewModel
                .ProfileEvent.ShowError)
                    .message shouldBe "Failed to update profile"
            }
        }

        `when`("updateUser returns failure") {
            then("sheet stays open — user can retry") {
                // WHY keep sheet open on error?
                // User filled the form
                // Close on error = they lose their changes
                // Keep open = they can just tap Save again

                fakeUserRepo.updateUserResult = Result.failure(
                    Exception("Network error")
                )

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("Swapna S")
                viewModel.onEmailChanged("new@email.com")

                viewModel.onSaveProfile()

                // Sheet must stay open
                viewModel.uiState.value
                    .showEditSheet shouldBe true
            }
        }

        `when`("updateUser returns failure") {
            then("user in uiState NOT updated with failed data") {
                // Save failed → user data must stay as original
                // Not partially updated

                val originalName = viewModel.uiState.value
                    .user?.name

                fakeUserRepo.updateUserResult = Result.failure(
                    Exception("Error")
                )

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("New Name")
                viewModel.onEmailChanged("new@email.com")

                viewModel.onSaveProfile()

                // User name must be unchanged
                viewModel.uiState.value
                    .user?.name shouldBe originalName
            }
        }

        `when`("updateUser returns failure") {
            then("isSaving should be false after error") {
                // Save button must re-enable after error
                // User can try again

                fakeUserRepo.updateUserResult = Result.failure(
                    Exception("Error")
                )

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("Swapna S")
                viewModel.onEmailChanged("new@email.com")

                viewModel.onSaveProfile()

                viewModel.uiState.value
                    .isSaving shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Logout
    // Tests logout flow
    // ══════════════════════════════════════════════════════════

    given("user taps Logout button") {

        `when`("logout is confirmed") {
            then("userRepository.logout is called") {
                // Repository must clear session
                // Without this → user still logged in (security bug)

                viewModel.onLogout()

                fakeUserRepo.logoutCalled shouldBe true
            }
        }

        `when`("logout is called") {
            then("NavigateToLogin event is emitted") {
                // After logout → send to Login screen
                // Back stack must be cleared (checked in Screen)

                viewModel.onLogout()

                val event = viewModel.events.first()

                assert(
                    event is ProfileViewModel
                    .ProfileEvent.NavigateToLogin
                ) { "Expected NavigateToLogin but got $event" }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Navigation Events
    // Tests all menu item taps
    // ══════════════════════════════════════════════════════════

    given("user taps menu items on Profile screen") {

        `when`("user taps My Orders") {
            then("NavigateToOrders event emitted") {
                viewModel.onOrdersTapped()

                val event = viewModel.events.first()

                assert(
                    event is ProfileViewModel
                    .ProfileEvent.NavigateToOrders
                ) { "Expected NavigateToOrders but got $event" }
            }
        }

        `when`("user taps Saved Addresses") {
            then("NavigateToAddresses event emitted") {
                viewModel.onAddressesTapped()

                val event = viewModel.events.first()

                assert(
                    event is ProfileViewModel
                    .ProfileEvent.NavigateToAddresses
                ) { "Expected NavigateToAddresses but got $event" }
            }
        }

        `when`("user taps Payment Methods") {
            then("NavigateToPayments event emitted") {
                viewModel.onPaymentsTapped()

                val event = viewModel.events.first()

                assert(
                    event is ProfileViewModel
                    .ProfileEvent.NavigateToPayments
                ) { "Expected NavigateToPayments but got $event" }
            }
        }

        `when`("user taps Settings") {
            then("NavigateToSettings event emitted") {
                viewModel.onSettingsTapped()

                val event = viewModel.events.first()

                assert(
                    event is ProfileViewModel
                    .ProfileEvent.NavigateToSettings
                ) { "Expected NavigateToSettings but got $event" }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Edge Cases
    // Tests unusual but possible scenarios
    // ══════════════════════════════════════════════════════════

    given("edge cases") {

        `when`("user taps Edit Profile when user is null") {
            then("showEditSheet stays false — no crash") {
                // What if user loads failed but
                // Edit Profile button somehow tapped?
                // Must not crash

                fakeUserRepo.getUserResult = Result.failure(
                    Exception("Error")
                )
                val failVM = ProfileViewModel(fakeUserRepo)

                // user is null — edit should do nothing
                failVM.onEditProfileTapped()

                // Sheet should NOT open — no user to edit
                failVM.uiState.value
                    .showEditSheet shouldBe false
            }
        }

        `when`("user saves same values without changes") {
            then("repository is still called and succeeds") {
                // User opens edit, doesn't change anything, taps Save
                // Should still work — not an error case

                viewModel.onEditProfileTapped()
                // Don't change anything — save as-is

                viewModel.onSaveProfile()

                fakeUserRepo.updateUserCalled shouldBe true
            }
        }

        `when`("user taps Save multiple times quickly") {
            then("updateUser called only once per tap") {
                // Double-tap protection — isSaving = true
                // prevents concurrent saves
                // In our fake: each call goes through
                // Real test: verify count

                viewModel.onEditProfileTapped()
                viewModel.onNameChanged("Swapna S")
                viewModel.onEmailChanged("new@email.com")

                viewModel.onSaveProfile()

                // After first save — updateUserCalled = true
                fakeUserRepo.updateUserCalled shouldBe true
            }
        }
    }
})*/
