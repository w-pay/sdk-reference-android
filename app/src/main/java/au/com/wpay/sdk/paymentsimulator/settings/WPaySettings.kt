package au.com.wpay.sdk.paymentsimulator.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.com.woolworths.village.sdk.Wallet
import au.com.woolworths.village.sdk.model.FraudPayload
import au.com.woolworths.village.sdk.model.NewPaymentRequest
import au.com.wpay.sdk.paymentsimulator.SimulatorCustomerOptions
import au.com.wpay.sdk.paymentsimulator.SimulatorMerchantOptions
import au.com.wpay.sdk.paymentsimulator.model.SimulatorPaymentRequest
import au.com.wpay.sdk.paymentsimulator.ui.components.ComboBox
import au.com.wpay.sdk.paymentsimulator.ui.components.LayoutBox
import au.com.wpay.sdk.paymentsimulator.ui.components.PrimaryButton
import au.com.wpay.sdk.paymentsimulator.ui.theme.Typography
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.math.BigDecimal

enum class WPayEnvironment(
    val text: String,
    val baseUrl: String
) {
    DEV1("Dev 1", "https://dev.mobile-api.woolworths.com.au"),
    UAT("UAT", "https://test.mobile-api.woolworths.com.au")
}

@ExperimentalMaterialApi
@Composable
fun WPaySettings(
    props: WPaySettingsProps,
    actions: WPaySettingsActions
) {
    val data = SettingsData(
        env = remember { mutableStateOf(WPayEnvironment.DEV1) },
        merchant = MerchantData(
            merchantId = remember { mutableStateOf(props.merchant.merchantId) },
            apiKey = remember { mutableStateOf(props.merchant.apiKey) },
            require3DSNPA = remember { mutableStateOf(props.merchant.require3DSNPA) },
            require3DSPA = remember { mutableStateOf(props.merchant.require3DSPA) },
            threeDSWindowSize = remember { mutableStateOf(null) }
        ),
        customer = CustomerData(
            userId = remember { mutableStateOf(props.customer.userId) },
            walletId = remember { mutableStateOf(props.customer.walletId) },
            apiKey = remember { mutableStateOf(props.customer.apiKey) },
            useEveryDayPay = remember { mutableStateOf(props.customer.useEveryDayPay) },
        ),
        paymentRequest = PaymentRequestData(
            amount = remember { mutableStateOf(props.paymentRequest.amount) },
            maxUses = remember { mutableStateOf(props.paymentRequest.maxUses.toString()) },
            fraud = remember { mutableStateOf(props.paymentRequest.fraud) },
            fraudPayload = props.paymentRequest.fraudPayload
        )
    )

    var createEnabled: Boolean by rememberSaveable { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LayoutBox {
        Column {
            Setting(
                text = "Environment",
                input = {
                    ComboBox(
                        items = WPayEnvironment.values().map { it.text },
                        modifier = Modifier.weight(1f),
                        onClick = {
                            data.env.value = WPayEnvironment.values()[it]
                        }
                    )
                }
            )

            Heading(text = "Merchant Details")

            Setting(
                text = "Merchant ID",
                input = {
                    TextField(
                        value = data.merchant.merchantId.value,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {
                            data.merchant.merchantId.value = it
                        }
                    )
                }
            )

            Setting(
                text = "API Key",
                input = {
                    TextField(
                        value = data.merchant.apiKey.value,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {
                            data.merchant.apiKey.value = it
                        }
                    )
                }
            )

            Setting(
                text = "Require 3DS - Card Capture",
                input = {
                    Checkbox(
                        checked = data.merchant.require3DSNPA.value,
                        modifier = Modifier.weight(1f),
                        onCheckedChange = {
                            data.merchant.require3DSNPA.value = it
                        }
                    )
                }
            )

            Setting(
                text = "Require 3DS - Payment",
                input = {
                    Checkbox(
                        checked = data.merchant.require3DSPA.value,
                        modifier = Modifier.weight(1f),
                        onCheckedChange = {
                            data.merchant.require3DSPA.value = it
                        }
                    )
                }
            )

            Setting(
                text = "3DS - Window Size",
                input = {
                    ComboBox(
                        items = props.merchant.threeDSWindowSizes,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            data.merchant.threeDSWindowSize.value = props.merchant.threeDSWindowSizes[it]
                        }
                    )
                }
            )

            Heading(text = "Customer Details")

            Setting(
                text = "User ID",
                input = {
                    TextField(
                        value = data.customer.userId.value,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {
                            data.customer.userId.value = it
                        }
                    )
                }
            )

            Setting(
                text = "API Key",
                input = {
                    TextField(
                        value = data.customer.apiKey.value,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {
                            data.customer.apiKey.value = it
                        }
                    )
                }
            )

            Setting(
                text = "Use Everyday Pay Wallet",
                input = {
                    Checkbox(
                        checked = data.customer.useEveryDayPay.value,
                        modifier = Modifier.weight(1f),
                        onCheckedChange = {
                            data.customer.useEveryDayPay.value = it
                        }
                    )
                }
            )

            Heading(text = "Payment Request")

            Setting(
                text = "Total",
                input = {
                    TextField(
                        value = data.paymentRequest.amount.value,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {
                            data.paymentRequest.amount.value = it
                        }
                    )
                }
            )

            Setting(
                text = "Max Uses",
                input = {
                    TextField(
                        value = data.paymentRequest.maxUses.value,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {
                            data.paymentRequest.maxUses.value = it
                        }
                    )
                }
            )

            Setting(
                text = "Enable Fraud Checking",
                input = {
                    Checkbox(
                        checked = data.paymentRequest.fraud.value,
                        modifier = Modifier.weight(1f),
                        onCheckedChange = {
                            data.paymentRequest.fraud.value = it
                        }
                    )
                }
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 20.dp, 0.dp, 0.dp)
            ) {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    enabled = createEnabled,
                    waiting = !createEnabled,
                    text = "Create new payment request",
                    onClick = {
                        scope.launch {
                            createEnabled = false
                            createPaymentRequest(data, actions)
                            createEnabled = true
                        }
                    }
                )
            }
        }
    }
}

@ExperimentalMaterialApi
@Preview(
    showBackground = true
)
@Composable
private fun WPaySettingsPreview() {
    val scope = rememberCoroutineScope()

    @Suppress("DeferredIsResult")
    val actions = object : WPaySettingsActions {
        override fun onCreatePaymentRequest(
            merchant: SimulatorMerchantOptions,
            customer: SimulatorCustomerOptions,
            paymentRequest: NewPaymentRequest
        ): Deferred<Unit> {
            return scope.async {}
        }

        override fun onError(error: Exception) {

        }
    }

    WPaySettings(
        props = defaultSettingsProps(),
        actions = actions
    )
}

@Composable
private fun Heading(
    text: String
) {
    Row(
        modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 20.dp)
    ) {
        Text(
            text = text,
            style = Typography.subtitle1
        )
    }
}

@Composable
private fun Setting(
    text: String,
    input: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .weight(1f)
                .padding(0.dp, 0.dp, 20.dp, 0.dp)
        )

        input()
    }
}

private suspend fun createPaymentRequest(
    data: SettingsData,
    actions: WPaySettingsActions
) {
    try {
        actions.onCreatePaymentRequest(
            merchant = SimulatorMerchantOptions(
                baseUrl = data.env.value.baseUrl,
                apiKey = data.merchant.apiKey.value,
                merchantId = data.merchant.merchantId.value,
                wallet = when (data.customer.useEveryDayPay.value) {
                    true -> Wallet.EVERYDAY_PAY
                    else -> Wallet.MERCHANT
                }
            ),
            customer = SimulatorCustomerOptions(
                baseUrl = data.env.value.baseUrl,
                apiKey = data.customer.apiKey.value,
                walletId = data.customer.walletId.value,
                wallet = when (data.customer.useEveryDayPay.value) {
                    true -> Wallet.EVERYDAY_PAY
                    else -> Wallet.MERCHANT
                },
                customerId = data.customer.userId.value
            ),
            paymentRequest = SimulatorPaymentRequest(
                grossAmount = BigDecimal(data.paymentRequest.amount.value),
                maxUses = Integer.parseInt(data.paymentRequest.maxUses.value),
                require3DSPA = data.merchant.require3DSPA.value
            )
        ).await()
    }
    catch (e: Exception) {
        actions.onError(e)
    }
}

private data class SettingsData(
    val env: MutableState<WPayEnvironment>,
    val merchant: MerchantData,
    val customer: CustomerData,
    val paymentRequest: PaymentRequestData
)

private data class MerchantData(
    val merchantId: MutableState<String>,
    val apiKey: MutableState<String>,
    val require3DSNPA: MutableState<Boolean>,
    val require3DSPA: MutableState<Boolean>,
    val threeDSWindowSize: MutableState<String?>
)

private data class CustomerData(
    val userId: MutableState<String>,
    val walletId: MutableState<String>,
    val apiKey: MutableState<String>,
    val useEveryDayPay: MutableState<Boolean>
)

private data class PaymentRequestData(
    val amount: MutableState<String>,
    val maxUses: MutableState<String>,
    val fraud: MutableState<Boolean>,
    val fraudPayload: FraudPayload
)