package com.kkt.dietadvisor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kkt.dietadvisor.utility.AuthStateUtil
import com.kkt.dietadvisor.utility.TokenStateUtil
import com.kkt.dietadvisor.utility.UserInfoUtil
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


class LoginActivity : AppCompatActivity() {

    private lateinit var authService: AuthorizationService
    private lateinit var authState: AuthState
    private lateinit var authorizationLauncher: ActivityResultLauncher<Intent>

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize AuthorizationService & AuthState
        authService = AuthorizationService(this)
        authState = AuthStateUtil.readAuthState(this)

        // Attempt silent login
        attemptSilentLogin()

        // Initialize the authorizationLauncher
        authorizationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            Log.d(TAG, "Activity Result Code: $activityResult")
            if (activityResult.resultCode == RESULT_OK) {
                handleAuthorizationResult(activityResult)
            } else {
                Log.e("Authorization", "Authorization failed or was cancelled.")
            }
        }

        /* Init views */
        val signUpTextView = findViewById<TextView>(R.id.sign_up_request)
        signUpRequest(signUpTextView)

        val signIn = findViewById<LinearLayout>(R.id.sign_in_google)
        signIn.setOnClickListener {
            // Check if auth state is valid and has a valid access token
            if (authState.isAuthorized) {
                // Check if access token is expired and renew if necessary
                TokenStateUtil.checkAndRenewAccessToken(this, authState, authService) { _ ->
                    // AuthState should have a valid access token
                    val accessToken = authState.accessToken
                    accessToken?.let {
                        Log.d(TAG, "Access Token: $accessToken")
                        // Check if user exists using the existing access token
                        UserInfoUtil.getUserInfo(this, accessToken, client) { userExists, _ ->
                            if (userExists) {
                                // User exists -> Move to homepage
                                runOnUiThread {
                                    Log.d(TAG, "User Exists, Signing In...")
                                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, HomePage::class.java))
                                    finish() // Prevent back navigation to login screen
                                }
                            } else {
                                // User does not exist -> Move to sign-up screen
                                runOnUiThread {
                                    Log.d(TAG, "User Not Found, Prompting Sign-up...")
                                    Toast.makeText(this, "Please sign up!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, SignUpwithOAuth::class.java))
                                }
                            }
                        }
                    }
                }
            } else {
                // AuthState is invalid or access token is null. Perform authorization request
                launchAuthorizationRequest()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Dispose of the AuthorizationService to prevent leaks
        authService.dispose()
    }

    private fun launchAuthorizationRequest() {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse(getString(R.string.GOOGLE_AUTHORIZATION_ENDPOINT)),  // Authorization endpoint
            Uri.parse(getString(R.string.GOOGLE_TOKEN_ENDPOINT))           // Token endpoint
        )

        val authRequest = AuthorizationRequest.Builder(
            serviceConfig,
            getString(R.string.ANDROID_CLIENT_ID),              // Android Client ID
            ResponseTypeValues.CODE,
            Uri.parse(getString(R.string.GOOGLE_REDIRECT_URI))  // Redirect URI
        )
            .setScopes("openid", "profile", "email")
            .build()

        val intent = authService.getAuthorizationRequestIntent(authRequest)
        authorizationLauncher.launch(intent)
    }

    private fun handleAuthorizationResult(authorizationResult: ActivityResult) {
        val data = authorizationResult.data
        val authResponse = data?.let { AuthorizationResponse.fromIntent(it) }
        val authException = data?.let { AuthorizationException.fromIntent(it) }

        authResponse?.let {
            val tokenRequest = authResponse.createTokenExchangeRequest()
            authService.performTokenRequest(tokenRequest) { tokenResponse, tokenException ->
                tokenResponse?.let {
                    val accessToken = tokenResponse.accessToken
                    val refreshToken = tokenResponse.refreshToken
                    Log.i(TAG, "Access token: $accessToken")
                    Log.i(TAG, "Refresh token: $refreshToken")

                    // Update Auth state and save refresh token
                    authState.update(tokenResponse, tokenException)
                    AuthStateUtil.writeAuthState(this, authState)
                    TokenStateUtil.saveRefreshToken(this, refreshToken)

                    // Use the access token to get the user info from the backend
                    accessToken?.let {
                        UserInfoUtil.getUserInfo(this, accessToken, client) { userExists, _ ->
                            if (userExists) { // User exists -> Move to home page
                                runOnUiThread {
                                    Toast.makeText(this, "Welcome back to Diet Advisor!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, HomePage::class.java))
                                    finish() // Prevent navigation back to login screen
                                }
                            } else { // User doesn't exist -> Move to sign up screen
                                runOnUiThread {
                                    Log.d(TAG, "Creating User Now")
                                    Toast.makeText(this, "Please create an account!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, SignUpwithOAuth::class.java))
                                }
                            }
                        }
                    } ?: Log.e(TAG, "Unable to retrieve access token")
                } ?: Log.e(TAG, "Token exchange failed: ${tokenException?.message}")
            }
        } ?: run {
            authException?.let {
                Log.e(TAG, "Authorization failed: ${authException.message}")
            }
        }
    }

    private fun attemptSilentLogin() {
        // Check if we have a saved refresh token
        val savedRefreshToken = TokenStateUtil.getRefreshToken(this)
        savedRefreshToken?.let {
            Log.d(TAG, "Attempting silent login using saved refresh token...")

            TokenStateUtil.checkAndRenewAccessToken(this, authState, authService) { _ ->
                val accessToken = authState.accessToken
                accessToken?.let {
                    Log.d(TAG, "Silent Login Success! Access Token: $accessToken")
                    UserInfoUtil.getUserInfo(this, accessToken, client) { userExists, _ ->
                        if (userExists) {
                            runOnUiThread {
                                Log.d(TAG, "User Exists!. Signing in...")
                                Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomePage::class.java ))
                                finish() // Prevent back navigation to the login screen
                            }
                        }
                    }
                }
            }
        } ?: Log.d(TAG, "Silent Login Failed! No stored refresh token")
    }

    private fun signUpRequest(signUpTextView: TextView) {
        val text = "Not a member? Sign up now!"
        val spannableString = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Just move to the sign-up screen
                val intent = Intent(this@LoginActivity, SignUpwithOAuth::class.java)
                widget.context.startActivity(intent)
            }
        }

        val startIndex = text.indexOf("Sign up now!")
        val endIndex = startIndex + "Sign up now!".length
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val colorSpan =
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.greenText))
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val underlineSpan = UnderlineSpan()
        spannableString.setSpan(
            underlineSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        signUpTextView.text = spannableString
        signUpTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    companion object {
        const val TAG: String = "LoginActivity"
    }
}