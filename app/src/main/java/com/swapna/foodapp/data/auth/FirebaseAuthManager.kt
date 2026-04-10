package com.swapna.foodapp.data.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.util.Log
import com.google.firebase.FirebaseTooManyRequestsException

@Singleton
class FirebaseAuthManager @Inject constructor() {

    private val TAG = "FirebaseAuthManager"
    private val auth = FirebaseAuth.getInstance()

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    init {
        // ✅ KEY FIX: Disable App Verification for debug builds
        // Forces reCAPTCHA flow instead of Play Integrity
        // reCAPTCHA works on ALL devices including debug builds
        if (com.swapna.foodapp.BuildConfig.DEBUG) {
            auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(false)
        }
    }

    suspend fun sendOtp(
        phoneNumber: String,
        activity: Activity,
    ): Result<Unit> = suspendCancellableCoroutine { cont ->

        val formatted = formatPhone(phoneNumber)
        Log.d(TAG, "Sending OTP to: $formatted")

       /* FirebaseAuth.getInstance().firebaseAuthSettings
            .setAppVerificationDisabledForTesting(true)*/

        val callbacks = object :
            PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(
                credential: PhoneAuthCredential,
            ) {
                Log.d(TAG, "Auto verified")
                if (cont.isActive) cont.resume(Result.success(Unit))
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "onVerificationFailed: ${e.message}")
                val msg = when (e) {
                    is FirebaseAuthInvalidCredentialsException ->
                        "Invalid phone number. Use 10 digits."
                    is FirebaseTooManyRequestsException ->
                        "Too many requests. Try after some time."
                    else -> e.message ?: "OTP sending failed."
                }
                if (cont.isActive) {
                    cont.resume(Result.failure(Exception(msg)))
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                Log.d(TAG, "OTP sent successfully ✅")
                storedVerificationId = verificationId
                resendToken          = token
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
            Log.e(TAG, "Exception: ${e.message}")
            if (cont.isActive) {
                cont.resume(
                    Result.failure(Exception(e.message ?: "Firebase error"))
                )
            }
        }

        cont.invokeOnCancellation { Log.d(TAG, "Cancelled") }
    }

    suspend fun verifyOtp(otp: String): Result<String> {
        val verificationId = storedVerificationId
            ?: return Result.failure(
                Exception("Session expired. Tap Send OTP again.")
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
                                    Result.failure(Exception("Login failed."))
                                )
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        val msg = when (e) {
                            is FirebaseAuthInvalidCredentialsException ->
                                "Wrong OTP. Please check and try again."
                            else -> e.message ?: "Verification failed"
                        }
                        if (cont.isActive) {
                            cont.resume(Result.failure(Exception(msg)))
                        }
                    }
            } catch (e: Exception) {
                if (cont.isActive) {
                    cont.resume(
                        Result.failure(Exception(e.message ?: "Error"))
                    )
                }
            }
        }
    }

    private fun formatPhone(phone: String): String = when {
        phone.startsWith("+")                        -> phone
        phone.startsWith("91") && phone.length == 12 -> "+$phone"
        phone.length == 10                           -> "+91$phone"
        else                                         -> "+91$phone"
    }

    fun isSignedIn(): Boolean = auth.currentUser != null
    fun getUid(): String?     = auth.currentUser?.uid
    fun signOut()             = auth.signOut()
}