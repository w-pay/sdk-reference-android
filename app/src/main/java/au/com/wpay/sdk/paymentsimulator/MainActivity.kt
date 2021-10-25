package au.com.wpay.sdk.paymentsimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import au.com.wpay.sdk.paymentsimulator.ui.theme.WPayPaymentSimulatorAppTheme

@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            WPayPaymentSimulatorAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Scaffold(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Column {
                            Toolbar()
                            NavHost(navController = navController, startDestination = "settings") {
                                composable("settings") { WPaySettings() }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Toolbar() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.wpay_logo),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(0.7f)
                    .padding(0.dp, 20.dp, 0.dp, 0.dp)
            )

            Text(
                text = "Payment Simulator",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WPayPaymentSimulatorAppTheme {
        Toolbar()
    }
}