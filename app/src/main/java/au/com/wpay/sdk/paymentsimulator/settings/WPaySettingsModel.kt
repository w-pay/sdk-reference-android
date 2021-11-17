package au.com.wpay.sdk.paymentsimulator.settings

import au.com.woolworths.village.sdk.model.FraudPayload
import au.com.woolworths.village.sdk.model.FraudPayloadFormat
import au.com.wpay.frames.types.ActionType
import au.com.wpay.sdk.paymentsimulator.PaymentSimulatorActions
import au.com.wpay.sdk.paymentsimulator.model.SimulatorCustomerOptions
import au.com.wpay.sdk.paymentsimulator.model.SimulatorMerchantOptions
import au.com.wpay.sdk.paymentsimulator.model.SimulatorPaymentRequest
import kotlinx.coroutines.Deferred

data class WPaySettingsProps(
    val merchant: WPayMerchantSettings,
    val customer: WPayCustomerSettings,
    val paymentRequest: InitialPaymentRequest
)

data class WPayMerchantSettings(
    val merchantId: String,
    val apiKey: String,
    val require3DSNPA: Boolean,
    val require3DSPA: Boolean,
    val threeDSWindowSizes: List<ThreeDSWindowSizes>
)

data class WPayCustomerSettings(
    val userId: String,
    val walletId: String,
    val apiKey: String,
    val useEveryDayPay: Boolean
)

data class InitialPaymentRequest(
    val amount: String,
    val maxUses: Int,
    val fraud: Boolean,
    val fraudPayload: FraudPayload
)

data class ThreeDSWindowSizes(
    val size: ActionType.AcsWindowSize,
    val displaySize: String
)

@Suppress("DeferredIsResult")
interface WPaySettingsActions : PaymentSimulatorActions {
    fun onCreatePaymentRequest(
        merchant: SimulatorMerchantOptions,
        customer: SimulatorCustomerOptions,
        paymentRequest: SimulatorPaymentRequest
    ): Deferred<Unit>
}

fun defaultSettingsProps() =
    WPaySettingsProps(
        merchant = WPayMerchantSettings(
            merchantId = "aMerchant",
            apiKey = "dfdafasfdasfadfads",
            require3DSNPA = false,
            require3DSPA = false,
            threeDSWindowSizes = listOf(
                ThreeDSWindowSizes(ActionType.AcsWindowSize.ACS_250x400, "250x400"),
                ThreeDSWindowSizes(ActionType.AcsWindowSize.ACS_390x400, "390x400"),
                ThreeDSWindowSizes(ActionType.AcsWindowSize.ACS_500x600, "500x600"),
                ThreeDSWindowSizes(ActionType.AcsWindowSize.ACS_600x400, "600x400"),
                ThreeDSWindowSizes(ActionType.AcsWindowSize.ACS_FULL_PAGE, "Full Page")
            )
        ),
        customer = WPayCustomerSettings(
            userId = "1234563455633",
            apiKey = "dvdsdfggadaa",
            walletId = "",
            useEveryDayPay = false
        ),
        paymentRequest = InitialPaymentRequest(
            amount = "12.40",
            maxUses = 3,
            fraud = false,
            fraudPayload = object : FraudPayload {
                override val format: FraudPayloadFormat
                    get() = FraudPayloadFormat.XML

                override val message: String
                    get() = """<?xml version="1.0" encoding="utf-8" ?><requestMessage xmlns="urn:schemas-cybersource-com:transaction-data-1.101"><!-- TODO: Fill me --></requestMessage>"""

                override val provider: String
                    get() = "cybersource"

                override val responseFormat: FraudPayloadFormat
                    get() = FraudPayloadFormat.XML

                override val version: String
                    get() = "CyberSourceTransaction_1.101"

            }
        )
    )