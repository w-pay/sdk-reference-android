package au.com.wpay.sdk.paymentsimulator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.woolworths.village.sdk.*
import au.com.woolworths.village.sdk.model.NewPaymentRequest
import au.com.wpay.sdk.paymentsimulator.settings.WPaySettingsActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

class PaymentSimulatorModel : ViewModel(), WPaySettingsActions {
    lateinit var customerSDK: VillageCustomerApiRepository
    lateinit var merchantSDK: VillageMerchantApiRepository

    val error: MutableLiveData<Exception> = MutableLiveData()

    override fun onError(error: Exception) {
        this.error.postValue(error)
    }

    override fun onCreatePaymentRequest(
        merchant: SimulatorMerchantOptions,
        customer: SimulatorCustomerOptions,
        paymentRequest: NewPaymentRequest
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val authToken = authenticateCustomer(customer)

                authToken?.let {
                    val options = VillageCustomerOptions(
                        apiKey = customer.apiKey,
                        baseUrl = sdkBaseUrl(customer.baseUrl),
                        wallet = customer.wallet,
                        walletId = customer.walletId
                    )

                    customerSDK = createCustomerSDK(
                        options = options,
                        token = it
                    )
                }

                authToken?.let {
                    val options = VillageMerchantOptions(
                        apiKey = customer.apiKey,
                        baseUrl = sdkBaseUrl(merchant.baseUrl),
                        wallet = customer.wallet,
                    )

                    merchantSDK = createMerchantSDK(
                        options = options
                    )
                }
            }
        }
    }

    private fun authenticateCustomer(customer: SimulatorCustomerOptions): String? {
        val authenticator = createCustomerLoginAuthenticator(customer)

        return when (val result = authenticator.authenticate()) {
            is ApiResult.Success -> result.value.accessToken
            is ApiResult.Error -> {
                onError(result.e)

                null
            }
        }
    }

    private fun sdkBaseUrl(origin: String): String =
        "${origin}/wow/v1/pay"
}