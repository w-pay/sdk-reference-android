package au.com.woolworths.village.sdk.app

import au.com.woolworths.village.sdk.*
import au.com.woolworths.village.sdk.auth.ApiAuthenticator
import au.com.woolworths.village.sdk.auth.HasAccessToken
import au.com.woolworths.village.sdk.openapi.OpenApiCustomerApiRepositoryFactory

fun createCustomerSDK(
    options: VillageCustomerOptions,
    authenticator: ApiAuthenticator<HasAccessToken>
): VillageCustomerApiRepository =
    createCustomerSDK(
        options,
        // see the docs on how we can use different token types.
        ApiTokenType.ApiAuthenticatorToken(authenticator),
        OpenApiCustomerApiRepositoryFactory
    )

fun createCustomerLoginAuthenticator(
    options: VillageOptions,
    origin: String
): CustomerLoginApiAuthenticator {
    val authenticator = CustomerLoginApiAuthenticator(
        requestHeaders = RequestHeaderChain(listOf(ApiKeyRequestHeader(options))),
        path = "/wow/v1/idm/servers/token"
    )

    authenticator.setOrigin(origin)

    return authenticator
}