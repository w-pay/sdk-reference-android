package au.com.wpay.sdk.paymentsimulator.model

import au.com.woolworths.village.sdk.VillageMerchantOptions
import au.com.woolworths.village.sdk.Wallet

class SimulatorMerchantOptions(
    apiKey: String,
    baseUrl: String,
    wallet: Wallet? = null,
    merchantId: String? = null
) : VillageMerchantOptions(apiKey, baseUrl, wallet, merchantId)