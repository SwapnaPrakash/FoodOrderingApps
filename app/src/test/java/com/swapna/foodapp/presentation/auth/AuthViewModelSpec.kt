package com.swapna.foodapp.presentation.auth

import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.fakeUser
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelSpec : BehaviorSpec({

    // ── Test doubles
    val userRepository = mockk<UserRepository>()
    lateinit var viewModel: AuthViewModel

    // ── Setup / Teardown
    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AuthViewModel(userRepository)
    }

    afterEach {
        Dispatchers.resetMain()
    }

    // GIVEN: Initial state
    given("ViewModel is just created") {
        `when`("no action has been taken") {
            then("state should be Idle") {
                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Idle>()
            }
        }
    }

    // GIVEN: Invalid phone — all actions INSIDE then
    given("phone number is less than 10 digits") {
        `when`("sendOtp is called with '98765'") {
            then("state should be Error — no API call made") {

                viewModel.sendOtp("98765")

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                val error = viewModel.state.value as AuthViewModel.AuthState.Error
                error.message.shouldNotBeEmpty()
                coVerify(exactly = 0) { userRepository.sendOtp(any()) }
            }
        }
    }

    given("phone number is empty string") {
        `when`("sendOtp is called with ''") {
            then("state should be Error") {
                viewModel.sendOtp("")

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                coVerify(exactly = 0) { userRepository.sendOtp(any()) }
            }
        }
    }

    given("phone number contains letters") {
        `when`("sendOtp is called with 'abc1234567'") {
            then("state should be Error — letters are not valid") {
                viewModel.sendOtp("abc1234567")

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                coVerify(exactly = 0) { userRepository.sendOtp(any()) }
            }
        }
    }

    given("phone number has more than 10 digits") {
        `when`("sendOtp is called with '123456789012'") {
            then("state should be Error") {
                viewModel.sendOtp("123456789012")

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                coVerify(exactly = 0) { userRepository.sendOtp(any()) }
            }
        }
    }

    // GIVEN: Valid phone + API outcomes
    given("phone number is valid 10 digits") {

        `when`("sendOtp is called and repository returns success") {
            then("state should be OtpSent with the phone number") {
                coEvery {
                    userRepository.sendOtp("9876543210")
                } returns Result.success(Unit)

                viewModel.sendOtp("9876543210")

                viewModel.state.value shouldBe
                        AuthViewModel.AuthState.OtpSent("9876543210")
            }
        }

        `when`("sendOtp is called and repository throws network error") {
            then("state should be Error with the failure message") {
                coEvery {
                    userRepository.sendOtp(any())
                } returns Result.failure(Exception("Network unavailable"))

                viewModel.sendOtp("9876543210")

                val state = viewModel.state.value
                state.shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                (state as AuthViewModel.AuthState.Error).message shouldBe
                        "Network unavailable"
            }
        }
    }

    // GIVEN: OTP has been sent
    // Use nested beforeEach for shared OTP setup
    given("OTP was sent to 9876543210") {

        // nested beforeEach — runs AFTER outer beforeEach
        // Order: clearAllMocks → create viewModel → this block → then
        beforeEach {
            coEvery {
                userRepository.sendOtp(any())
            } returns Result.success(Unit)
            viewModel.sendOtp("9876543210")
            // viewModel is now in OtpSent state for all thens inside this given
        }

        `when`("verifyOtp is called with correct 6-digit OTP") {
            then("state should be Success with the returned User") {
                val mockUser = fakeUser()
                coEvery {
                    userRepository.verifyOtp("123456")
                } returns Result.success(mockUser)

                viewModel.verifyOtp("123456")

                viewModel.state.value shouldBe
                        AuthViewModel.AuthState.Success(mockUser)
            }
        }

        `when`("verifyOtp is called with wrong OTP") {
            then("state should be Error with the failure message") {
                coEvery {
                    userRepository.verifyOtp(any())
                } returns Result.failure(Exception("Wrong OTP. Try again."))

                viewModel.verifyOtp("000000")

                val state = viewModel.state.value
                state.shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                (state as AuthViewModel.AuthState.Error).message shouldBe
                        "Wrong OTP. Try again."
            }
        }

        `when`("verifyOtp is called with only 4 digits") {
            then("state should be Error — OTP too short") {
                viewModel.verifyOtp("1234")

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                coVerify(exactly = 0) { userRepository.verifyOtp(any()) }
            }
        }

        `when`("verifyOtp is called with empty string") {
            then("state should be Error") {
                viewModel.verifyOtp("")

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                coVerify(exactly = 0) { userRepository.verifyOtp(any()) }
            }
        }
    }

    // GIVEN: Slow network / loading transition
    given("valid phone and slow network") {
        `when`("sendOtp is called") {
            then("final state should be OtpSent — Loading was intermediate") {
                coEvery {
                    userRepository.sendOtp(any())
                } returns Result.success(Unit)

                viewModel.sendOtp("9876543210")

                // UnconfinedTestDispatcher runs coroutine synchronously
                // so Loading → OtpSent happens before this assertion
                viewModel.state.value shouldBe
                        AuthViewModel.AuthState.OtpSent("9876543210")
            }
        }
    }

    // GIVEN: resetState
    given("state is currently Error") {
        `when`("resetState is called") {
            then("state should go back to Idle") {
                // First cause an error
                viewModel.sendOtp("123")
                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()

                // Then reset
                viewModel.resetState()

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Idle>()
            }
        }
    }

    // GIVEN: Background activity error
    given("app is in background when OTP is requested") {
        `when`("sendOtp is called and repo returns activity error") {
            then("state should be Error with background message") {
                coEvery {
                    userRepository.sendOtp(any())
                } returns Result.failure(
                    Exception("App is in background. Please reopen and try again.")
                )

                viewModel.sendOtp("9876543210")

                val state = viewModel.state.value
                state.shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                (state as AuthViewModel.AuthState.Error).message shouldBe
                        "App is in background. Please reopen and try again."
            }
        }
    }

    // GIVEN: Firebase rejects OTP
    given("OTP was sent successfully and Firebase rejects verify") {

        beforeEach {
            coEvery {
                userRepository.sendOtp(any())
            } returns Result.success(Unit)
            viewModel.sendOtp("9876543210")
        }

        `when`("verifyOtp called with wrong OTP and Firebase rejects") {
            then("state should be Error with Firebase rejection message") {
                coEvery {
                    userRepository.verifyOtp(any())
                } returns Result.failure(
                    Exception("Wrong OTP. Please check and try again.")
                )

                viewModel.verifyOtp("999999")

                val state = viewModel.state.value
                state.shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                (state as AuthViewModel.AuthState.Error).message shouldBe
                        "Wrong OTP. Please check and try again."
            }
        }
    }


    // GIVEN: Activity is in background (activityProvider returns null)
    given("app is in background when OTP is requested") {
        `when`("sendOtp is called and repo returns activity error") {

            then("state should be Error with background message") {
                coEvery {
                    userRepository.sendOtp(any())
                } returns Result.failure(
                    Exception("App is in background. Please reopen and try again.")
                )

                viewModel.sendOtp("9876543210")

                val state = viewModel.state.value
                state.shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                val error = state as AuthViewModel.AuthState.Error
                error.message shouldBe
                        "App is in background. Please reopen and try again."
            }
        }
    }

    // GIVEN: Firebase returns wrong OTP error
    given("OTP was sent successfully") {

        beforeEach {
            coEvery {
                userRepository.sendOtp(any())
            } returns Result.success(Unit)
            viewModel.sendOtp("9876543210")
        }

        `when`("verifyOtp called with wrong OTP and Firebase rejects") {

            then("state should be Error with Firebase rejection message") {
                coEvery {
                    userRepository.verifyOtp(any())
                } returns Result.failure(
                    Exception("Wrong OTP. Please check and try again.")
                )

                viewModel.verifyOtp("999999")

                val state = viewModel.state.value
                state.shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                val error = state as AuthViewModel.AuthState.Error
                error.message shouldBe
                        "Wrong OTP. Please check and try again."
            }
        }
    }

})