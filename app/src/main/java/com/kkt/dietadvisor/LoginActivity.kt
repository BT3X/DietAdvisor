package com.kkt.dietadvisor

import android.content.Intent
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kkt.dietadvisor.models.AccountInfo
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID

const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {

    private lateinit var authorizationLauncher: ActivityResultLauncher<IntentSenderRequest>

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

        // Initialize the authorizationLauncher
        authorizationLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { activityResult ->
            Log.d(TAG, "onCreate: activityResult Code: $activityResult")
            if (activityResult.resultCode == RESULT_OK) {
                val authorizationResponse = Identity.getAuthorizationClient(this)
                    .getAuthorizationResultFromIntent(activityResult.data)
                handleAuthorizationResult(authorizationResponse)
            } else {
                Log.e("Authorization", "Authorization failed or was cancelled.")
            }
        }

        /* Init views */
        val signUpTextView = findViewById<TextView>(R.id.sign_up_request)
        signUpRequest(signUpTextView)

        val signIn = findViewById<LinearLayout>(R.id.sign_in_google)
        signIn.setOnClickListener {
            startInteractiveAuthorization()
        }
    }

    private fun startInteractiveAuthorization() {
        // Build the authorization request with necessary scopes
        val authorizationRequest = AuthorizationRequest.Builder()
            .requestOfflineAccess(getString(R.string.WEB_CLIENT_ID), true)
            .setRequestedScopes(
                listOf(
                    Scope("https://www.googleapis.com/auth/userinfo.email"),
                    Scope("https://www.googleapis.com/auth/userinfo.profile"),
                    Scope("openid")
                )
            )
            .build()

        // Start the authorization process
        Identity.getAuthorizationClient(this)
            .authorize(authorizationRequest)
            .addOnSuccessListener { authorizationResult ->
                if (authorizationResult.hasResolution()) {
                    authorizationResult.pendingIntent?.intentSender?.let { intentSender ->
                        val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                        authorizationLauncher.launch(intentSenderRequest)
                    }
                } else {
                    handleAuthorizationResult(authorizationResult)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Authorization", "Authorization failed: ${exception.message}")
                exception.printStackTrace()  // Print the stack trace to get more details
            }
    }

    private fun handleAuthorizationResult(authorizationResult: AuthorizationResult?) {
        authorizationResult?.let {
            val accessToken = it.accessToken
            if (accessToken != null) {
                Log.d("Authorization", "Access token retrieved: $accessToken")
                Toast.makeText(this, "Authorization Successful!", Toast.LENGTH_SHORT).show()
                // Use the access token as needed in your app

                // Make request to backend and retrieve user info
                getUser(accessToken) { userExists ->
                    if (userExists) { // User exists -> Move to home page
                        runOnUiThread {
                            Toast.makeText(this, "Welcome back to Diet Advisor!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HomePage::class.java)
                            intent.putExtra("ACCESS_TOKEN", accessToken) // TODO: TEMPORARY SOLUTION! Find a way to refresh token instead of passing around to multiple intents
                            startActivity(intent)
                        }
                    } else { // User doesn't exist -> Move to sign up screen
                        runOnUiThread {
                            Log.d(TAG, "handleAuthorizationResult: Creating User Now")
                            Toast.makeText(this, "Please create an account!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, SignUpwithOAuth::class.java)
                            intent.putExtra("ACCESS_TOKEN", accessToken)
                            startActivity(intent)
                        }
                    }
                }
            } else {
                Log.e("Authorization", "Failed to retrieve access token.")
                Toast.makeText(this, "Authorization Failure!", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Log.e("Authorization", "Authorization response is null.")
            Toast.makeText(this, "Authorization Response is null!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUser(accessToken: String, onResult: (Boolean) -> Unit) {
        val url = getString(R.string.DIET_ADVISOR_USER_ENDPOINT_URL)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .get() // GET request to retrieve user data
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onResult(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle the response
                    val responseBody = response.body?.string()
                    println(responseBody)
                    // Here, you might want to parse the JSON response to a UserData object using Gson
                    Log.d(TAG, "onResponse: Success! Retrieved user information!")
                    onResult(true)
                } else {
                    // Handle the error
                    println("Request failed: ${response.message}")
                    Log.d(TAG, "onResponse: Failure! No User to Retrieve!")
                    onResult(false)
                }
            }
        })
    }

    // TODO: Probably won't be used
    private fun saveAccountInfo(accountInfo: AccountInfo) {
        val masterKeyAlias = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedSharedPreferences = EncryptedSharedPreferences.create(
            this,
            "account_prefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val sharedPreferencesEditor = encryptedSharedPreferences.edit()
        sharedPreferencesEditor.putString("user_email", accountInfo.email)
        sharedPreferencesEditor.putString("user_id", accountInfo.userId)
        // Add more fields as needed
        sharedPreferencesEditor.apply() // Apply changes asynchronously
    }

    // TODO: Probably won't be used
    private fun getStoredAccountInfo(): AccountInfo? {
        val masterKeyAlias = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedSharedPreferences = EncryptedSharedPreferences.create(
            this,
            "account_prefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val userEmail = encryptedSharedPreferences.getString("user_email", null)
        val userId = encryptedSharedPreferences.getString("user_id", null)

        return if (userEmail != null && userId != null) {
            AccountInfo(email = userEmail, userId = userId)
        } else {
            null // Return null if account info is not available
        }
    }

    private fun signUpRequest(signUpTextView: TextView) {
        val text = "Not a member? Sign up now!"
        val spannableString = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
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

    private fun getHashedNonce(): String {
        // Create cryptographic nonce to increase protection against Auth attacks
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }
        return hashedNonce
    }

    /*
    TODO:
        Backend must be modified to allow for best practice normal and silent sign in methods
        (uses Google ID Token & Credential Manager)
        Until then, the current sign in flow is using the dialog window to confirm your account
    */
    private suspend fun performSignIn() {
        // Create local instance of credential manager
        val credentialManager = CredentialManager.create(this)


        // Step 1: Instantiate Google Sign-In Request
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(R.string.WEB_CLIENT_ID))
            .setAutoSelectEnabled(true)
            .setNonce(getHashedNonce())
            .build()

        // Step 2: Make API Request for Sign-In
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Step 3: Retrieve credentials from selected user account
        try {
            val result = credentialManager.getCredential(this, request) // Get credential response
            val credential = result.credential // Raw credential data from response (Needs parsing)
            val googleIdTokenCredential = GoogleIdTokenCredential  // Parse raw data & get JWT
                .createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken // Extract Google ID Token

            Log.d(TAG, "Google ID: Token: $googleIdToken")

            /*
            TODO:
                Send ID token to backend server for verification
                Exchange ID Token for access token (Requires Backend Accommodation)
                Sign-Up Documentation: https://developer.android.com/identity/sign-in/credential-manager-siwg#enable-sign-up
                Sign-In Documentation: https://developer.android.com/identity/sign-in/credential-manager-siwg#create-sign
                Backend Verification: https://developers.google.com/identity/gsi/web/guides/verify-google-id-token
            */

        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential error during sign-in/sign-up: ${e.message}", e)
            Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_SHORT).show()
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "ID Token parsing error: ${e.message}", e)
            Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_SHORT).show()
        }
    }
}