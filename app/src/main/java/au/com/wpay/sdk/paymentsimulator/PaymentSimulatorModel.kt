package au.com.wpay.sdk.paymentsimulator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.woolworths.village.sdk.*
import au.com.woolworths.village.sdk.model.MerchantPaymentDetails
import au.com.woolworths.village.sdk.model.NewPaymentRequest
import au.com.wpay.frames.types.FramesConfig
import au.com.wpay.frames.types.LogLevel
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
    lateinit var framesConfig: FramesConfig

    val error: MutableLiveData<Exception> = MutableLiveData()
    val paymentRequest: MutableLiveData<MerchantPaymentDetails> = MutableLiveData()

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

                createCustomerSDK(customer, authToken)
                createMerchantSDK(merchant, customer, authToken)
                createFramesConfig(customer, authToken)

                createPaymentRequest(paymentRequest)
            }
        }
    }

    private fun createCustomerSDK(
        customer: SimulatorCustomerOptions,
        authToken: String?
    ) {
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
    }

    private fun createMerchantSDK(
        merchant: SimulatorMerchantOptions,
        customer: SimulatorCustomerOptions,
        authToken: String?
    ) {
        authToken?.let {
            val options = VillageMerchantOptions(
                apiKey = customer.apiKey,
                baseUrl = sdkBaseUrl(merchant.baseUrl),
                wallet = customer.wallet,
            )

            merchantSDK = createMerchantSDK(
                options = options,
                token = it
            )
        }
    }

    private fun createFramesConfig(customer: SimulatorCustomerOptions, authToken: String?) =
        authToken?.let {
            framesConfig = FramesConfig(
                apiKey = customer.apiKey,
                authToken = "Bearer $authToken",
                apiBase = "${sdkBaseUrl(customer.baseUrl)}/instore",
                logLevel = LogLevel.DEBUG
            )
        }

    private fun createPaymentRequest(paymentRequest: NewPaymentRequest) =
        when (val result = merchantSDK.payments.createPaymentRequest(paymentRequest)) {
            is ApiResult.Success -> getPaymentRequest(result.value.paymentRequestId)
            is ApiResult.Error -> onError(result.e)
        }

    private fun getPaymentRequest(paymentRequestId: String) =
        when (val result = merchantSDK.payments.getPaymentRequestDetailsBy(paymentRequestId)) {
            is ApiResult.Success -> paymentRequest.postValue(result.value)
            is ApiResult.Error -> onError(result.e)
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