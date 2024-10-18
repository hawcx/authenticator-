package com.hawcxapp

import android.widget.Toast
import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.hawcx.HawcxInitializer
import com.hawcx.auth.SignUp
import com.hawcx.auth.SignIn
import com.hawcx.auth.Restore
import com.hawcx.utils.AuthErrorHandler
import android.os.Handler
import android.os.Looper


class HawcxModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    // Hardcoded API Key
    private val apiKey = "YOUE_API_KEY"
    private var signUp: SignUp? = null
    private var signIn: SignIn? = null
    private var restore: Restore? = null

    init {
        HawcxInitializer.getInstance().init(reactContext, "apiKey")
        signUp = SignUp(reactContext, apiKey)
        signIn = SignIn(reactContext, apiKey)
        restore = Restore(reactContext, apiKey)
    }
    override fun getName(): String {
        return "HawcxModule"
    }
    
    // Expose the signUp method to React Native
    @ReactMethod
    fun signUp(username: String, promise: Promise) {
        signUp?.signUp(username, object : SignUp.SignUpCallback {
            override fun showError(errorCode: String) {
                promise.reject("SIGNUP_ERROR", "SignUp failed: $errorCode")
            }

            override fun showError(errorCode: AuthErrorHandler.SignUpErrorCode, errorMessage: String) {
                promise.reject("SIGNUP_ERROR", "SignUp failed: $errorMessage")
            }

            override fun onGenerateOTPSuccess() {
                promise.resolve("OTP generated successfully")
            }

            override fun onSuccessfulSignup() {
                promise.resolve("Sign up successful")
            }
        })
    }

    // Expose the handleVerifyOTP method to React Native
    @ReactMethod
    fun handleVerifyOTP(username: String, otp: String, promise: Promise) {
        signUp?.handleVerifyOTP(username, otp, object : SignUp.SignUpCallback {
            override fun showError(errorCode: String) {
                promise.reject("VERIFY_OTP_ERROR", "OTP verification failed: $errorCode")
            }

            override fun showError(errorCode: AuthErrorHandler.SignUpErrorCode, errorMessage: String) {
                promise.reject("VERIFY_OTP_ERROR", "OTP verification failed: $errorMessage")
            }

            override fun onGenerateOTPSuccess() {
                // Not used for OTP verification
            }

            override fun onSuccessfulSignup() {
                promise.resolve("OTP verified and signup complete")
            }
        })
    }

     // Expose the signIn method to React Native
    @ReactMethod
    fun signIn(username: String, promise: Promise) {
        signIn?.signIn(username, object : SignIn.SignInCallback {
            override fun showEmailSignInScreen() {
                promise.reject("EMAIL_SIGN_IN_REQUIRED", "Please sign in with your email.")
            }

            override fun onSuccessfulLogin(username: String) {
                promise.resolve("Login successful")
            }

            override fun showError(errorCode: AuthErrorHandler.SignInErrorCode, message: String) {
                promise.reject("SIGNIN_ERROR", "SignIn failed: $message")
            }

            override fun showError(message: String) {
                promise.reject("SIGNIN_ERROR", "SignIn failed: $message")
            }

            override fun initiateBiometricLogin(callback: Runnable) {
                initiateBiometricLogin(promise, callback)
            }
        })
    }

    // Expose the getLastUser method to React Native
    @ReactMethod
    fun getLastUser(promise: Promise) {
        try {
            val lastUser = signIn?.getLastUser() ?: ""
            promise.resolve(lastUser)
        } catch (e: Exception) {
            promise.reject("GET_LAST_USER_ERROR", "Failed to get last user: ${e.message}")
        }
    }

    // Expose the checkLastUser method to React Native
    @ReactMethod
    fun checkLastUser(promise: Promise) {
        signIn?.checkLastUser(object : SignIn.SignInCallback {
            override fun showEmailSignInScreen() {
                promise.resolve("SHOW_EMAIL_SIGN_IN_SCREEN")
            }

            override fun onSuccessfulLogin(username: String) {
                promise.resolve("Login successful for user: $username")
            }

            override fun showError(errorCode: AuthErrorHandler.SignInErrorCode, message: String) {
                promise.reject("CHECK_LAST_USER_ERROR", "Check last user failed: $message")
            }

            override fun showError(message: String) {
                promise.reject("CHECK_LAST_USER_ERROR", "Check last user failed: $message")
            }

            override fun initiateBiometricLogin(callback: Runnable) {
                initiateBiometricLogin(promise, callback)
            }
        })
    }

    // Method to handle biometric login using React Native Biometrics
    @ReactMethod
    private fun initiateBiometricLogin(promise: Promise, callback: Runnable) {
        val currentActivity = reactApplicationContext.currentActivity

        // Ensure currentActivity is not null
        if (currentActivity == null) {
            promise.reject("ACTIVITY_ERROR", "Current activity is not available")
            return
        }

        // Ensure currentActivity is a FragmentActivity
        if (currentActivity !is androidx.fragment.app.FragmentActivity) {
            promise.reject("ACTIVITY_ERROR", "Current activity is not a FragmentActivity")
            return
        }

        // Run on the main thread to avoid fragment transaction issues
        Handler(Looper.getMainLooper()).post {
            val fragmentActivity = currentActivity as androidx.fragment.app.FragmentActivity
            val executor = ContextCompat.getMainExecutor(reactApplicationContext)

            // Create the BiometricPrompt
            val biometricPrompt = BiometricPrompt(
                fragmentActivity, 
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errorMessage: CharSequence) {
                        super.onAuthenticationError(errorCode, errorMessage)
                        promise.reject("BIOMETRIC_AUTH_ERROR", "Authentication error: $errorMessage")
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        //promise.resolve("Biometric login successful")
                        callback.run()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        promise.reject("BIOMETRIC_AUTH_FAILED", "Authentication failed")
                    }
                })

            // Build the BiometricPrompt promptInfo
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setDescription("Use your fingerprint or face to log in")
                .setNegativeButtonText("Cancel")
                .build()

            // Show the biometric prompt to the user
            biometricPrompt.authenticate(promptInfo)
        }
    }


    // Expose the generateOtpForAccountRestore method for account restoration
    @ReactMethod
    fun generateOtpForAccountRestore(email: String, promise: Promise) {
        restore?.generateOtp(email, object : Restore.OnSuccessListener {
            override fun onSuccess(message: String) {
                promise.resolve(message)
            }
        }, object : Restore.OnErrorListener {
            override fun onError(error: String) {
                promise.reject("GENERATE_OTP_ERROR", error)
            }
        })
    }

    // Expose the verifyOtpForAccountRestore method for account restoration
    @ReactMethod
    fun verifyOtpForAccountRestore(email: String, otp: String, promise: Promise) {
        restore?.verifyOtp(email, otp, object : Restore.OnSuccessListener {
            override fun onSuccess(message: String) {
                promise.resolve(message)
            }
        }, object : Restore.OnErrorListener {
            override fun onError(error: String) {
                promise.reject("VERIFY_OTP_ERROR", error)
            }
        })
    }

}
