package au.com.wpay.sdk.paymentsimulator.model

import au.com.woolworths.village.sdk.VillageCustomerOptions
import au.com.woolworths.village.sdk.Wallet

class SimulatorCustomerOptions(
    apiKey: String,
    baseUrl: String,
    wallet: Wallet? = null,
    walletId: String? = null,

    val customerId: String
) : VillageCustomerOptions(apiKey, baseUrl, wallet, walletId)