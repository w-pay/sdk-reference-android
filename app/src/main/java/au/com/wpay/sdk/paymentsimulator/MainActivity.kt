package au.com.wpay.sdk.paymentsimulator

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import au.com.wpay.sdk.paymentsimulator.ui.theme.WPayPaymentSimulatorAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WPayPaymentSimulatorAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Toolbar(dimensionResource(id = getActionBarSize()))
                    }
                }
            }
        }
    }

    // TODO: Figure out better way to get attr value in Compose
    private fun getActionBarSize(): Int =
        getResIdFromAttribute(this, android.R.attr.actionBarSize)
}

fun getResIdFromAttribute(context: Context, attr: Int): Int {
    if (attr == 0) {
        return 0
    }

    val typedValue = TypedValue()
    context.theme.resolveAttribute(attr, typedValue, true)

    return typedValue.resourceId
}

@Composable
fun Toolbar(height: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(Color.White),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.wpay_logo),
            contentDescription = "",
            modifier = Modifier.scale(0.7f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WPayPaymentSimulatorAppTheme {
        Toolbar(Dp(56.0f))
    }
}