package com.kkt.dietadvisor.utility

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kkt.dietadvisor.R
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.TokenRequest

object TokenStateUtil {
    private const val TAG = "AuthUtils"

    // Save refresh token to encrypted shared preferences
    fun saveRefreshToken(context: Context, refreshToken: String?) {
        refreshToken?.let {
            val masterKeyAlias = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                context.getString(R.string.token_preferences_file),
                masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            sharedPreferences.edit().putString(context.getString(R.string.refresh_token_key), refreshToken).apply()
        }
    }

    // Retrieve refresh token from encrypted shared preferences
    fun getRefreshToken(context: Context): String? {
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            context.getString(R.string.token_preferences_file),
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString(context.getString(R.string.refresh_token_key), null)
    }

    // Check if the access token needs to be renewed, and renew if necessary
    fun checkAndRenewAccessToken(
        context: Context,
        authState: AuthState,
        authService: AuthorizationService,
        onTokenRenewed: (Boolean) -> Unit
    ) {
        if (authState.needsTokenRefresh) {
            renewAccessToken(context, authState, authService, onTokenRenewed)
        } else {
            onTokenRenewed(false) // No need to refresh
        }
    }

    // Renew access token using refresh token
    private fun renewAccessToken(
        context: Context,
        authState: AuthState,
        authService: AuthorizationService,
        onTokenRenewed: (Boolean) -> Unit
    ) {

        val refreshToken = authState.refreshToken
        refreshToken?.let {
            // Ensure the authState has a valid service configuration
            val serviceConfig = authState.authorizationServiceConfiguration
                ?: AuthorizationServiceConfiguration(
                    Uri.parse(context.getString(R.string.GOOGLE_AUTHORIZATION_ENDPOINT)),  // Auth endpoint
                    Uri.parse(context.getString(R.string.GOOGLE_TOKEN_ENDPOINT))           // Token endpoint
            )

            // Create refresh token request using the service config file
            val tokenRequest = TokenRequest.Builder(
                serviceConfig,
                context.getString(R.string.ANDROID_CLIENT_ID)
            )
                .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                .setRefreshToken(refreshToken)
                .build()

            authService.performTokenRequest(tokenRequest) { tokenResponse, authException ->
                tokenResponse?.let {
                    Log.i(TAG, "New Access Token: ${tokenResponse.accessToken}")
                    Log.i(TAG, "New Refresh Token: ${tokenResponse.refreshToken}")

                    // Update auth state and save tokens
                    authState.update(tokenResponse, authException)
                    AuthStateUtil.writeAuthState(context, authState)

                    // Save the new refresh token if available
                    saveRefreshToken(context, tokenResponse.refreshToken)

                    onTokenRenewed(true) // Token was refreshed
                } ?: run {
                    Log.e(TAG, "Token renewal failed: ${authException?.message}")
                    onTokenRenewed(false) // Token renewal failed
                }
            }
        } ?: run {
            Log.e(TAG, "No refresh token available")
            onTokenRenewed(false) // No refresh token available
        }
    }

    fun signOut(context: Context) {
        // Clear the saved AuthState
        AuthStateUtil.writeAuthState(context, AuthState()) // Writes an empty AuthState, effectively resetting it

        // Clear any saved tokens (access token, refresh token)
        clearSavedTokens(context)

        Log.i(TAG, "User signed out and tokens cleared.")
    }

    private fun clearSavedTokens(context: Context) {
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            context.getString(R.string.token_preferences_file),
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Clear all stored preferences (including access token, refresh token)
        sharedPreferences.edit().clear().apply()
    }
}