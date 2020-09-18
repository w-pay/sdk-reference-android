package au.com.woolworths.village.sdk.app

import au.com.woolworths.village.sdk.auth.HasAccessToken

class IdmTokenDetails(
    override val accessToken: String,
    val accessTokenExpiresIn: Int,
    val refreshToken: String,
    val refreshTokenExpiresIn: Int,
    val tokensIssuedAt: Long,
    val isGuestToken: Boolean,
    val idmStatusOK: Boolean
): HasAccessToken