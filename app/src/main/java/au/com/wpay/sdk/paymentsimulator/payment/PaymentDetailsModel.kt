package au.com.wpay.sdk.paymentsimulator.payment

import au.com.woolworths.village.sdk.model.CreditCard
import au.com.woolworths.village.sdk.model.CreditCardStepUp
import au.com.woolworths.village.sdk.model.PaymentInstrument
import au.com.wpay.frames.types.FramesConfig
import au.com.wpay.frames.types.LogLevel
import au.com.wpay.sdk.paymentsimulator.PaymentOptions
import kotlinx.coroutines.Deferred
import org.threeten.bp.OffsetDateTime
import java.net.URL

data class PaymentDetailsProps(
    val cards: List<CreditCard>,
    val framesConfig: FramesConfig
)

@Suppress("DeferredIsResult")
interface PaymentDetailsActions {
    fun makePayment(paymentOption: PaymentOptions): Deferred<Unit>
}

val fakeCreditCard = object : CreditCard {
    override val allowed: Boolean
        get() = true

    override val cardName: String
        get() = "Bruce Wayne"

    override val cardSuffix: String
        get() = "1234"

    override val cvvValidated: Boolean
        get() = true

    override val expired: Boolean
        get() = false

    override val expiryMonth: String
        get() = "11"

    override val expiryYear: String
        get() = "2025"

    override val lastUpdated: OffsetDateTime
        get() = OffsetDateTime.now()

    override val lastUsed: OffsetDateTime?
        get() = null

    override val paymentInstrumentId: String
        get() = "abc123"

    override val paymentToken: String
        get() = "token"

    override val primary: Boolean
        get() = true

    override val requiresCVV: Boolean
        get() = false

    override val scheme: String
        get() = "VISA"

    override val status: PaymentInstrument.InstrumentStatus
        get() = PaymentInstrument.InstrumentStatus.VERIFIED

    override val stepUp: CreditCardStepUp
        get() = object : CreditCardStepUp {
            override val mandatory: Boolean
                get() = false

            override val type: String
                get() = ""

            override val url: URL
                get() = URL("http://foobar.com")

        }

    override val updateURL: URL
        get() = URL("http://foobar.com")
}

fun fakeCreditCards(): List<CreditCard> =
    listOf(fakeCreditCard, fakeCreditCard, fakeCreditCard)

fun fakeFramesConfig(): FramesConfig =
    FramesConfig(
        apiBase = "http://localhost",
        apiKey = "abc123",
        authToken = "Bearer abc123",
        logLevel = LogLevel.DEBUG
    )