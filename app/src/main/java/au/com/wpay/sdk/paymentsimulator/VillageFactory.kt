package au.com.wpay.sdk.paymentsimulator

import au.com.woolworths.village.sdk.*
import au.com.woolworths.village.sdk.auth.ApiAuthenticator
import au.com.woolworths.village.sdk.auth.HasAccessToken
import au.com.woolworths.village.sdk.openapi.*
import au.com.wpay.sdk.paymentsimulator.model.SimulatorCustomerOptions

val openApiCustomerApiRepositoryFactory: CustomerApiRepositoryFactory =
    fun(options: VillageCustomerOptions,
        headers: RequestHeadersFactory,
        authenticator: ApiAuthenticator<HasAccessToken>
    ): VillageCustomerApiRepository {
        return OpenApiVillageCustomerApiRepository(headers, options, authenticator, ClientOptions(
            debug = true
        ))
    }

val openApiMerchantApiRepositoryFactory: MerchantApiRepositoryFactory =
    fun(options: VillageMerchantOptions,
        headers: RequestHeadersFactory,
        authenticator: ApiAuthenticator<HasAccessToken>
    ): VillageMerchantApiRepository {
        return OpenApiVillageMerchantApiRepository(headers, options, authenticator, ClientOptions(
            debug = true
        ))
    }

fun createCustomerSDK(
    options: VillageCustomerOptions,
    token: String
): VillageCustomerApiRepository =
    createCustomerSDK(
        options,
        // see the docs on how we can use different token types.
        ApiTokenType.StringToken(token),
        openApiCustomerApiRepositoryFactory
    )

fun createMerchantSDK(
    options: VillageMerchantOptions,
    token: String
): VillageMerchantApiRepository =
    createMerchantSDK(
        options,
        // see the docs on how we can use different token types.
        ApiTokenType.StringToken(token),
        openApiMerchantApiRepositoryFactory
    )

fun createCustomerLoginAuthenticator(
    options: SimulatorCustomerOptions
): CustomerLoginApiAuthenticator =
    CustomerLoginApiAuthenticator(
        requestHeaders = RequestHeaderChain(listOf(ApiKeyRequestHeader(options))),
        url = "${options.baseUrl}/wow/v1/idm/servers/token",
        customerId = options.customerId
    )