package au.com.wpay.sdk.paymentsimulator

import au.com.woolworths.village.sdk.*
import au.com.woolworths.village.sdk.openapi.OpenApiCustomerApiRepositoryFactory
import au.com.woolworths.village.sdk.openapi.OpenApiMerchantApiRepositoryFactory
import au.com.wpay.sdk.paymentsimulator.model.SimulatorCustomerOptions

fun createCustomerSDK(
    options: VillageCustomerOptions,
    token: String
): VillageCustomerApiRepository =
    createCustomerSDK(
        options,
        // see the docs on how we can use different token types.
        ApiTokenType.StringToken(token),
        OpenApiCustomerApiRepositoryFactory
    )

fun createMerchantSDK(
    options: VillageMerchantOptions,
    token: String
): VillageMerchantApiRepository =
    createMerchantSDK(
        options,
        // see the docs on how we can use different token types.
        ApiTokenType.StringToken(token),
        OpenApiMerchantApiRepositoryFactory
    )

fun createCustomerLoginAuthenticator(
    options: SimulatorCustomerOptions
): CustomerLoginApiAuthenticator =
    CustomerLoginApiAuthenticator(
        requestHeaders = RequestHeaderChain(listOf(ApiKeyRequestHeader(options))),
        url = "${options.baseUrl}/wow/v1/idm/servers/token",
        customerId = options.customerId
    )