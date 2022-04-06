package au.com.wpay.sdk.paymentsimulator

import au.com.wpay.sdk.auth.HasAccessToken

class IdmTokenDetails(
    override val accessToken: String,
    val accessTokenExpiresIn: Int,
    val refreshToken: String,
    val refreshTokenExpiresIn: Int,
    val tokensIssuedAt: Long,
    val isGuestToken: Boolean,
    val idmStatusOK: Boolean
): HasAccessToken