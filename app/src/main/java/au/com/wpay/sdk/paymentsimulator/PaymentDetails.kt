package au.com.wpay.sdk.paymentsimulator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.com.wpay.sdk.paymentsimulator.ui.components.LayoutBox
import au.com.wpay.sdk.paymentsimulator.ui.theme.Typography

@Preview
@Composable
fun PaymentDetails() {
    LayoutBox {
        Column {
            Heading()
        }
    }
}

@Composable
private fun Heading() {
    Row(
        modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 20.dp)
    ) {
        Text(
            text = "How would you like to pay?",
            style = Typography.subtitle1
        )
    }
}