package au.com.wpay.sdk.paymentsimulator.settings

import au.com.woolworths.village.sdk.VillageCustomerOptions
import au.com.woolworths.village.sdk.VillageMerchantOptions
import au.com.woolworths.village.sdk.model.FraudPayload
import au.com.woolworths.village.sdk.model.FraudPayloadFormat
import au.com.woolworths.village.sdk.model.NewPaymentRequest
import au.com.wpay.sdk.paymentsimulator.PaymentSimulatorActions

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
    val threeDSWindowSizes: List<String>
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

interface WPaySettingsActions : PaymentSimulatorActions {
    fun onCreatePaymentRequest(
        merchant: VillageMerchantOptions,
        customer: VillageCustomerOptions,
        paymentRequest: NewPaymentRequest,
    )
}

fun defaultSettingsProps() =
    WPaySettingsProps(
        merchant = WPayMerchantSettings(
            merchantId = "aMerchant",
            apiKey = "dfdafasfdasfadfads",
            require3DSNPA = false,
            require3DSPA = false,
            threeDSWindowSizes = listOf(
                "",
                "01 - 250x400",
                "02 - 390x400",
                "03 - 500x600",
                "04 - 600x400",
                "05 - Full Page"
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