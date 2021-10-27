package au.com.wpay.sdk.paymentsimulator.model

import au.com.woolworths.village.sdk.model.Basket
import au.com.woolworths.village.sdk.model.MerchantPayload
import au.com.woolworths.village.sdk.model.NewPaymentRequest
import au.com.woolworths.village.sdk.model.PosPayload
import java.math.BigDecimal
import java.util.*

data class SimulatorPaymentRequest(
    override val grossAmount: BigDecimal,
    override val maxUses: Int,

    private val require3DSPA: Boolean
) : NewPaymentRequest {
    override val basket: Basket?
        get() = null

    override val generateQR: Boolean
        get() = false

    override val merchantPayload: MerchantPayload
        get() = object : MerchantPayload {
            override val schemaId: String
                get() = "0a221353-b26c-4848-9a77-4a8bcbacf228"

            override val payload: Map<String, Any>
                get() = mapOf(
                    "requires3DS" to require3DSPA
                )
        }

    override val merchantReferenceId: String
        get() = UUID.randomUUID().toString()

    override val posPayload: PosPayload?
        get() = null

    override val specificWalletId: String?
        get() = null

    override val timeToLivePayment: Int
        get() = 300

    override val timeToLiveQR: Int?
        get() = null
}