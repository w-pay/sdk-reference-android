package au.com.wpay.sdk.paymentsimulator

import android.util.Log
import au.com.wpay.sdk.ApiResult
import au.com.wpay.sdk.HttpFailureError
import au.com.wpay.sdk.auth.ApiAuthenticator
import au.com.wpay.sdk.headers.X_API_KEY
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.IllegalStateException

class CustomerLoginApiAuthenticator(
    private var url: String,
    private var apiKey: String,
    private var customerId: String
): ApiAuthenticator<IdmTokenDetails> {
    override fun authenticate(): ApiResult<IdmTokenDetails> {
        val gson: Gson = GsonBuilder().create()
        val credentials: String = gson.toJson(mapOf(
            "shopperId" to customerId,
            "username" to customerId
        ))

        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
        val client = builder.build()
        val req: Request = Request.Builder()
            .url(url)
            .apply { addHeader(X_API_KEY, apiKey)  }
            .post(credentials.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(req).execute().use { response ->
            val body = response.body?.string() ?: throw IllegalStateException("No response body")
            Log.d("Authentication", "customerLogin: body = '${body}'")

            if (response.isSuccessful) {
                val result = gson.fromJson<IdmTokenDetails>(body, IdmTokenDetails::class.java)

                return ApiResult.Success(result)
            }

            return ApiResult.Error(HttpFailureError(
                response.code,
                response.headers.toMultimap().mapValues { it.value.joinToString(";") },
                body
            ))
        }
    }
}