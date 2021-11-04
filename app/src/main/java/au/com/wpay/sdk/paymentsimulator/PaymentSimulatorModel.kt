package au.com.wpay.sdk.paymentsimulator

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.woolworths.village.sdk.*
import au.com.woolworths.village.sdk.model.CreditCard
import au.com.woolworths.village.sdk.model.MerchantPaymentDetails
import au.com.woolworths.village.sdk.model.NewPaymentRequest
import au.com.wpay.frames.FramesError
import au.com.wpay.frames.FramesView
import au.com.wpay.frames.JavascriptCommand
import au.com.wpay.frames.types.FramesConfig
import au.com.wpay.frames.types.LogLevel
import au.com.wpay.sdk.paymentsimulator.model.PaymentOptions
import au.com.wpay.sdk.paymentsimulator.model.SimulatorCustomerOptions
import au.com.wpay.sdk.paymentsimulator.model.SimulatorMerchantOptions
import au.com.wpay.sdk.paymentsimulator.payment.*
import au.com.wpay.sdk.paymentsimulator.settings.WPaySettingsActions
import kotlinx.coroutines.*

interface PaymentSimulatorActions {
    fun onError(error: Exception)
}

@Suppress("DeferredIsResult")
class PaymentSimulatorModel : ViewModel(), FramesView.Callback, PaymentDetailsActions, WPaySettingsActions {
    lateinit var customerSDK: VillageCustomerApiRepository
    lateinit var merchantSDK: VillageMerchantApiRepository
    lateinit var framesConfig: FramesConfig

    val error: MutableLiveData<Exception> = MutableLiveData()
    val paymentRequest: MutableLiveData<MerchantPaymentDetails> = MutableLiveData()
    val paymentInstruments: MutableLiveData<List<CreditCard>> = MutableLiveData()
    val paymentOption: MutableLiveData<PaymentOptions> = MutableLiveData(PaymentOptions.NoOption)

    /*
     * The command that needs to be executed in the Frames SDK
     */
    val framesCommand: MutableLiveData<JavascriptCommand?> = MutableLiveData(null)

    /*
     * If messages are emitted from the Frames SDK we need to capture them
     */
    val framesMessage: MutableLiveData<String> = MutableLiveData("")

    /*
     * Because the Frames SDK only emits validation changes we need to record them in case the
     * UI is recomposed
     */
    private var cardNumberValid: Boolean = false
    private var cardExpiryValid: Boolean = false
    private var cardCvvValid: Boolean = false

    private var fraudPayload: FraudPayload? = null

    override fun onError(error: Exception) {
        this.error.postValue(error)
    }

    override fun onCreatePaymentRequest(
        merchant: SimulatorMerchantOptions,
        customer: SimulatorCustomerOptions,
        paymentRequest: SimulatorPaymentRequest
    ): Deferred<Unit> {
        return viewModelScope.async {
            withContext(Dispatchers.IO) {
                val authToken = authenticateCustomer(customer)

                createCustomerSDK(customer, authToken)
                createMerchantSDK(merchant, customer, authToken)
                createFramesConfig(customer, authToken)

                createPaymentRequest(paymentRequest)
                listPaymentInstruments()

                fraudPayload = paymentRequest.fraudPayload
            }
        }
    }

    override fun selectNewCardPaymentOption() {
        paymentOption.postValue(PaymentOptions.NewCard(newCardValid()))
    }

    override fun selectExistingCardPaymentOption(card: CreditCard) {
        paymentOption.postValue(PaymentOptions.ExistingCard(
            card = card
        ))
    }

    override fun makePayment(paymentOption: PaymentOptions): Deferred<Unit> {
        return viewModelScope.async {
            this@PaymentSimulatorModel.paymentOption.postValue(paymentOption)
        }
    }

    override fun onComplete(response: String) {
        debug("onComplete(response: $response)")
    }

    override fun onError(error: FramesError) {
        debug("onError(error: $error)")

        framesMessage.postValue(error.errorMessage.toString())
    }

    override fun onFocusChange(domId: String, isFocussed: Boolean) {
        debug("onFocusChange($domId, isFocussed: $isFocussed)")
    }

    override fun onPageLoaded() {
        debug("onPageLoaded()")

        framesCommand.postValue(cardCaptureCommand())
    }

    override fun onProgressChanged(progress: Int) {
        debug("onProgressChanged(progress: $progress)")
    }

    override fun onRendered() {
        debug("onRendered()")
    }

    override fun onValidationChange(domId: String, isValid: Boolean) {
        debug("onValidationChange($domId, isValid: $isValid)")

        when(domId) {
            CARD_NO_DOM_ID -> cardNumberValid = isValid
            CARD_EXPIRY_DOM_ID -> cardExpiryValid = isValid
            CARD_CVV_DOM_ID -> cardCvvValid = isValid
        }

        /*
         * If the user has already selected to use a new card to pay,
         * as they enter data into the card elements we need to keep
         * the option updated with whether the card is valid or not.
         */
        if (paymentOption.value is PaymentOptions.NewCard) {
            selectNewCardPaymentOption()
        }

        if (newCardValid()) {
            framesMessage.postValue("")
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

    private fun listPaymentInstruments() =
        when (val result = customerSDK.instruments.list()) {
            is ApiResult.Error -> onError(result.e)
            is ApiResult.Success -> {
                if (customerSDK.options.wallet == Wallet.EVERYDAY_PAY) {
                    paymentInstruments.postValue(result.value.everydayPay?.creditCards)
                }
                else {
                    paymentInstruments.postValue(result.value.creditCards)
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

    private fun newCardValid(): Boolean =
        cardNumberValid && cardExpiryValid && cardCvvValid

    private fun sdkBaseUrl(origin: String): String =
        "${origin}/wow/v1/pay"
}

private fun debug(message: String) {
    Log.d("[Callback]", message)
}