package com.kkt.dietadvisor.utility

import android.content.Context
import android.util.Log
import com.kkt.dietadvisor.AnalysisResult
import com.kkt.dietadvisor.LoginActivity.Companion.TAG
import com.kkt.dietadvisor.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object UserInfoUtil {
    fun getUserInfo(
        context: Context,
        accessToken: String,
        requestClient: OkHttpClient,
        onResult: (Boolean, String?) -> Unit,
    ) {
        val url = context.getString(R.string.DIET_ADVISOR_USER_ENDPOINT_URL)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .get() // GET request to retrieve user data
            .build()

        requestClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onResult(false, null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle the response
                    val responseBody = response.body?.string()
                    println(responseBody)
                    // Here, you might want to parse the JSON response to a UserData object using Gson
                    Log.d(AnalysisResult.TAG, "onResponse: Success! Retrieved user information!")
                    onResult(true, responseBody)
                } else {
                    // Handle the error
                    println("Request failed: ${response.message}")
                    Log.d(AnalysisResult.TAG, "onResponse: Failure! No User to Retrieve!")
                    onResult(false, null)
                }
            }
        })
    }
}