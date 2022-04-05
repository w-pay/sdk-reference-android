package au.com.wpay.sdk.paymentsimulator.model

import au.com.wpay.sdk.Wallet

data class SimulatorCustomerOptions(
    val apiKey: String,
    val baseUrl: String,
    val wallet: Wallet? = null,
    val walletId: String? = null,
    val customerId: String
)