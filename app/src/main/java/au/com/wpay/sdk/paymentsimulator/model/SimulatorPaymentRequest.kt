package au.com.wpay.sdk.paymentsimulator.model

import au.com.wpay.sdk.model.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.math.BigDecimal
import java.util.*

data class SimulatorPaymentRequest(
    val grossAmount: BigDecimal,
    val maxUses: Int,
    val require3DSPA: Boolean,
    val fraudPayload: FraudPayload?
) {
    fun toPaymentRequest(): NewPaymentRequest =
        NewPaymentRequest(
            merchantReferenceId = UUID.randomUUID().toString(),
            grossAmount = grossAmount,
            generateQR = false,
            merchantPayload = MerchantPayload(
                schemaId = "0a221353-b26c-4848-9a77-4a8bcbacf228",
                payload = JsonObject(mapOf(
                    "requires3DS" to JsonPrimitive(require3DSPA)
                ))
            ),
            timeToLivePayment = 300
        )
}