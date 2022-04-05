package au.com.wpay.sdk.paymentsimulator

import au.com.redcrew.apisdkcreator.httpclient.okhttp.okHttpClient
import au.com.wpay.sdk.*
import au.com.wpay.sdk.paymentsimulator.model.SimulatorCustomerOptions

fun createCustomerSDK(
    options: WPayCustomerOptions,
): WPayCustomerApi =
    createCustomerSDK(::okHttpClient, options)

fun createMerchantSDK(options: WPayMerchantOptions): WPayMerchantApi =
    createMerchantSDK(::okHttpClient, options)

fun createCustomerLoginAuthenticator(
    options: SimulatorCustomerOptions
): CustomerLoginApiAuthenticator =
    CustomerLoginApiAuthenticator(
        apiKey = options.apiKey,
        url = "${options.baseUrl}/wow/v1/idm/servers/token",
        customerId = options.customerId
    )