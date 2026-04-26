package com.swapna.foodapp.presentation.auth

import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.TestConstants.ERR_APP_IN_BACKGROUND
import com.swapna.foodapp.utils.TestConstants.ERR_NETWORK_UNAVAILABLE
import com.swapna.foodapp.utils.TestConstants.ERR_WRONG_OTP_FIREBASE
import com.swapna.foodapp.utils.TestConstants.ERR_WRONG_OTP_REPOSITORY
import com.swapna.foodapp.utils.TestConstants.OTP_EMPTY
import com.swapna.foodapp.utils.TestConstants.OTP_TOO_SHORT
import com.swapna.foodapp.utils.TestConstants.OTP_WRONG
import com.swapna.foodapp.utils.TestConstants.OTP_WRONG_FIREBASE
import com.swapna.foodapp.utils.TestConstants.PHONE_EMPTY
import com.swapna.foodapp.utils.TestConstants.PHONE_TOO_LONG
import com.swapna.foodapp.utils.TestConstants.PHONE_TOO_SHORT
import com.swapna.foodapp.utils.TestConstants.PHONE_WITH_LETTERS
import com.swapna.foodapp.utils.TestConstants.VALID_OTP
import com.swapna.foodapp.utils.TestConstants.VALID_PHONE
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

    val userRepository = mockk<UserRepository>()
    lateinit var viewModel: AuthViewModel

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AuthViewModel(userRepository)
    }

    afterEach { Dispatchers.resetMain() }

    // ══════════════════════════════════════════════════════════
    // Initial State
    // ══════════════════════════════════════════════════════════

    given("ViewModel is just created") {
        `when`("no action has been taken") {
            then("state should be Idle") {
                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Idle>()
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // Phone Validation — invalid inputs
    // ══════════════════════════════════════════════════════════

    given("phone number is less than 10 digits") {
        `when`("sendOtp is called with '$PHONE_TOO_SHORT'") {
            then("state should be Error — no API call made") {
                viewModel.sendOtp(PHONE_TOO_SHORT)

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                (viewModel.state.value as AuthViewModel.AuthState.Error)
                    .message.shouldNotBeEmpty()
                coVerify(exactly = 0) { userRepository.sendOtp(any()) }
            }
        }
    }

    given("phone number is empty") {
        `when`("sendOtp is called with empty string") {
            then("state should be Error — no API call made") {
                viewModel.sendOtp(PHONE_EMPTY)

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                coVerify(exactly = 0) { userRepository.sendOtp(any()) }
            }
        }
    }

    given("phone number contains letters") {
        `when`("sendOtp is called with '$PHONE_WITH_LETTERS'") {
            then("state should be Error — letters are not valid") {
                viewModel.sendOtp(PHONE_WITH_LETTERS)                // ✅ was "9876abc210"

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                coVerify(exactly = 0) { userRepository.sendOtp(any()) }
            }
        }
    }

    given("phone number has more than 10 digits") {
        `when`("sendOtp is called with '$PHONE_TOO_LONG'") {
            then("state should be Error — no API call made") {
                viewModel.sendOtp(PHONE_TOO_LONG)                    // ✅ was "98765432101"

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                coVerify(exactly = 0) { userRepository.sendOtp(any()) }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // Send OTP — valid phone + API outcomes
    // ══════════════════════════════════════════════════════════

    given("phone number is valid 10 digits") {

        `when`("sendOtp is called and repository returns success") {
            then("state should be OtpSent with the phone number") {
                coEvery {
                    userRepository.sendOtp(VALID_PHONE)              // ✅ was "9876543210"
                } returns Result.success(Unit)

                viewModel.sendOtp(VALID_PHONE)

                viewModel.state.value shouldBe
                        AuthViewModel.AuthState.OtpSent(VALID_PHONE)
            }
        }

        `when`("sendOtp is called and repository returns network error") {
            then("state should be Error with the failure message") {
                coEvery {
                    userRepository.sendOtp(any())
                } returns Result.failure(Exception(ERR_NETWORK_UNAVAILABLE)) // ✅ was hardcoded

                viewModel.sendOtp(VALID_PHONE)

                val state = viewModel.state.value
                state.shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                (state as AuthViewModel.AuthState.Error).message shouldBe
                        ERR_NETWORK_UNAVAILABLE
            }
        }

        `when`("sendOtp is called and app is in background") {
            then("state should be Error with background message") {
                coEvery {
                    userRepository.sendOtp(any())
                } returns Result.failure(Exception(ERR_APP_IN_BACKGROUND)) // ✅ was hardcoded

                viewModel.sendOtp(VALID_PHONE)

                val state = viewModel.state.value
                state.shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                (state as AuthViewModel.AuthState.Error).message shouldBe
                        ERR_APP_IN_BACKGROUND
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // Verify OTP — after OTP sent
    // ══════════════════════════════════════════════════════════

    given("OTP was sent to $VALID_PHONE") {

        beforeEach {
            coEvery {
                userRepository.sendOtp(any())
            } returns Result.success(Unit)
            viewModel.sendOtp(VALID_PHONE)
        }

        `when`("verifyOtp is called with correct 6-digit OTP") {
            then("state should be Success with the returned User") {
                val mockUser = fakeUser()
                coEvery {
                    userRepository.verifyOtp(VALID_OTP)              // ✅ was "123456"
                } returns Result.success(mockUser)

                viewModel.verifyOtp(VALID_OTP)

                viewModel.state.value shouldBe
                        AuthViewModel.AuthState.Success(mockUser)
            }
        }

        `when`("verifyOtp is called with wrong OTP") {
            then("state should be Error with repository failure message") {
                coEvery {
                    userRepository.verifyOtp(any())
                } returns Result.failure(Exception(ERR_WRONG_OTP_REPOSITORY)) // ✅

                viewModel.verifyOtp(OTP_WRONG)                       // ✅ was "000000"

                val state = viewModel.state.value
                state.shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                (state as AuthViewModel.AuthState.Error).message shouldBe
                        ERR_WRONG_OTP_REPOSITORY
            }
        }

        `when`("verifyOtp is called and Firebase rejects OTP") {
            then("state should be Error with Firebase rejection message") {
                coEvery {
                    userRepository.verifyOtp(any())
                } returns Result.failure(Exception(ERR_WRONG_OTP_FIREBASE))   // ✅

                viewModel.verifyOtp(OTP_WRONG_FIREBASE)              // ✅ was "999999"

                val state = viewModel.state.value
                state.shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                (state as AuthViewModel.AuthState.Error).message shouldBe
                        ERR_WRONG_OTP_FIREBASE
            }
        }

        `when`("verifyOtp is called with OTP shorter than 6 digits") {
            then("state should be Error — no API call made") {
                viewModel.verifyOtp(OTP_TOO_SHORT)                   // ✅ was "1234"

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                coVerify(exactly = 0) { userRepository.verifyOtp(any()) }
            }
        }

        `when`("verifyOtp is called with empty string") {
            then("state should be Error — no API call made") {
                viewModel.verifyOtp(OTP_EMPTY)                       // ✅ was ""

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()
                coVerify(exactly = 0) { userRepository.verifyOtp(any()) }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // Loading state
    // ══════════════════════════════════════════════════════════

    given("valid phone and slow network") {
        `when`("sendOtp is called") {
            then("final state should be OtpSent — Loading was intermediate") {
                coEvery {
                    userRepository.sendOtp(any())
                } returns Result.success(Unit)

                viewModel.sendOtp(VALID_PHONE)

                viewModel.state.value shouldBe
                        AuthViewModel.AuthState.OtpSent(VALID_PHONE)
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // Reset State
    // ══════════════════════════════════════════════════════════

    given("state is currently Error") {
        `when`("resetState is called") {
            then("state should go back to Idle") {
                viewModel.sendOtp(PHONE_TOO_SHORT)
                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Error>()

                viewModel.resetState()

                viewModel.state.value
                    .shouldBeInstanceOf<AuthViewModel.AuthState.Idle>()
            }
        }
    }
})