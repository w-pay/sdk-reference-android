package au.com.wpay.sdk.paymentsimulator

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.com.wpay.sdk.paymentsimulator.ui.components.ComboBox
import au.com.wpay.sdk.paymentsimulator.ui.components.LayoutBox
import au.com.wpay.sdk.paymentsimulator.ui.theme.Typography

@ExperimentalMaterialApi
@Composable
fun WPaySettings(
    onCreatePaymentRequest: () -> Unit
) {
    val envs = listOf("Dev1", "UAT")
    val windowSizes = listOf(
        "",
        "01 - 250x400",
        "02 - 390x400",
        "03 - 500x600",
        "04 - 600x400",
        "05 - Full Page"
    )

    LayoutBox {
        Column {
            Setting(
                text = "Environment",
                input = {
                    ComboBox(
                        items = envs,
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    )
                }
            )

            Heading(text = "Merchant Details")

            Setting(
                text = "Merchant ID",
                input = {
                    TextField(
                        value = "petculture",
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {}
                    )
                }
            )

            Setting(
                text = "API Key",
                input = {
                    TextField(
                        value = "abc1234",
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {}
                    )
                }
            )

            Setting(
                text = "Require 3DS - Card Capture",
                input = {
                    Checkbox(
                        checked = false,
                        modifier = Modifier.weight(1f),
                        onCheckedChange = {}
                    )
                }
            )

            Setting(
                text = "Require 3DS - Payment",
                input = {
                    Checkbox(
                        checked = false,
                        modifier = Modifier.weight(1f),
                        onCheckedChange = {}
                    )
                }
            )

            Setting(
                text = "3DS - Window Size",
                input = {
                    ComboBox(
                        items = windowSizes,
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    )
                }
            )

            Heading(text = "Customer Details")

            Setting(
                text = "User ID",
                input = {
                    TextField(
                        value = "1100000000093126352",
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {}
                    )
                }
            )

            Setting(
                text = "API Key",
                input = {
                    TextField(
                        value = "abc1234",
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {}
                    )
                }
            )

            Setting(
                text = "Use Everyday Pay Wallet",
                input = {
                    Checkbox(
                        checked = false,
                        modifier = Modifier.weight(1f),
                        onCheckedChange = {}
                    )
                }
            )

            Heading(text = "Payment Request")

            Setting(
                text = "Total",
                input = {
                    TextField(
                        value = "12.4",
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {}
                    )
                }
            )

            Setting(
                text = "Max Uses",
                input = {
                    TextField(
                        value = "3",
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        onValueChange = {}
                    )
                }
            )

            Setting(
                text = "Enable Fraud Checking",
                input = {
                    Checkbox(
                        checked = false,
                        modifier = Modifier.weight(1f),
                        onCheckedChange = {}
                    )
                }
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 20.dp, 0.dp, 0.dp)
            ) {
                Button(
                    onClick = onCreatePaymentRequest
                ) {
                    Text(text = "Create new payment request")
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
private fun WPaySettingsPreview() {
    WPaySettings {

    }
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