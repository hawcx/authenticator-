package com.example.flutter_hawcx

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import com.hawcx.HawcxInitializer
import com.hawcx.auth.SignIn
import com.hawcx.auth.SignUp
import com.hawcx.auth.Restore
import com.hawcx.utils.AuthErrorHandler
import com.hawcx.utils.AuthErrorHandler.SignInErrorCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HawcxModule(private val context: Context) : MethodChannel.MethodCallHandler {

    private val apiKey = "YOUR_API_KEY"
    private var signUp: SignUp? = null
    private var signIn: SignIn? = null
    private var restore: Restore? = null

    init {
        signUp = SignUp(context, apiKey)
        signIn = SignIn(context, apiKey)
        restore = Restore(context, apiKey)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "signUp" -> {
                val username = call.argument<String>("username")
                if (username != null) {
                    handleSingleReply(result) {
                        signUp(username, result)
                    }
                } else {
                    result.error("ERROR", "Username is required", null)
                }
            }
            "signIn" -> {
                val username = call.argument<String>("username")
                if (username != null) {
                    handleSingleReply(result) {
                        signIn(username, result)
                    }
                } else {
                    result.error("ERROR", "Username is required", null)
                }
            }
            "handleVerifyOTP" -> {
                val username = call.argument<String>("username")
                val otp = call.argument<String>("otp")
                if (username != null && otp != null) {
                    handleSingleReply(result) {
                        handleVerifyOTP(username, otp, result)
                    }
                } else {
                    result.error("ERROR", "Username and OTP are required", null)
                }
            }
            "checkLastUser" -> {
                handleSingleReply(result) {
                    checkLastUser(result)
                }
            }
            "generateOtpForAccountRestore" -> {
                val email = call.argument<String>("email")
                if (email != null) {
                    handleSingleReply(result) {
                        generateOtpForAccountRestore(email, result)
                    }
                } else {
                    result.error("ERROR", "Email is required", null)
                }
            }
            "verifyOtpForAccountRestore" -> {
                val email = call.argument<String>("email")
                val otp = call.argument<String>("otp")
                if (email != null && otp != null) {
                    handleSingleReply(result) {
                        verifyOtpForAccountRestore(email, otp, result)
                    }
                } else {
                    result.error("ERROR", "Email and OTP are required", null)
                }
            }
            "getLastUser" -> {
                getLastUser(result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun handleSingleReply(result: MethodChannel.Result, block: () -> Unit) {
        var resultSubmitted = false
        try {
            block()
        } catch (e: Exception) {
            if (!resultSubmitted) {
                resultSubmitted = true
                result.error("ERROR", e.message, null)
            }
        }
    }

    // Public method to handle signUp
    public fun signUp(username: String, result: MethodChannel.Result) {
        signUp?.signUp(username, object : SignUp.SignUpCallback {
            override fun showError(errorCode: String) {
                result.error("SIGNUP_ERROR", "SignUp failed: $errorCode", null)
            }

            override fun showError(errorCode: AuthErrorHandler.SignUpErrorCode, errorMessage: String) {
                result.error("SIGNUP_ERROR", "SignUp failed: $errorMessage", null)
            }

            override fun onGenerateOTPSuccess() {
                result.success("OTP generated successfully")
            }

            override fun onSuccessfulSignup() {
                result.success("Sign up successful")
            }
        })
    }

    // Public method to handle signIn
    public fun signIn(username: String, result: MethodChannel.Result) {
        signIn?.signIn(username, object : SignIn.SignInCallback {
            override fun showEmailSignInScreen() {
                result.error("EMAIL_SIGN_IN_REQUIRED", "Please sign in with your email.", null)
            }

            override fun onSuccessfulLogin(username: String) {
                result.success("Login successful for user: $username")
            }

            override fun showError(errorCode: AuthErrorHandler.SignInErrorCode, message: String) {
                result.error("SIGNIN_ERROR", "SignIn failed: $message", null)
            }

            override fun showError(message: String) {
                result.error("SIGNIN_ERROR", "SignIn failed: $message", null)
            }

            override fun initiateBiometricLogin(callback: Runnable) {
                initiateBiometricLogin(result, callback)
            }
        })
    }

    fun getLastUser(result: MethodChannel.Result) {
        try {
            val lastUser = signIn?.getLastUser()
            result.success(lastUser)
        } catch (e: Exception) {
            result.error("GET_LAST_USER_ERROR", "Failed to get last user: ${e.message}", null)
        }
    }
    


    public fun handleVerifyOTP(username: String, otp: String, result: MethodChannel.Result) {
        signUp?.handleVerifyOTP(username, otp, object : SignUp.SignUpCallback {
            override fun showError(errorCode: String) {
                result.error("VERIFY_OTP_ERROR", "OTP verification failed: $errorCode", null)
            }

            override fun showError(errorCode: AuthErrorHandler.SignUpErrorCode, errorMessage: String) {
                result.error("VERIFY_OTP_ERROR", "OTP verification failed: $errorMessage", null)
            }

            override fun onGenerateOTPSuccess() {
                // Not used for OTP verification
            }

            override fun onSuccessfulSignup() {
                result.success("OTP verified and signup complete")
            }
        })
    }

    public fun checkLastUser(result: MethodChannel.Result) {
        signIn?.checkLastUser(object : SignIn.SignInCallback {
            override fun showEmailSignInScreen() {
                result.success("SHOW_EMAIL_SIGN_IN_SCREEN")
            }

            override fun onSuccessfulLogin(username: String) {
                result.success("Login successful for user: $username")
            }

            override fun showError(errorCode: AuthErrorHandler.SignInErrorCode, message: String) {
                result.error("CHECK_LAST_USER_ERROR", "Check last user failed: $message", null)
            }

            override fun showError(message: String) {
                result.error("CHECK_LAST_USER_ERROR", "Check last user failed: $message", null)
            }

            override fun initiateBiometricLogin(callback: Runnable) {
                initiateBiometricLogin(result, callback)
            }
        })
    }

    public fun generateOtpForAccountRestore(email: String, result: MethodChannel.Result) {
        restore?.generateOtp(email, object : Restore.OnSuccessListener {
            override fun onSuccess(message: String) {
                result.success(message)
            }
        }, object : Restore.OnErrorListener {
            override fun onError(error: String) {
                result.error("GENERATE_OTP_ERROR", error, null)
            }
        })
    }

    public fun verifyOtpForAccountRestore(email: String, otp: String, result: MethodChannel.Result) {
        restore?.verifyOtp(email, otp, object : Restore.OnSuccessListener {
            override fun onSuccess(message: String) {
                result.success(message)
            }
        }, object : Restore.OnErrorListener {
            override fun onError(error: String) {
                result.error("VERIFY_OTP_ERROR", error, null)
            }
        })
    }

    public fun initiateBiometricLogin(result: MethodChannel.Result, callback: Runnable) {
        val currentActivity = (context as? androidx.fragment.app.FragmentActivity)

        if (currentActivity == null) {
            result.error("ACTIVITY_ERROR", "Current activity is not available", null)
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            currentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errorMessage: CharSequence) {
                    result.error("BIOMETRIC_AUTH_ERROR", "Authentication error: $errorMessage", null)
                }

                override fun onAuthenticationSucceeded(authResult: BiometricPrompt.AuthenticationResult) {
                    //result.success("Biometric login successful")
                    callback.run()
                }

                override fun onAuthenticationFailed() {
                    result.error("BIOMETRIC_AUTH_FAILED", "Authentication failed", null)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setDescription("Use your fingerprint or face to log in")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}