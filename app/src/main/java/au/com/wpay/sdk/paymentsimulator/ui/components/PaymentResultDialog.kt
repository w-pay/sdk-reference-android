package au.com.wpay.sdk.paymentsimulator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.com.wpay.sdk.paymentsimulator.model.PaymentOutcomes

@Composable
fun PaymentResultDialog(
    result: State<PaymentOutcomes>,
    show: Boolean,
    dismiss: () -> Unit
) {
    val text = when(result.value) {
        is PaymentOutcomes.Success -> "Payment successful"
        is PaymentOutcomes.Failure -> "Payment failed"
        else -> ""
    }

    val icon = when(result.value) {
        is PaymentOutcomes.Success ->  Icons.Filled.Check
        else -> Icons.Rounded.Close
    }

    val backgroundColour = when(result.value) {
        is PaymentOutcomes.Success -> Color(28, 154, 37)
        else -> Color.Red
    }

    if (show) {
        AlertDialog(
            onDismissRequest = dismiss,
            title = {
                Text(text = "Payment Result")
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp)
                    )

                    Box(modifier = Modifier
                        .clip(CircleShape)
                        .size(100.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Payment result icon",
                            tint = Color.White,
                            modifier = Modifier
                                .background(backgroundColour)
                                .fillMaxSize()
                        )
                    }
                }
            },
            buttons = {
                Row(
                    modifier = Modifier.padding(all = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = dismiss
                    ) {
                        Text("OK")
                    }
                }
            }
        )
    }
}

@Preview(
    showBackground = true
)
@Composable
fun PaymentResultDialogPreview() {
    PaymentResultDialog(
        result = remember { mutableStateOf(PaymentOutcomes.Success) },
        show = true,
        dismiss = {}
    )
}