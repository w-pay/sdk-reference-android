package au.com.wpay.sdk.paymentsimulator

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.woolworths.village.sdk.*
import au.com.woolworths.village.sdk.model.*
import au.com.wpay.frames.*
import au.com.wpay.frames.dto.CardCaptureResponse
import au.com.wpay.frames.dto.ThreeDSError
import au.com.wpay.frames.dto.ValidateCardResponse
import au.com.wpay.frames.types.ActionType
import au.com.wpay.frames.types.FramesConfig
import au.com.wpay.frames.types.LogLevel
import au.com.wpay.sdk.paymentsimulator.model.*
import au.com.wpay.sdk.paymentsimulator.payment.*
import au.com.wpay.sdk.paymentsimulator.settings.WPaySettingsActions
import kotlinx.coroutines.*
import org.json.JSONArray
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

interface PaymentSimulatorActions {
    fun onError(error: Exception)
}

/*
 * When a Frames SDK action completes, we want to handle the result differently based on the command
 * that was being executed
 */
typealias FramesActionHandler = (String) -> Unit

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

    private var framesActionHandler: FramesActionHandler = ::onCaptureCard

    private var fraudPayload: FraudPayload? = null
    private var challengeResponses: List<ChallengeResponse> = mutableListOf()

    private var require3DSNPA: Boolean = false
    private var customerWallet: Wallet? = null
    private lateinit var windowSize: ActionType.AcsWindowSize

    /*
     * If we try to validate a card more than once, we should stop and fail.
     */
    private var validCardAttemptCounter = 0

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

        try {
            framesActionHandler(response)
        }
        catch (e: Exception) {
            onError(e)
        }
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

        framesCommand.postValue(cardCaptureCommand(CardCaptureOptions(
            wallet = customerWallet,
            require3DS = require3DSNPA
        )))
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

    private fun onCaptureCard(data: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val response = CardCaptureResponse.fromJson(data)
                val instrumentId =
                    when {
                        response.itemId != null -> {
                            response.itemId
                        }
                        else -> {
                            response.paymentInstrument?.itemId
                        }
                    }


                if (response.threeDSError == ThreeDSError.TOKEN_REQUIRED) {
                    validateCard(response.threeDSToken!!)
                }

                if (response.threeDSError == ThreeDSError.VALIDATION_FAILED) {
                    failPayment(Exception("Three DS Validation Failed"))
                }

                if (response.status?.responseText == "ACCEPTED") {
                    val cards = listPaymentInstruments()
                    val card = cards?.find { it.paymentInstrumentId == instrumentId }

                    makePayment(PaymentOptions.ExistingCard(card))
                }
            }
        }
    }

    private fun onValidateCard(data: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val response = ValidateCardResponse.fromJson(data)

                framesActionHandler = this@PaymentSimulatorModel::onCaptureCard

                framesCommand.postValue(GroupCommand("completeCardCapture",
                    CompleteActionCommand(CAPTURE_CARD_ACTION, JSONArray().apply {
                        response.challengeResponse?.let { put(it.toJson()) }
                    })
                ))
            }
        }
    }

    private fun completeCapturingCard() {
        validCardAttemptCounter = 0

        framesCommand.postValue(SubmitFormCommand(CAPTURE_CARD_ACTION))
    }

    private fun validateCard(threeDSToken: String) {
        if (validCardAttemptCounter > 1) {
            failPayment(Exception("Validate card attempt counter exceeded"))
        }
        else {
            validCardAttemptCounter++

            framesActionHandler = ::onValidateCard

            framesCommand.postValue(cardValidateCommand(threeDSToken, windowSize))
        }
    }

    private fun payWithCard(card: CreditCard) {
        for (retryCount in 1..3) {
            if (paymentOutcome.value is PaymentOutcomes.NoOutcome) {
                val result = customerSDK.paymentRequests.makePayment(
                    paymentRequestId = paymentRequest.value!!.paymentRequestId,
                    primaryInstrument = card.paymentInstrumentId,
                    secondaryInstruments = emptyList(),
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
                        failPayment(result.e)

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
        customerWallet = customer.wallet

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
        windowSize = merchant.windowSize
        require3DSNPA = merchant.require3DSNPA

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

    private fun listPaymentInstruments(): List<CreditCard>? =
        when (val result = customerSDK.instruments.list()) {
            is ApiResult.Error -> {
                onError(result.e)

                null
            }

            is ApiResult.Success -> {
                val cards = if (customerSDK.options.wallet == Wallet.EVERYDAY_PAY) {
                    result.value.everydayPay?.creditCards
                }
                else {
                    result.value.creditCards
                }

                paymentInstruments.postValue(cards)

                cards
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

    private fun failPayment(error: Exception) {
        paymentOutcome.postValue(PaymentOutcomes.Failure(error.message!!))

        Log.e("PaymentSimulator", "Payment error", error)
    }

    private fun newCardValid(): Boolean =
        cardNumberValid && cardExpiryValid && cardCvvValid

    private fun sdkBaseUrl(origin: String): String =
        "${origin}/wow/v1/pay"
}

private fun debug(message: String) {
    Log.d("[Callback]", message)
}