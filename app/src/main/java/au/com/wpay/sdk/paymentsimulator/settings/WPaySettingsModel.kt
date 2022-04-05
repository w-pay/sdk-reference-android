package au.com.wpay.sdk.paymentsimulator.settings

import au.com.wpay.sdk.model.FraudPayload
import au.com.wpay.sdk.model.FraudPayloadFormat
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
            fraudPayload = FraudPayload(
                format = FraudPayloadFormat.XML,
                message = """<?xml version="1.0" encoding="utf-8" ?><requestMessage xmlns="urn:schemas-cybersource-com:transaction-data-1.101"><merchantReferenceCode>TEST98765</merchantReferenceCode><billTo><firstName>Tony</firstName><lastName>Stark</lastName><street1>Malibu Point 10880</street1><city>Malibu</city><state>CA</state><postalCode>90265</postalCode><country>US</country><phoneNumber>678-136-7092</phoneNumber><email>tonystark@cybersource.com</email><ipAddress>10.7.7.7</ipAddress></billTo><shipTo><firstName>CONTAINER</firstName><lastName>TESTING</lastName><street1>100 Elm Street</street1><city>San Mateo</city><state>CA</state><postalCode>94401</postalCode><country>US</country></shipTo><purchaseTotals><currency>USD</currency><grandTotalAmount>3000.00</grandTotalAmount></purchaseTotals><card><expirationMonth>12</expirationMonth><expirationYear>30</expirationYear></card><merchantDefinedData><mddField id="19">Pickup</mddField><mddField id="10">NO</mddField><mddField id="3">ALEXANDRIA Click &amp; Collect, ALEXANDRIA</mddField><mddField id="1">2017-09-05 11:00</mddField><mddField id="2">NSW</mddField><mddField id="12">NO</mddField><mddField id="16">130</mddField><mddField id="23"></mddField><mddField id="17">2017-09-04 15:56</mddField><mddField id="18">2017-09-04 15:56</mddField><mddField id="25">2017-09-05 11:00</mddField><mddField id="20">WEB</mddField><mddField id="57">Normal</mddField><mddField id="58"></mddField><mddField id="59" /><mddField id="60">1002.20</mddField></merchantDefinedData><deviceFingerprintID>18SFXO-26IFULS2</deviceFingerprintID><afsService run="true"/></requestMessage>""",
                provider = "cybersource",
                responseFormat = FraudPayloadFormat.XML,
                version = "CyberSourceTransaction_1.101"
            )
        )
    )