package au.com.wpay.sdk.paymentsimulator.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    waiting: Boolean = false,
    text: String
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick
    ) {
        if (!waiting) {
            Text(text = text)
        }
        else {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}