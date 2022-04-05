package au.com.wpay.sdk.paymentsimulator.payment

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import au.com.wpay.sdk.model.CreditCard
import au.com.wpay.sdk.model.CreditCardStepUp
import au.com.wpay.sdk.model.PaymentInstrument
import au.com.wpay.frames.FramesError
import au.com.wpay.frames.FramesView
import au.com.wpay.frames.JavascriptCommand
import au.com.wpay.frames.types.FramesConfig
import au.com.wpay.frames.types.LogLevel
import au.com.wpay.sdk.paymentsimulator.model.PaymentOptions
import au.com.wpay.sdk.paymentsimulator.model.PaymentOutcomes
import org.threeten.bp.OffsetDateTime

data class PaymentDetailsFramesConfig(
    val config: FramesConfig,
    val callback: FramesView.Callback,
    val command: State<JavascriptCommand?>,
    val message: State<String>
)

data class PaymentDetailsProps(
    val framesConfig: PaymentDetailsFramesConfig,
    val cards: State<List<CreditCard>?>,
    val selectedPaymentOption: State<PaymentOptions>,
    val paymentOutcome: State<PaymentOutcomes>
)

@Suppress("DeferredIsResult")
interface PaymentDetailsActions {
    fun selectNewCardPaymentOption()

    fun selectExistingCardPaymentOption(card: CreditCard)

    suspend fun makePayment(paymentOption: PaymentOptions)

    suspend fun deleteCard(card: CreditCard)
}

fun fakeCreditCards(): List<CreditCard> =
    listOf(fakeCreditCard, fakeCreditCard, fakeCreditCard)

fun fakeFramesConfig(): PaymentDetailsFramesConfig =
    PaymentDetailsFramesConfig(
        config = FramesConfig(
            apiBase = "http://localhost",
            apiKey = "abc123",
            authToken = "Bearer abc123",
            logLevel = LogLevel.DEBUG
        ),
        callback = fakeCallback,
        command = mutableStateOf(null),
        message = mutableStateOf("")
    )

val fakeCreditCard = CreditCard(
    allowed = true,
    cardName = "Bruce Wayne",
    cardSuffix = "1234",
    cvvValidated = true,
    expired = false,
    expiryMonth = "11",
    expiryYear = "2025",
    lastUpdated = OffsetDateTime.now(),
    lastUsed = null,
    paymentInstrumentId = "abc123",
    paymentToken = "token",
    primary = true,
    requiresCVV = false,
    scheme = "VISA",
    status = PaymentInstrument.InstrumentStatus.VERIFIED,
    updateURL = "http://foobar.com",
    stepUp = CreditCardStepUp(
        mandatory = false,
        type = "",
        url = "http://foobar.com"
    )
)

var fakeCallback = object : FramesView.Callback {
    override fun onComplete(response: String) {

    }

    override fun onError(error: FramesError) {

    }

    override fun onFocusChange(domId: String, isFocussed: Boolean) {

    }

    override fun onPageLoaded() {

    }

    override fun onProgressChanged(progress: Int) {

    }

    override fun onRendered(id: String) {

    }

    override fun onRemoved(id: String) {

    }

    override fun onValidationChange(domId: String, isValid: Boolean) {

    }

    override fun onFormValidationChange(isValid: Boolean) {

    }
}