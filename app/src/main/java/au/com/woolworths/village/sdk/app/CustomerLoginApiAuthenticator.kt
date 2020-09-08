package au.com.woolworths.village.sdk.app

import android.util.Log
import au.com.woolworths.village.sdk.ApiResult
import au.com.woolworths.village.sdk.HttpErrorException
import au.com.woolworths.village.sdk.RequestHeadersFactory
import au.com.woolworths.village.sdk.auth.ApiAuthenticator
import au.com.woolworths.village.sdk.auth.IdmTokenDetails
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.IllegalStateException

class CustomerLoginApiAuthenticator(
    private val requestHeaders: RequestHeadersFactory,
    private var path: String
): ApiAuthenticator<IdmTokenDetails> {
    private var origin: String? = null

    override fun authenticate(): ApiResult<IdmTokenDetails> {
        val origin = this.origin ?: throw IllegalStateException("Origin server must be set")
        val gson: Gson = GsonBuilder().create()
        val credentials: String = gson.toJson(mapOf(
            "shopperId" to "1100000000093126352",
            "username" to "1100000000093126352"
        ))

        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
        val client = builder.build()
        val req: Request = Request.Builder()
            .url("${origin}${path}")
            .apply { requestHeaders.createHeaders().forEach { (name, value) -> addHeader(name, value) } }
            .post(credentials.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(req).execute().use { response ->
            val body = response.body?.string() ?: throw IllegalStateException("No response body")
            Log.d("Authentication", "customerLogin: body = '${body}'")

            if (response.isSuccessful) {
                val result = gson.fromJson<IdmTokenDetails>(body, IdmTokenDetails::class.java)

                return ApiResult.Success(result)
            }

            return ApiResult.Error(HttpErrorException(response.code, response.headers.toMultimap(), body))
        }
    }

    override fun setHost(host: String) {
       origin = host
    }
}