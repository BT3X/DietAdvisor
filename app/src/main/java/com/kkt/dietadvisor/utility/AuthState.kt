package com.kkt.dietadvisor.utility

import android.content.Context
import net.openid.appauth.AuthState

object AuthStateUtil {
    private const val AUTH_STATE_KEY = "auth_state"

    fun writeAuthState(context: Context, authState: AuthState) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(AUTH_STATE_KEY, authState.jsonSerializeString()).apply()
    }

    fun readAuthState(context: Context): AuthState {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val stateJson = sharedPrefs.getString(AUTH_STATE_KEY, null)
        return if (stateJson != null) {
            AuthState.jsonDeserialize(stateJson)
        } else {
            AuthState()  // Return a new AuthState if none exists
        }
    }
}