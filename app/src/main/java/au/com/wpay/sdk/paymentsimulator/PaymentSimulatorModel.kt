package au.com.wpay.sdk.paymentsimulator

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.woolworths.village.sdk.*
import au.com.woolworths.village.sdk.model.*
import au.com.wpay.frames.FramesError
import au.com.wpay.frames.FramesView
import au.com.wpay.frames.JavascriptCommand
import au.com.wpay.frames.types.FramesConfig
import au.com.wpay.frames.types.LogLevel
import au.com.wpay.sdk.paymentsimulator.model.*
import au.com.wpay.sdk.paymentsimulator.payment.*
import au.com.wpay.sdk.paymentsimulator.settings.WPaySettingsActions
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

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
    val paymentOutcome: MutableLiveData<PaymentOutcomes> = MutableLiveData(PaymentOutcomes.NoOutcome)

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
    private var challengeResponses: List<ChallengeResponse> = mutableListOf()

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

    override suspend fun makePayment(paymentOption: PaymentOptions) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (paymentOption) {
                    is PaymentOptions.NewCard -> completeCapturingCard()

                    is PaymentOptions.ExistingCard ->
                        paymentOption.card?.let { payWithCard(it) }
                            ?: run { throw IllegalArgumentException("Missing card") }

                    else -> { throw IllegalStateException("Can't pay with nothing") }
                }
            }
        }
    }

    override suspend fun deleteCard(card: CreditCard) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val result = customerSDK.instruments.delete(card.paymentInstrumentId)) {
                    is ApiResult.Error -> onError(result.e)
                    is ApiResult.Success -> listPaymentInstruments()
                }
            }
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

    private fun completeCapturingCard() {

    }

    private fun payWithCard(card: CreditCard) {
        for (retryCount in 1..3) {
            if (paymentOutcome.value is PaymentOutcomes.NoOutcome) {
                val result = customerSDK.paymentRequests.makePayment(
                    paymentRequestId = paymentRequest.value!!.paymentRequestId,
                    primaryInstrument = card.paymentInstrumentId,
                    secondaryInstruments = null,
                    clientReference = null,
                    preferences = null,
                    challengeResponses = challengeResponses,
                    fraudPayload = fraudPayload,
                    transactionType = null,
                    allowPartialSuccess = null
                )

                when(result) {
                    is ApiResult.Success -> {
                        // TODO: Check for 3DS response

                        paymentOutcome.postValue(PaymentOutcomes.Success)

                        break
                    }

                    is ApiResult.Error -> {
                        paymentOutcome.postValue(PaymentOutcomes.Failure(result.e.message))

                        Log.e("PaymentSimulator", "Payment error", result.e)

                        break
                    }
                }
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