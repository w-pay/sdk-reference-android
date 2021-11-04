package au.com.wpay.sdk.paymentsimulator.model

sealed class PaymentOutcomes {
    object NoOutcome : PaymentOutcomes()
    object Success : PaymentOutcomes()
    data class Failure(val reason: String) : PaymentOutcomes()
}