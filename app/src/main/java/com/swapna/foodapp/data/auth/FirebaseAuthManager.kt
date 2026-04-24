package com.swapna.foodapp.data.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.swapna.foodapp.utils.AppConstants.ERROR
import com.swapna.foodapp.utils.AppConstants.FIREBASE_ERROR
import com.swapna.foodapp.utils.AppConstants.LOGIN_FAILED
import com.swapna.foodapp.utils.AppConstants.OTP_SEND_FAILED
import com.swapna.foodapp.utils.AppConstants.PHONE_COUNTRY_CODE
import com.swapna.foodapp.utils.AppConstants.PHONE_LENGTH
import com.swapna.foodapp.utils.AppConstants.SESSION_EXPIRED
import com.swapna.foodapp.utils.AppConstants.VERIFY_FAILED
import com.swapna.foodapp.utils.AppConstants.WRONG_OTP
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FirebaseAuthManager @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    init {
        if (com.swapna.foodapp.BuildConfig.DEBUG) {
            auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(false)
        }
    }

    suspend fun sendOtp(
        phoneNumber: String,
        activity: Activity,
    ): Result<Unit> = suspendCancellableCoroutine { cont ->
        val formatted = formatPhone(phoneNumber)
        val callbacks = object :
            PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(
                credential: PhoneAuthCredential,
            ) {
                if (cont.isActive) cont.resume(Result.success(Unit))
            }

            override fun onVerificationFailed(
                exception: FirebaseException,
            ) {
                if (cont.isActive) {
                    cont.resume(
                        Result.failure(
                            Exception(
                                exception.message ?: OTP_SEND_FAILED
                            )
                        )
                    )
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                storedVerificationId = verificationId
                resendToken = token
                if (cont.isActive) {
                    cont.resume(Result.success(Unit))
                }
            }
        }

        try {
            val builder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formatted)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)

            resendToken?.let { builder.setForceResendingToken(it) }

            PhoneAuthProvider.verifyPhoneNumber(builder.build())

        } catch (e: Exception) {
            if (cont.isActive) {
                cont.resume(
                    Result.failure(Exception(e.message ?: FIREBASE_ERROR))
                )
            }
        }

        cont.invokeOnCancellation {}
    }

    suspend fun verifyOtp(otp: String): Result<String> {
        val verificationId = storedVerificationId
            ?: return Result.failure(
                Exception(SESSION_EXPIRED)
            )

        return suspendCancellableCoroutine { cont ->
            try {
                val credential = PhoneAuthProvider.getCredential(
                    verificationId, otp
                )
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        val uid = result.user?.uid
                        if (cont.isActive) {
                            if (uid != null) {
                                cont.resume(Result.success(uid))
                            } else {
                                cont.resume(
                                    Result.failure(Exception(LOGIN_FAILED))
                                )
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        val msg = when (e) {
                            is FirebaseAuthInvalidCredentialsException ->
                                WRONG_OTP

                            else -> e.message ?: VERIFY_FAILED
                        }
                        if (cont.isActive) {
                            cont.resume(Result.failure(Exception(msg)))
                        }
                    }
            } catch (e: Exception) {
                if (cont.isActive) {
                    cont.resume(
                        Result.failure(Exception(e.message ?: ERROR))
                    )
                }
            }
        }
    }

    private fun formatPhone(phone: String): String = when {
        phone.startsWith("+") -> phone
        phone.startsWith(PHONE_COUNTRY_CODE) && phone.length == 12 -> "+$phone"
        phone.length == PHONE_LENGTH -> "$PHONE_COUNTRY_CODE$phone"
        else -> "$PHONE_COUNTRY_CODE$phone"
    }

    fun isSignedIn(): Boolean = auth.currentUser != null
    fun getUid(): String? = auth.currentUser?.uid
    fun signOut() = auth.signOut()
}