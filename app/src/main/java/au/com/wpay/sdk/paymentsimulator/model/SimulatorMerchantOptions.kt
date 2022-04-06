package au.com.wpay.sdk.paymentsimulator.model

import au.com.wpay.frames.types.ActionType
import au.com.wpay.sdk.WPayMerchantOptions
import au.com.wpay.sdk.Wallet

data class SimulatorMerchantOptions(
    val apiKey: String,
    val baseUrl: String,
    val wallet: Wallet? = null,
    val merchantId: String? = null,

    val require3DSNPA: Boolean,
    val windowSize: ActionType.AcsWindowSize
)