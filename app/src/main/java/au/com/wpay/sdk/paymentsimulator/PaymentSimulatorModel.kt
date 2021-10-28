package au.com.wpay.sdk.paymentsimulator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import au.com.woolworths.village.sdk.VillageCustomerOptions
import au.com.woolworths.village.sdk.VillageMerchantOptions
import au.com.woolworths.village.sdk.Wallet
import java.lang.Exception

interface PaymentSimulatorActions {
    fun onError(error: Exception)
}

class SimulatorCustomerOptions(
    apiKey: String,
    baseUrl: String,
    wallet: Wallet? = null,
    walletId: String? = null,

    val customerId: String
) : VillageCustomerOptions(apiKey, baseUrl, wallet, walletId)

class SimulatorMerchantOptions(
    apiKey: String,
    baseUrl: String,
    wallet: Wallet? = null,
    merchantId: String? = null
) : VillageMerchantOptions(apiKey, baseUrl, wallet, merchantId)

class PaymentSimulatorModel : ViewModel() {
    val error: MutableLiveData<Exception> = MutableLiveData()

    fun onError(error: Exception) {
        this.error.postValue(error)
    }

    fun createPaymentRequest(

    ) {

    }
}