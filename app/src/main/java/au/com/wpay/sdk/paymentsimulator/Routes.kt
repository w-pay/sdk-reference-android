package au.com.wpay.sdk.paymentsimulator

sealed class Routes(val route: String) {
    object Settings : Routes("settings")
    object PaymentDetails : Routes("payment-details")
}