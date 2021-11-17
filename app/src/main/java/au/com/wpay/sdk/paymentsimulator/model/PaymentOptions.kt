package au.com.wpay.sdk.paymentsimulator.model

import au.com.woolworths.village.sdk.model.CreditCard

sealed class PaymentOptions {
    data class NewCard(val valid: Boolean) : PaymentOptions() {
        override fun isValid(): Boolean {
            return valid
        }
    }

    data class ExistingCard(val card: CreditCard?) : PaymentOptions() {
        override fun isValid(): Boolean {
            return card != null
        }
    }

    object NoOption : PaymentOptions() {
        override fun isValid(): Boolean = false
    }

    abstract fun isValid(): Boolean
}