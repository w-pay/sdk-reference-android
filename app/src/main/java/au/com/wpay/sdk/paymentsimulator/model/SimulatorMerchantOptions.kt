package au.com.wpay.sdk.paymentsimulator.model

import au.com.woolworths.village.sdk.VillageMerchantOptions
import au.com.woolworths.village.sdk.Wallet
import au.com.wpay.frames.types.ActionType

class SimulatorMerchantOptions(
    apiKey: String,
    baseUrl: String,
    wallet: Wallet? = null,
    merchantId: String? = null,

    val windowSize: ActionType.AcsWindowSize
) : VillageMerchantOptions(apiKey, baseUrl, wallet, merchantId)