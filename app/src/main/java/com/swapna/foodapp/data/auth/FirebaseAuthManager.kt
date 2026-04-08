package com.swapna.foodapp.data.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.swapna.foodapp.utils.AppConstants
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseAuthManager @Inject constructor() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    // Stored between sendOtp() and verifyOtp() calls
    // Firebase needs this to verify the OTP
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // ── Send OTP ──────────────────────────────────────────────
    // Converts Firebase callback into suspend function
    // Caller gets Result<Unit> — success or failure, no callbacks

    suspend fun sendOtp(
        phoneNumber: String,    // e.g. "+919876543210"
        activity: Activity,     // Required by Firebase for reCAPTCHA
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // Called instantly if phone was auto-verified (rare)
            // or if the same number was verified recently
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification — sign in directly
                signInWithCredential(credential) { result ->
                    if (continuation.isActive) {
                        continuation.resume(result.map { Unit })
                    }
                }
            }

            // Called when Firebase can't verify (wrong number format, quota exceeded)
            override fun onVerificationFailed(exception: FirebaseException) {
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        Exception(exception.message ?: "OTP sending failed")
                    )
                }
            }

            // Called when OTP SMS is sent — normal flow
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // Store for use in verifyOtp()
                storedVerificationId = verificationId
                resendToken          = token

                if (continuation.isActive) {
                    continuation.resume(Result.success(Unit))
                }
            }
        }

        // Build the phone auth request
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(formatPhoneNumber(phoneNumber))
            .setTimeout(AppConstants.OTP_TIMEOUT_SEC.toLong(), TimeUnit.SECONDS)
            .setActivity(activity)   // Required — Firebase attaches reCAPTCHA to Activity
            .setCallbacks(callbacks)
            .apply {
                // If resending, use the resend token to avoid waiting
                resendToken?.let { setForceResendingToken(it) }
            }
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        // Cancel cleanup — if coroutine is cancelled, clean up
        continuation.invokeOnCancellation {
            // Nothing to cancel for Firebase, but good practice
        }
    }

    // ── Verify OTP ────────────────────────────────────────────
    // Takes the 6-digit code user entered
    // Returns Result<String> where String is the Firebase UID

    suspend fun verifyOtp(otp: String): Result<String> {
        val verificationId = storedVerificationId
            ?: return Result.failure(Exception("Session expired. Please request OTP again."))

        return try {
            // Build credential from verificationId + OTP
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)

            suspendCancellableCoroutine { continuation ->
                signInWithCredential(credential) { result ->
                    result.fold(
                        onSuccess = { uid ->
                            if (continuation.isActive) continuation.resume(Result.success(uid))
                        },
                        onFailure = { e ->
                            if (continuation.isActive) continuation.resumeWithException(e)
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Sign In with Credential ───────────────────────────────
    // Used by both auto-verify and manual OTP verify
    // Returns the Firebase UID on success

    private fun signInWithCredential(
        credential: PhoneAuthCredential,
        onResult: (Result<String>) -> Unit,
    ) {
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    onResult(Result.success(uid))
                } else {
                    onResult(Result.failure(Exception("Sign in succeeded but UID is null")))
                }
            }
            .addOnFailureListener { exception ->
                val message = when (exception) {
                    is FirebaseAuthInvalidCredentialsException ->
                        "Wrong OTP. Please check and try again."
                    else ->
                        exception.message ?: "Sign in failed"
                }
                onResult(Result.failure(Exception(message)))
            }
    }

    // ── Helpers ───────────────────────────────────────────────

    // Add country code if not present
    // "9876543210" → "+919876543210"
    private fun formatPhoneNumber(phone: String): String {
        return if (phone.startsWith("+")) phone else "+91$phone"
    }

    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

    fun signOut() = firebaseAuth.signOut()
}